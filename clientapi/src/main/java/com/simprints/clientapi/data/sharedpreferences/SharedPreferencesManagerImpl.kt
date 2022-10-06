package com.simprints.clientapi.data.sharedpreferences

import android.content.Context
import androidx.core.content.edit
import javax.inject.Inject

class SharedPreferencesManagerImpl @Inject constructor(
    context: Context
) : SharedPreferencesManager {

    companion object {
        internal const val SHARED_PREF_KEY =
            "client_api_shared_pref_80094647-bf24-4927-a527-c77b9664a250"

        internal const val SESSION_ID = "session_id"
    }

    private val sharedPreferences =
        context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)

    override fun stashSessionId(sessionId: String) =
        sharedPreferences.edit { putString(SESSION_ID, sessionId) }

    override fun peekSessionId() = sharedPreferences.getString(SESSION_ID, "") ?: ""

    override fun popSessionId(): String = peekSessionId().also {
        stashSessionId("")
    }
}
