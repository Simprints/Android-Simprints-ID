package com.simprints.id.activities.checkLogin

import com.simprints.id.domain.ALERT_TYPE

interface CheckLoginContract {

    interface View {
        fun openAlertActivityForError(alertType: ALERT_TYPE)
    }
}
