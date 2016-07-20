package com.simprints.id.model;

import java.util.ArrayList;

public class Finger {

    public enum FingerStatus {
        DO_NOT_COLLECT,
        OPTIONAL,
        REQUIRED,
        ADDED,
        NOT_COLLECTED,
        COLLECTING,
        GOOD_SCAN,
        BAD_SCAN,
        NO_SCAN
    };

    public static ArrayList<Finger> fingers = new ArrayList<Finger>();

    private int fingerPos;
    private int fingerNo;
    private FingerStatus fingerStatus;
    private byte[] fingerTemplate;

    public Finger(int fingerPos, int fingerNo, FingerStatus fingerStatus) {
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

    public FingerStatus getFingerStatus() {
        return fingerStatus;
    }

    public void setFingerStatus(FingerStatus fingerStatus) {
        this.fingerStatus = fingerStatus;
    }

    public byte[] getFingerTemplate() {
        return fingerTemplate;
    }

    public void setFingerTemplate(byte[] fingerTemplate) {
        this.fingerTemplate = fingerTemplate;
    }
}
