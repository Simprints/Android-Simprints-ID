package com.simprints.id.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.simprints.id.tools.SharedPrefHelper;
import com.simprints.libdata.DatabaseContext;
import com.simprints.libdata.DatabaseEventListener;
import com.simprints.libdata.Event;

public class CommCareReceiver extends BroadcastReceiver implements DatabaseEventListener {
    private DatabaseContext data;
    private String caseId;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();

        if (extras == null) {
            return;
        }

        caseId = extras.getString("case_id");
        if (caseId == null || caseId.isEmpty()) {
            return;
        }

        String user = DatabaseContext.signedInUserId();
        if (user == null) {
            return;
        }

        DatabaseContext.initDatabase(context, new SharedPrefHelper(context).getLastUserId());
        data = new DatabaseContext(user, new SharedPrefHelper(context).getLastUserId(), context, this);
    }

    @Override
    public void onDataEvent(Event event) {
        switch (event) {
            case SIGNED_IN:
                data.updateIdentification(caseId);
                break;
        }
    }
}
