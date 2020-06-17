package com.simprints.clientapi.identity

import android.content.Context
import android.widget.Toast
import com.simprints.clientapi.R

class CommCareGuidSelectionNotifier(context: Context) : GuidSelectionNotifier(context) {

    override fun showMessage() {
        Toast.makeText(context, R.string.guid_selection_data_sent, Toast.LENGTH_LONG).show()
    }

}
