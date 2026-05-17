package com.coffeechain.config;

/**
 * Tập trung toàn bộ URL backend mà frontend gọi tới.
 * Khi đổi port, domain hoặc prefix API, ưu tiên sửa ở đây thay vì sửa rải rác trong các API client.
 */
public class ApiConfig {
    private ApiConfig() {}

    public static final String BASE_URL = "http://localhost:8080";
    public static final String LOGIN_URL = BASE_URL + "/api/auth/login";
    public static final String FORGOT_PASSWORD_URL = BASE_URL + "/api/auth/forgot-password";
    public static final String VERIFY_RESET_CODE_URL = BASE_URL + "/api/auth/forgot-password/verify";
    public static final String RESET_PASSWORD_URL = BASE_URL + "/api/auth/forgot-password/reset";
    public static final String ME_URL = BASE_URL + "/api/auth/me";
    public static final String LOGOUT_URL = BASE_URL + "/api/auth/logout";
    public static final String CHECK_PERMISSION_URL = BASE_URL + "/api/auth/check";
    public static final String PERMISSIONS_URL = BASE_URL + "/api/auth/permissions";
    public static final String INVENTORY_IMPORT_LOOKUPS_URL = BASE_URL + "/api/inventory/imports/lookups";
    public static final String INVENTORY_IMPORTS_URL = BASE_URL + "/api/inventory/imports";
    public static final String INVENTORY_EXPORT_LOOKUPS_URL = BASE_URL + "/api/inventory/exports/lookups";
    public static final String INVENTORY_EXPORT_LOTS_URL = BASE_URL + "/api/inventory/exports/lots";
    public static final String INVENTORY_EXPORTS_URL = BASE_URL + "/api/inventory/exports";
    public static final String INVENTORY_EXPORT_STOCK_URL = BASE_URL + "/api/inventory/exports/stock";
    public static final String INVENTORY_STOCK_URL = BASE_URL + "/api/inventory/stock";
    public static final String INVENTORY_STOCK_LOTS_URL = BASE_URL + "/api/inventory/stock/lots";
    public static final String INVENTORY_STOCK_SUMMARY_URL = BASE_URL + "/api/inventory/stock/summary";
    public static final String USER_LOOKUPS_URL = BASE_URL + "/api/users/lookups";
    public static final String USERS_URL = BASE_URL + "/api/users";
}
