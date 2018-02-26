package com.simprints.id.activities.checkLogin

import com.simprints.id.model.ALERT_TYPE

interface CheckLoginContract {

    interface View {
        fun launchAlertForError(alertType: ALERT_TYPE)
    }
}
