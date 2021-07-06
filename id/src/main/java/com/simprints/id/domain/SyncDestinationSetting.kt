package com.simprints.id.domain

import androidx.annotation.Keep

/**
 * The value of COSYNCCALLINGAPP here is actualy the string "COMMCARE" on remote config. This is an issue with naming that
 * we did not see we would need to reuse it in future for cosync projects such as ECHIS (which uses the Commcare calling app) and GIZ
 * */
@Keep
enum class SyncDestinationSetting {
    SIMPRINTS,
    COSYNCCALLINGAPP
}
