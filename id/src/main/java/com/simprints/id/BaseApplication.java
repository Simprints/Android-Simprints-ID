package com.simprints.id;

import android.app.Application;
import android.content.Context;

import com.simprints.libdata.Data;
import com.simprints.libscanner.Scanner;

public class BaseApplication extends Application {

    private static Context context = null;

    public static Context getContext() { return context; }

    public static void setContext(Context context) {
        BaseApplication.context = context;
    }

    final public static int MISSING_API_KEY = 0;
    final public static int INVALID_API_KEY = 1;
    final public static int VALIDATION_FAILED = 2;
    final public static int BLUETOOTH_NOT_SUPPORTED = 3;
    final public static int BLUETOOTH_NOT_ENABLED = 4;
    final public static int NO_SCANNER_FOUND = 5;
    final public static int MULTIPLE_SCANNERS_FOUND = 6;

    final public static int REGISTER_SUBJECT = 0;
    final public static int IDENTIFY_SUBJECT = 1;
    private static int mode;

    public static int getMode() { return mode; }

    public static void setMode(int mode) { BaseApplication.mode = mode; }

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

    private static Scanner scanner;

    public static Scanner getScanner() {
        if (scanner == null) {

        }
        return BaseApplication.scanner;
    }

    public static void setScanner(Scanner scanner) { BaseApplication.scanner = scanner; }

    private static Data data;

    public static Data getData() {
        if (data == null) {
            data = new Data(context);
        }
        return data;
    }

    public static void setData(Data data) { BaseApplication.data = data; }
}
