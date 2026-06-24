package com.atguigu.common.exception;

public enum BisCodeEnum {
    UNKNOWN_EXCEPTION(10000, "Unknown exception"),
    VALID_EXCEPTION(10001, "Parameter format validation failed"),
    /** SMS: Redis interval / rate limit */
    SMS_CODE_RATE_TOO_HIGH(10002, "SMS code requests are too frequent, try again later"),
    /** Phone does not match configured region (e.g. NANP vs China) */
    PHONE_FORMAT_INVALID(10003, "Invalid phone number format for the selected region"),
    SMS_VERIFICATION_CODE_REQUIRED(10004, "SMS verification code is required"),
    SMS_CODE_EXPIRED(10005, "Verification code has expired, request a new one"),
    SMS_CODE_INCORRECT(10006, "Verification code is incorrect. Check the code and try again; you do not need to request a new one"),
    VERIFICATION_SERVICE_UNAVAILABLE(10007, "Verification service is temporarily unavailable, please try again later"),
    /** Twilio trial: destination not on verified caller list (HTTP 400 / error 21608) */
    SMS_DESTINATION_NOT_ALLOWED(10011, "This phone number cannot receive SMS yet. On a Twilio trial account, verify the number in Twilio Console or upgrade your account"),
    MOBILE_NUMBER_REQUIRED(10008, "Mobile number is required"),
    REGISTRATION_REQUIRES_MOBILE_AND_SMS(10009, "Registration requires a mobile number and SMS verification"),
    MEMBER_SERVICE_UNAVAILABLE(10010, "Member service is temporarily unavailable, please try again later"),

    PRODUCT_UP_EXCEPTION(11000, "Product listing failed"),
    /** SKU/SPU delete blocked by in-progress orders */
    PRODUCT_DELETE_ACTIVE_ORDERS(11001, "Cannot delete: SKU is referenced by unpaid or in-progress orders"),
    /** SKU delete blocked by locked warehouse stock */
    PRODUCT_DELETE_LOCKED_STOCK(11002, "Cannot delete: SKU has locked warehouse stock"),
    PRODUCT_DELETE_SERVICE_UNAVAILABLE(11003, "Cannot verify orders or stock; delete aborted"),
    PRODUCT_DELETE_REMOTE_CLEANUP_FAILED(11004, "Delete aborted: warehouse or promotion cleanup failed"),

    /** Member / account */
    USERNAME_PASSWORD_REQUIRED(15001, "Username and password are required"),
    USERNAME_LENGTH_INVALID(15002, "Username must be between 4 and 20 characters"),
    USERNAME_CANNOT_BE_ALL_NUMBERS(15003, "Username cannot be all numbers"),
    PASSWORD_TOO_SHORT(15004, "Password must be at least 6 characters"),
    USERNAME_ALREADY_EXISTS(15005, "Username already exists"),
    PHONE_ALREADY_REGISTERED(15006, "Mobile number is already registered"),
    DEFAULT_MEMBER_LEVEL_NOT_CONFIGURED(15007, "Default member level is not configured"),
    INVALID_USERNAME_OR_PASSWORD(15008, "Invalid username or password"),
    ACCOUNT_DISABLED(15009, "Account is disabled"),
    OAUTH_IDENTIFIER_REQUIRED(15010, "OAuth account identifier is missing");

    private final int code;
    private final String message;

    BisCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
