package com.simprints.id.libdata.tools;

import android.net.Uri;

import com.simprints.libsimprints.FingerIdentifier;

public class Constants {

    public final static String CC_PERMISSION = "org.commcare.dalvik.provider.cases.read";

    public final static String CC_AUTHORITY = "org.commcare.dalvik.case";

    public final static Uri CC_CASES_URI = Uri.parse("content://org.commcare.dalvik.case/casedb/case");
    public final static String CC_CASE_ID_COL = "case_id";
    public final static String CC_OWNER_ID_COL = "owner_id";

    public final static String[] CC_TEMPLATE_SELECTION_ARGS;

    static {
        FingerIdentifier[] fIds = FingerIdentifier.values();
        CC_TEMPLATE_SELECTION_ARGS = new String[fIds.length];
        for (int i = 0; i < fIds.length; i++) {
            CC_TEMPLATE_SELECTION_ARGS[i] = fIds[i].toString();
        }
    }

    public final static String CC_DATA_URI = "content://org.commcare.dalvik.case/casedb/data/%s";
    public final static String CC_VALUE_COL = "value";
    public final static String CC_DATUM_ID_COL = "datum_id";

    public final static String GLOBAL_ID = "GLOBAL-2b14bf72-b68a-4c24-acaf-66d5e1fcc4bc";

    public enum GROUP {
        GLOBAL,
        USER,
        MODULE
    }
}
