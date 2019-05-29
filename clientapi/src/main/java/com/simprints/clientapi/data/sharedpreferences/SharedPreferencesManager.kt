package com.simprints.clientapi.data.sharedpreferences


interface SharedPreferencesManager {

    fun stashSessionId(sessionId: String)

    fun peekSessionId(): String

    fun popSessionId(): String

}
