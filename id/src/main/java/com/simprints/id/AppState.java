package com.simprints.id;


import com.simprints.id.activities.MODE;
import com.simprints.libdata.Data;
import com.simprints.libscanner.Scanner;

import java.math.BigInteger;
import java.security.SecureRandom;


public class AppState {

    private final static class RandomCodeGenerator {
        private final static SecureRandom random = new SecureRandom();

        public static String nextRandomCode() {
            return new BigInteger(130, random).toString(32);
        }
    }

    public final static String randomCodeKey = "randomCode";

    private String randomCode;

    private MODE mode;
    private String apiKey;
    private boolean apiKeyIsValid;
    private String deviceId;
    private String userId;
    private String callingPackage;
    private String guid;
    private Scanner scanner;
    private Data data;

    private static AppState singleton;

    public static AppState getInstance(){
        if (singleton == null) {
            singleton = new AppState();
        }
        return singleton;
    }

    protected AppState() {
        randomCode = "";
        mode = MODE.REGISTER_SUBJECT;
        apiKey = null;
        apiKeyIsValid = false;
        deviceId = null;
        userId = null;
        callingPackage = null;
        guid = null;
        scanner = null;
        data = null;
    }

    public boolean checkRandomCode(String code) {
        return code != null && randomCode.equals(code);
    }

    public String setRandomCode() {
        randomCode = RandomCodeGenerator.nextRandomCode();
        return randomCode;
    }

    public MODE getMode() { return mode; }

    public void setMode(MODE mode) { this.mode = mode; }

    public String getApiKey() { return apiKey; }

    public void setApiKey(String apiKey) {this.apiKey = apiKey; }

    public void validApiKey() {
        apiKeyIsValid = true;
    }

    public void invalidApiKey() {
        apiKeyIsValid = false;
    }

    public boolean isApiKeyValid() {
        return apiKeyIsValid;
    }

    public String getDeviceId() { return deviceId; }

    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getUserId() { return userId; }

    public void setUserId(String userId) { this.userId = userId; }

    public String getCallingPackage() { return callingPackage; }

    public void setCallingPackage(String callingPackage) { this.callingPackage = callingPackage; }

    public String getGuid() { return guid; }

    public void setGuid(String guid) { this.guid = guid; }

    public Scanner getScanner() { return scanner; }

    public void setScanner(Scanner scanner) { this.scanner = scanner; }

    public Data getData() { return data; }

    public void setData(Data data) { this.data = data; }
}
