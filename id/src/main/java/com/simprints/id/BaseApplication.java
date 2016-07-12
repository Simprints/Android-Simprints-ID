package com.simprints.id;

import android.app.Application;

public class BaseApplication extends Application {

    final public static int MISSING_API_KEY = 0;
    final public static int INVALID_API_KEY = 1;

    final public static int REGISTER_SUBJECT = 0;
    final public static int IDENTIFY_SUBJECT = 1;
    private static int operation;

    public static int getOperation() { return operation; }

    public static void setOperation(int operation) { BaseApplication.operation = operation; }

    private static String apiKey = null;

    public static String getApiKey() { return apiKey; }

    public static void setApiKey(String apiKey) { BaseApplication.apiKey = apiKey; }

    private static String deviceId = null;

    public static String getDeviceId() { return deviceId; }

    public static void setDeviceId(String deviceId) { BaseApplication.deviceId = deviceId; }

    private static String userId = null;

    public static String getUserId() { return userId; }

    public static void setUserId(String userId) { BaseApplication.userId = userId; }

    private static String callingPackage = null;

    public static String getCallingPackage() { return callingPackage; }

    public static void setCallingPackage(String callingPackage) { BaseApplication.callingPackage = callingPackage; }

    private static String guid = null;

    public static String getGuid() { return guid; }

    public static void setGuid(String guid) { BaseApplication.guid = guid; }
}
