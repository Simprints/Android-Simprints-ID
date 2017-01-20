package com.simprints.id.tools;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.simprints.id.activities.LaunchActivity;

import static android.app.Activity.RESULT_CANCELED;

public class Dialogs {
    public static ProgressDialog getRestartDialog(final LaunchActivity launchActivity) {
        ProgressDialog dialog = new ProgressDialog(launchActivity);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("Please restart Simprints ID");
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Restart",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        launchActivity.finishWith(RESULT_CANCELED, null);
                    }
                });
        return dialog;
    }

    public static ProgressDialog getUn20Dialog(Context context) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("Re-Connecting...");
        return dialog;
    }

}
