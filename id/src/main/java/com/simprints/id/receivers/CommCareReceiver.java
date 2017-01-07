package com.simprints.id.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.simprints.libdata.DatabaseContext;

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

        DatabaseContext.updateIdentification(caseId);
    }
}
