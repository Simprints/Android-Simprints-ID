package com.simprints.clientapi.data.sharedpreferences

import com.simprints.id.domain.SyncDestinationSetting


interface SharedPreferencesManager {
    val syncDestinationSetting: List<SyncDestinationSetting>

    fun stashSessionId(sessionId: String)

    fun peekSessionId(): String

    fun popSessionId(): String

}
