package com.coffeechain.config;

/**
 * Táº­p trung toĂ n bá»™ URL backend mĂ  frontend gá»i tá»›i.
 * Khi Ä‘á»•i port, domain hoáº·c prefix API, Æ°u tiĂªn sá»­a á»Ÿ Ä‘Ă¢y thay vĂ¬ sá»­a ráº£i rĂ¡c trong cĂ¡c API client.
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
    public static final String INVENTORY_TRANSFER_LOOKUPS_URL = BASE_URL + "/api/inventory/transfers/lookups";
    public static final String INVENTORY_TRANSFER_STOCK_URL = BASE_URL + "/api/inventory/transfers/stock";
    public static final String INVENTORY_TRANSFER_LOTS_URL = BASE_URL + "/api/inventory/transfers/lots";
    public static final String INVENTORY_TRANSFERS_URL = BASE_URL + "/api/inventory/transfers";
    public static final String INVENTORY_STOCK_URL = BASE_URL + "/api/inventory/stock";
    public static final String INVENTORY_STOCK_LOTS_URL = BASE_URL + "/api/inventory/stock/lots";
    public static final String INVENTORY_STOCK_SUMMARY_URL = BASE_URL + "/api/inventory/stock/summary";
    public static final String INVENTORY_EXPIRY_LOOKUPS_URL = BASE_URL + "/api/inventory/expiry/lookups";
    public static final String INVENTORY_EXPIRY_LOTS_URL = BASE_URL + "/api/inventory/expiry/lots";
    public static final String INVENTORY_EXPIRY_STATISTICS_URL = BASE_URL + "/api/inventory/expiry/statistics";
    public static final String INVENTORY_EXPIRY_REFRESH_URL = BASE_URL + "/api/inventory/expiry/refresh";
    public static final String INVENTORY_HISTORY_URL = BASE_URL + "/api/inventory/history";
    public static final String INVENTORY_HISTORY_LOOKUPS_URL = BASE_URL + "/api/inventory/history/lookups";
    public static final String INVENTORY_HISTORY_SUMMARY_URL = BASE_URL + "/api/inventory/history/summary";
    public static final String INVENTORY_WASTAGES_URL = BASE_URL + "/api/inventory/wastages";
    public static final String INVENTORY_WASTAGE_LOOKUPS_URL = BASE_URL + "/api/inventory/wastages/lookups";
    public static final String INVENTORY_WASTAGE_LOTS_URL = BASE_URL + "/api/inventory/wastages/lots";
    public static final String INVENTORY_STOCKTAKES_URL = BASE_URL + "/api/inventory/stocktakes";
    public static final String INVENTORY_STOCKTAKE_LOOKUPS_URL = BASE_URL + "/api/inventory/stocktakes/lookups";
    public static final String INVENTORY_STOCKTAKE_SYSTEM_STOCK_URL = BASE_URL + "/api/inventory/stocktakes/system-stock";
    public static final String SUPPLIERS_URL = BASE_URL + "/api/suppliers";
    public static final String WAREHOUSES_URL = BASE_URL + "/api/warehouses";
    public static final String WAREHOUSE_LOOKUPS_URL = BASE_URL + "/api/warehouses/lookups";
    public static final String INGREDIENTS_URL = BASE_URL + "/api/ingredients";
    public static final String INGREDIENT_LOOKUPS_URL = BASE_URL + "/api/ingredients/lookups";
    public static final String UNITS_URL = BASE_URL + "/api/units";
    public static final String BRANCHES_URL = BASE_URL + "/api/branches";
    public static final String BRANCH_STATISTICS_URL = BASE_URL + "/api/branches/statistics";
    public static final String USER_LOOKUPS_URL = BASE_URL + "/api/users/lookups";
    public static final String USERS_URL = BASE_URL + "/api/users";
}

