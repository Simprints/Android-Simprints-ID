package com.simprints.id.activities.checkLogin

import com.simprints.id.model.ALERT_TYPE

interface CheckLoginContract {

    interface View {
        fun openAlertActivityForError(alertType: ALERT_TYPE)
    }
}
