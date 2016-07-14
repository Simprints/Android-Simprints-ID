package com.simprints.id.model;

import java.util.ArrayList;

public class Finger {

    public static int NOT_REQUIRED = 0;
    public static int NOT_COLLECTED = 1;
    public static int COLLECTING = 2;
    public static int GOOD_SCAN = 3;
    public static int BAD_SCAN = 4;

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
