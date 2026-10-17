from fastapi import HTTPException, UploadFile, status

ALLOWED_MIME_TYPES = {
    "image/jpeg": [".jpg", ".jpeg"],
    "image/png": [".png"],
    "image/webp": [".webp"]
}

MAX_FILE_SIZE_BYTES = 8 * 1024 * 1024  # 8 Megabytes

async def validate_image_file(file: UploadFile) -> bytes:
    """
    Validates uploaded image file against size and MIME type constraints.
    Returns the raw byte content if valid.
    """
    if not file or not file.filename:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Image file is required and cannot be empty."
        )

    content_type = (file.content_type or "").lower().strip()
    if content_type not in ALLOWED_MIME_TYPES:
        allowed = ", ".join(ALLOWED_MIME_TYPES.keys())
        raise HTTPException(
            status_code=status.HTTP_415_UNSUPPORTED_MEDIA_TYPE,
            detail=f"Unsupported image type '{content_type}'. Supported types: {allowed}."
        )

    # Read content to check actual byte size
    content = await file.read()
    if len(content) == 0:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Uploaded image file is empty (0 bytes)."
        )

    if len(content) > MAX_FILE_SIZE_BYTES:
        raise HTTPException(
            status_code=status.HTTP_413_REQUEST_ENTITY_TOO_LARGE,
            detail=f"Image exceeds maximum allowable size of 8 MB (received {len(content)/(1024*1024):.2f} MB)."
        )

    return content
