package com.simprints.id.activities.checkLogin

import com.simprints.id.domain.alert.Alert

interface CheckLoginContract {

    interface View {
        fun openAlertActivityForError(alert: Alert)
    }
}
