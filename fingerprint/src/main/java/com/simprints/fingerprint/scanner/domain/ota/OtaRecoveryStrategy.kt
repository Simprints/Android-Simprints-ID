package com.simprints.fingerprint.scanner.domain.ota

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class OtaRecoveryStrategy : Parcelable {

    sealed class UserActionRequired : OtaRecoveryStrategy() {
        @Parcelize object HardReset : UserActionRequired()
        @Parcelize object SoftReset : UserActionRequired()
    }

    sealed class NoUserActionRequired : OtaRecoveryStrategy() {
        @Parcelize object Un20OnlyReset : NoUserActionRequired()
    }
}
