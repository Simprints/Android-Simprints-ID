package com.simprints.id.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.simprints.libdata.DatabaseContext;
import com.simprints.libdata.models.M_IdEvent;

public class CommCareReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();

        if (extras == null) {
            return;
        }

        String caseId = extras.getString("case_id");

        if (caseId == null || caseId.isEmpty()) {
            return;
        }

        DatabaseContext.initActiveAndroid(context);
        M_IdEvent idEvent = M_IdEvent.getLatest();

        if (idEvent == null || idEvent.hasSelectedMatchGuid()) {
            return;
        }

        idEvent.setSelectedMatchGuid(caseId);
        idEvent.save();
    }
}
