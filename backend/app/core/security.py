import os
import bcrypt
import jwt
from datetime import datetime, timedelta
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials


def _get_jwt_secret() -> str:
    """Load and validate JWT secret from environment."""
    secret = os.getenv("JWT_SECRET_KEY")

    if not secret:
        if os.getenv("ENVIRONMENT", "development").lower() == "production":
            raise RuntimeError(
                "JWT_SECRET_KEY environment variable is required in production. "
                "Set a strong, unique secret of at least 32 characters."
            )
        # Development fallback with warning
        import warnings
        warnings.warn(
            "JWT_SECRET_KEY not set - using insecure default. "
            "Set JWT_SECRET_KEY environment variable for production.",
            UserWarning
        )
        return "dev_only_insecure_secret_do_not_use_in_prod"

    # Validate secret strength
    if len(secret) < 32:
        raise ValueError(
            "JWT_SECRET_KEY must be at least 32 characters long for security."
        )
    if secret.lower() in ("secret", "password", "changeme", "jwt_secret"):
        raise ValueError(
            "JWT_SECRET_KEY is too weak. Use a strong, unique secret."
        )

    return secret


SECRET_KEY = _get_jwt_secret()
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_DAYS = 30

security_bearer = HTTPBearer(auto_error=False)


def hash_password(password: str) -> str:
    """Hashes a password using bcrypt."""
    salt = bcrypt.gensalt()
    return bcrypt.hashpw(password.encode('utf-8'), salt).decode('utf-8')


def verify_password(plain_password: str, hashed_password: str) -> bool:
    """Verifies a plain password against a hashed one."""
    return bcrypt.checkpw(plain_password.encode('utf-8'), hashed_password.encode('utf-8'))


def create_access_token(data: dict, expires_delta: timedelta | None = None) -> str:
    """Generates a signed JWT access token."""
    to_encode = data.copy()
    expire = datetime.utcnow() + (expires_delta or timedelta(days=ACCESS_TOKEN_EXPIRE_DAYS))
    to_encode.update({"exp": expire})
    return jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)


def decode_access_token(token: str) -> dict | None:
    """Decodes and validates a JWT token."""
    try:
        return jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
    except jwt.PyJWTError:
        return None


def get_current_user(auth: HTTPAuthorizationCredentials | None = Depends(security_bearer)) -> dict:
    """FastAPI Dependency enforcing JWT Bearer token authentication."""
    if not auth or not auth.credentials:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Authentication token required."
        )
    payload = decode_access_token(auth.credentials)
    if not payload or "sub" not in payload:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or expired authentication token."
        )
    return payload