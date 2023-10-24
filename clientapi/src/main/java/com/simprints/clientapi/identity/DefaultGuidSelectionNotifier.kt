package com.simprints.clientapi.identity

import android.content.Context
import android.widget.Toast
import com.simprints.infra.resources.R
import javax.inject.Inject

class DefaultGuidSelectionNotifier @Inject constructor(context: Context) :
    GuidSelectionNotifier(context) {

    override fun showMessage() {
        Toast.makeText(context, R.string.guid_selection_data_sent, Toast.LENGTH_LONG).show()
    }

}
