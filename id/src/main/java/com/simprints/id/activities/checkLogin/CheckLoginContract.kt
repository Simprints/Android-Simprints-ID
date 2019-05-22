package com.simprints.id.activities.checkLogin

import com.simprints.id.domain.alert.NewAlert

interface CheckLoginContract {

    interface View {
        fun openAlertActivityForError(alert: NewAlert)
    }
}
