package com.simprints.id.model;

import java.util.ArrayList;

public class Finger {

    //TODO: turn these into enums
    public static int DO_NOT_COLLECT = 0;
    public static int OPTIONAL = 1;
    public static int REQUIRED = 2;
    public static int ADDED = 3;
    public static int NOT_COLLECTED = 4;
    public static int COLLECTING = 5;
    public static int GOOD_SCAN = 6;
    public static int BAD_SCAN = 7;
    public static int NO_SCAN = 8;

    public static ArrayList<Finger> fingers = new ArrayList<Finger>();

    private int fingerPos;
    private int fingerNo;
    private int fingerStatus;
    private byte[] fingerTemplate;

    public Finger(int fingerPos, int fingerNo, int fingerStatus) {
        this.fingerNo = fingerNo;
        this.fingerStatus = fingerStatus;
        fingers.set(fingerPos, this);
    }

    public int getFingerPos() {
        return fingerPos;
    }

    public void setFingerPos(int fingerPos) {
        this.fingerPos = fingerPos;
    }

    public int getFingerNo() {
        return fingerNo;
    }

    public void setFingerNo(int fingerNo) {
        this.fingerNo = fingerNo;
    }

    public int getFingerStatus() {
        return fingerStatus;
    }

    public void setFingerStatus(int fingerStatus) {
        this.fingerStatus = fingerStatus;
    }

    public byte[] getFingerTemplate() {
        return fingerTemplate;
    }

    public void setFingerTemplate(byte[] fingerTemplate) {
        this.fingerTemplate = fingerTemplate;
    }
}
