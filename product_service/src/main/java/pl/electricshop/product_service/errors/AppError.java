package pl.electricshop.product_service.errors;

public class AppError {
    // Product errors
    public static final String ERROR_PRODUCT_EXISTS = "Product with this name already exists";
    public static final String ERROR_NO_PRODUCTS = "No products found";
    public static final String ERROR_PRODUCT_NOT_FOUND = "Product not found";

    // Category errors
    public static final String ERROR_CATEGORY_EXISTS = "Category with this name already exists";
    public static final String ERROR_CATEGORY_NOT_FOUND = "Category not found";
    public static final String ERROR_CATEGORY_NO_PRODUCTS = "does not have any products";

    // Image errors
    public static final String ERROR_IMAGE_UPLOAD_FAILED = "Failed to upload image";
    public static final String ERROR_INVALID_IMAGE = "Invalid image file. Please upload a valid image";
}
