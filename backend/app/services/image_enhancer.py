import cv2
import numpy as np
from PIL import Image
import io

def enhance_image_for_ocr(image_bytes: bytes) -> Image.Image:
    """
    Enhances an image for better OCR accuracy, especially for handwriting
    and bilingual documents (English/Kannada).
    """
    # 1. Convert bytes to OpenCV image
    nparr = np.frombuffer(image_bytes, np.uint8)
    img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
    
    if img is None:
        return Image.open(io.BytesIO(image_bytes))

    # 2. Rescaling
    height, width = img.shape[:2]
    if width < 1800:
        scale = 2000 / width
        img = cv2.resize(img, None, fx=scale, fy=scale, interpolation=cv2.INTER_CUBIC)

    # 3. Grayscale conversion
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    # 4. CLAHE (Contrast Limited Adaptive Histogram Equalization)
    # This is magic for shadowed/unevenly lit documents
    clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8,8))
    enhanced_gray = clahe.apply(gray)

    # 5. Denoising (Fast Non-Local Means)
    denoised = cv2.fastNlMeansDenoising(enhanced_gray, h=10)

    # 6. Sharpening
    kernel = np.array([[-1,-1,-1], [-1,9,-1], [-1,-1,-1]])
    sharpened = cv2.filter2D(denoised, -1, kernel)

    # 7. Convert to PIL
    return Image.fromarray(sharpened)
