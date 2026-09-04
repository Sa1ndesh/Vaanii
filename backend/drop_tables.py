import mysql.connector
import os
from dotenv import load_dotenv

load_dotenv()

DB_CONFIG = {
    'user': os.getenv('DB_USER'),
    'password': os.getenv('DB_PASSWORD'),
    'host': os.getenv('DB_HOST'),
    'database': os.getenv('DB_NAME')
}

try:
    conn = mysql.connector.connect(**DB_CONFIG)
    cursor = conn.cursor()
    print("Dropping 'chat_history' table (if exists)...")
    cursor.execute("DROP TABLE IF EXISTS chat_history")
    print("Dropping 'users' table (if exists)...")
    cursor.execute("DROP TABLE IF EXISTS users")
    conn.commit()
    cursor.close()
    conn.close()
    print("Tables dropped successfully.")
except Exception as e:
    print(f"Error: {e}")
