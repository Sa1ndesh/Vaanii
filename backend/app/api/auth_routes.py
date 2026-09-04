from fastapi import APIRouter, HTTPException, Depends
import mysql.connector
from mysql.connector import errorcode
import secrets
from datetime import datetime, timedelta
from fastapi_mail import FastMail, MessageSchema, ConnectionConfig, MessageType
import os

from app.schemas import (
    UserCreate, 
    UserLogin, 
    UserOut, 
    ForgotPasswordRequest, 
    ResetPasswordRequest, 
    UpdatePasswordRequest,
    UserUpdateProfile
)
from app.core.security import hash_password, verify_password, create_access_token, get_current_user
from app.core.database import get_db_connection

router = APIRouter(prefix="/api/v1/auth", tags=["Auth"])

# --------------------------------------------------------------------------
# 1. SIGN UP
# --------------------------------------------------------------------------
@router.post("/signup", response_model=UserOut)
def create_user(user: UserCreate):
    hashed_pw = hash_password(user.password)
    db_conn = None
    cursor = None
    try:
        db_conn = get_db_connection()
        if db_conn is None:
            raise HTTPException(status_code=500, detail="Database connection failed.")
        cursor = db_conn.cursor(dictionary=True)
        
        query = "INSERT INTO users (name, email, hashed_password, gender, phone_number, profile_pic) VALUES (%s, %s, %s, %s, %s, %s)"
        cursor.execute(query, (user.name, user.email, hashed_pw, user.gender, user.phone_number, None))
        db_conn.commit()
        
        new_user_id = cursor.lastrowid
        access_token = create_access_token({"sub": user.email, "id": new_user_id})

        return {
            "id": new_user_id,
            "name": user.name,
            "email": user.email, 
            "gender": user.gender,
            "phone_number": user.phone_number,
            "profile_pic": None,
            "access_token": access_token,
            "token_type": "bearer"
        }

    except mysql.connector.Error as err:
        print(f"[AUTH DEBUG] Database error during signup: {err}")
        if err.errno == 1062:
            raise HTTPException(status_code=400, detail="Email already registered. Please login or use a different email.")
        else:
            raise HTTPException(status_code=500, detail=f"Database error: {err.msg}")
    finally:
        if cursor: cursor.close()
        if db_conn: db_conn.close()

# --------------------------------------------------------------------------
# 2. LOGIN
# --------------------------------------------------------------------------
@router.post("/login", response_model=UserOut)
def login_user(form_data: UserLogin):
    db_conn = None
    cursor = None
    try:
        db_conn = get_db_connection()
        if db_conn is None:
            raise HTTPException(status_code=500, detail="Database connection failed.")
        cursor = db_conn.cursor(dictionary=True)
        
        query = "SELECT * FROM users WHERE email = %s"
        cursor.execute(query, (form_data.email,))
        user = cursor.fetchone()

        if user is None or not verify_password(form_data.password, user["hashed_password"]):
            raise HTTPException(status_code=401, detail="Incorrect email or password.")

        access_token = create_access_token({"sub": user["email"], "id": user["id"]})
        user["access_token"] = access_token
        user["token_type"] = "bearer"

        return UserOut(**user)
    except mysql.connector.Error as err:
        raise HTTPException(status_code=500, detail=f"Database error: {err.msg}")
    finally:
        if cursor: cursor.close()
        if db_conn: db_conn.close()

# --------------------------------------------------------------------------
# 3. FORGOT PASSWORD
# --------------------------------------------------------------------------
@router.post("/forgot-password")
async def forgot_password(request: ForgotPasswordRequest):
    db_conn = None
    cursor = None
    try:
        db_conn = get_db_connection()
        if db_conn is None:
            raise HTTPException(status_code=500, detail="Database connection failed.")
        cursor = db_conn.cursor(dictionary=True)
        cursor.execute("SELECT * FROM users WHERE email = %s", (request.email,))
        user = cursor.fetchone()
        if not user:
            return {"message": "If that email exists, we have sent a reset link."}
        token = secrets.token_urlsafe(32)
        expires = datetime.now() + timedelta(minutes=15)
        cursor.execute("UPDATE users SET reset_token = %s, reset_token_expires = %s WHERE id = %s", (token, expires, user['id']))
        db_conn.commit()
        
        frontend_url = os.getenv("FRONTEND_URL", "http://localhost:5173")
        reset_link = f"{frontend_url}/reset-password?token={token}"
        html = f"<p>Hello {user['name']},</p><p>Click here to reset: <a href='{reset_link}'>Reset Password</a></p>"
        message = MessageSchema(subject="Vani-Kanoon Password Reset", recipients=[request.email], body=html, subtype=MessageType.html)
        conf = ConnectionConfig(
            MAIL_USERNAME=os.getenv("MAIL_USERNAME", ""),
            MAIL_PASSWORD=os.getenv("MAIL_PASSWORD", ""),
            MAIL_FROM=os.getenv("MAIL_FROM", "noreply@vanikanoon.com"),
            MAIL_PORT=int(os.getenv("MAIL_PORT", 587)),
            MAIL_SERVER=os.getenv("MAIL_SERVER", "smtp.gmail.com"),
            MAIL_STARTTLS=True,
            MAIL_SSL_TLS=False,
            USE_CREDENTIALS=True,
            VALIDATE_CERTS=True
        )
        fm = FastMail(conf)
        await fm.send_message(message)
        return {"message": "Reset link sent to your email."}
    except Exception as e:
        print(f"Error sending mail: {e}")
        return {"message": "If that email exists, we have sent a reset link."}
    finally:
        if cursor: cursor.close()
        if db_conn: db_conn.close()

