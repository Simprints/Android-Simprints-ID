package com.simprints.id.tools;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.simprints.id.activities.LaunchActivity;

import static android.app.Activity.RESULT_CANCELED;

public class Dialogs {
    
    public static ProgressDialog getUn20Dialog(Context context) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("Re-Connecting...");
        return dialog;
    }

}
