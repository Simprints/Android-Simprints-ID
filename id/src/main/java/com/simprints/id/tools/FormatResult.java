package com.simprints.id.tools;

import android.content.Intent;

import com.simprints.libsimprints.Constants;
import com.simprints.libsimprints.Identification;
import com.simprints.libsimprints.Registration;
import com.simprints.libsimprints.Verification;

import java.util.ArrayList;

/**
 * Created by james on 21/07/2017.
 */

public class FormatResult {
    final static private String guidKey = "guid";
    final static private String confidenceKey = "confidence";
    final static private String tierKey = "tier";

    final static private String ODK_FORMAT_V01 = "ODKv01";  //ToDo Move to libCommon
    final static private String OdkSepV01 = " ";

    private interface attributeGetter {
        // called this apply so it has a similar signature to function in Java 8
        String apply(Identification identification );
    }

    static private String constructSring(ArrayList<Identification> identifications, attributeGetter function){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < identifications.size(); i++) {
            Identification id = identifications.get(i);
            sb.append(function.apply(id));
            if( i < identifications.size() - 1){
                sb.append(OdkSepV01);
            }
        }
        return sb.toString();
    }

    static private boolean isODK( ){
        return ODK_FORMAT_V01.equalsIgnoreCase(AppState.getInstance().getResultFormat());
    }

    static public void put(Intent intent, Registration registration){
        if( isODK() ){
            intent.putExtra(guidKey, registration.getGuid());
        }
        else {
            intent.putExtra(Constants.SIMPRINTS_REGISTRATION, registration);
        }
    }

    static public void put(Intent intent, Verification verification){
        if( isODK() ){
            intent.putExtra(guidKey, verification.getGuid());
            intent.putExtra(confidenceKey, verification.getConfidence());
            intent.putExtra(tierKey, verification.getTier());
        }
        else {
            intent.putExtra(Constants.SIMPRINTS_VERIFICATION, verification);
        }
    }

    static public void put(Intent intent, ArrayList<Identification> identifications){


        intent.putExtra(Constants.SIMPRINTS_SESSION_ID, AppState.getInstance().getSessionId());

        if( isODK()) {
            // a bit inefficient to run through the array x times but there will be <= 20 objects in there
            String guids = constructSring(identifications, new attributeGetter() {
                @Override
                public String apply(Identification identification) {
                    return identification.getGuid();
                }
            });
            String confidences = constructSring(identifications, new attributeGetter() {
                @Override
                public String apply(Identification identification) {
                    return Float.toString(identification.getConfidence());
                }
            });
            String tiers = constructSring(identifications, new attributeGetter() {
                @Override
                public String apply(Identification identification) {
                    return identification.getTier().toString();
                }
            });
            intent.putExtra(guidKey, guids);
            intent.putExtra(confidenceKey, confidences);
            intent.putExtra(tierKey, tiers);
        }
        else {
            intent.putExtra(Constants.SIMPRINTS_IDENTIFICATIONS, identifications);
        }
    }
}