# --------------------------------------------------------------------------
# 4. RESET PASSWORD
# --------------------------------------------------------------------------
@router.post("/reset-password")
async def reset_password(request: ResetPasswordRequest):
    db_conn = None
    cursor = None
    try:
        db_conn = get_db_connection()
        if db_conn is None:
            raise HTTPException(status_code=500, detail="Database connection failed.")
        cursor = db_conn.cursor(dictionary=True)
        cursor.execute("SELECT * FROM users WHERE reset_token = %s AND reset_token_expires > NOW()", (request.token,))
        user = cursor.fetchone()
        if not user:
            raise HTTPException(status_code=400, detail="Invalid or expired token.")
        new_hashed_pw = hash_password(request.new_password)
        cursor.execute("UPDATE users SET hashed_password = %s, reset_token = NULL, reset_token_expires = NULL WHERE id = %s", (new_hashed_pw, user['id']))
        db_conn.commit()
        return {"message": "Password updated successfully."}
    except mysql.connector.Error as err:
        raise HTTPException(status_code=500, detail=f"Database error: {err.msg}")
    finally:
        if cursor: cursor.close()
        if db_conn: db_conn.close()

# --------------------------------------------------------------------------
# 5. UPDATE PASSWORD (PROTECTED via JWT Token)
# --------------------------------------------------------------------------
@router.put("/update-password")
def update_password(
    request: UpdatePasswordRequest,
    current_user: dict = Depends(get_current_user)
):
    db_conn = None
    cursor = None
    try:
        user_email = current_user.get("sub") or request.email
        db_conn = get_db_connection()
        if db_conn is None:
            raise HTTPException(status_code=500, detail="Database connection failed.")
        cursor = db_conn.cursor(dictionary=True)
        cursor.execute("SELECT * FROM users WHERE email = %s", (user_email,))
        user = cursor.fetchone()
        if not user:
            raise HTTPException(status_code=404, detail="User not found.")
        if not verify_password(request.current_password, user['hashed_password']):
            raise HTTPException(status_code=401, detail="Incorrect current password.")
        new_hashed_pw = hash_password(request.new_password)
        cursor.execute("UPDATE users SET hashed_password = %s WHERE id = %s", (new_hashed_pw, user['id']))
        db_conn.commit()
        return {"message": "Password updated successfully."}
    except mysql.connector.Error as err:
        raise HTTPException(status_code=500, detail=f"Database error: {err.msg}")
    finally:
        if cursor: cursor.close()
        if db_conn: db_conn.close()

# --------------------------------------------------------------------------
# 6. UPDATE PROFILE (PROTECTED via JWT Token)
# --------------------------------------------------------------------------
@router.put("/update-profile", response_model=UserOut)
def update_user_profile(
    profile_data: UserUpdateProfile,
    current_user: dict = Depends(get_current_user)
):
    db_conn = None
    cursor = None
    try:
        user_email = current_user.get("sub") or profile_data.email
        db_conn = get_db_connection()
        if db_conn is None:
            raise HTTPException(status_code=500, detail="Database connection failed.")
        cursor = db_conn.cursor(dictionary=True)

        cursor.execute("SELECT * FROM users WHERE email = %s", (user_email,))
        user = cursor.fetchone()
        if not user:
            raise HTTPException(status_code=404, detail="User not found.")

        update_query = """
            UPDATE users 
            SET name = %s, gender = %s, phone_number = %s, profile_pic = %s
            WHERE email = %s
        """
        cursor.execute(update_query, (
            profile_data.name, 
            profile_data.gender, 
            profile_data.phone_number, 
            profile_data.profile_pic,
            user_email
        ))
        db_conn.commit()
        
        cursor.execute("SELECT * FROM users WHERE email = %s", (user_email,))
        updated_user = cursor.fetchone()
        access_token = create_access_token({"sub": updated_user["email"], "id": updated_user["id"]})
        updated_user["access_token"] = access_token
        updated_user["token_type"] = "bearer"
        return UserOut(**updated_user)

    except mysql.connector.Error as err:
        raise HTTPException(status_code=500, detail=f"Database error: {err.msg}")
    finally:
        if cursor: cursor.close()
        if db_conn: db_conn.close()