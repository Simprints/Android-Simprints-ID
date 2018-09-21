package com.simprints.libscanner.enums;

import android.util.SparseArray;

/**
 * Secugen SDK error codes
 */
public enum SDK_ERROR {

    UNKNOWN(-1, "Unknown SGFDX error code"), // catch codes that we don't know about
    NONE(0, "SGFDX_ERROR_NONE"),
    INVALID_PARAM( 3, "SGFDX_ERROR_INVALID_PARAM"),
    LINE_DROPPED( 53, "SGFDX_ERROR_LINE_DROPPED"),
    WRONG_IMAGE( 57, "SGFDX_ERROR_WRONG_IMAGE"),
    FEAT_NUMBER(101, "SGFDX_ERROR_FEAT_NUMBER"),
    INVALID_TEMPLATE_TYPE(102, "SGFDX_ERROR_INVALID_TEMPLATE_TYPE"),
    INVALID_TEMPLATE1(103, "SGFDX_ERROR_INVALID_TEMPLATE1"),
    INVALID_TEMPLATE2(104, "SGFDX_ERROR_INVALID_TEMPLATE2"),
    EXTRACT_FAIL(105, "SGFDX_ERROR_EXTRACT_FAIL"),
    MATCH_FAIL(106, "SGFDX_ERROR_MATCH_FAIL");

    static final SparseArray<SDK_ERROR> allValues;

    static {
        allValues = new SparseArray<>();
        for (SDK_ERROR value : SDK_ERROR.values())
            allValues.append(value.getId(), value);
    }

    public static SDK_ERROR fromId(int id) {
        return allValues.get(id, UNKNOWN);
    }


    private int id;
    private String details;

    SDK_ERROR(int id, String details){
        this.id = id;
        this.details = details;
    }

    public String getDetails() {
        return details;
    }

    private int getId() {
        return id;
    }
}