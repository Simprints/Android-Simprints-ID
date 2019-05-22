package com.simprints.id.activities.checkLogin

import com.simprints.id.domain.alert.AlertType

interface CheckLoginContract {

    interface View {
        fun openAlertActivityForError(alertType: AlertType)
    }
}
