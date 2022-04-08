package com.simprints.clientapi.data.sharedpreferences

import android.content.Context
import androidx.core.content.edit
import com.simprints.core.domain.modality.Modality
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.domain.CosyncSetting
import com.simprints.id.domain.SyncDestinationSetting


class SharedPreferencesManagerImpl(
    context: Context,
    private val corePreferencesManager: IdPreferencesManager
) : SharedPreferencesManager {

    companion object {
        private const val SHARED_PREF_KEY = "client_api_shared_pref_80094647-bf24-4927-a527-c77b9664a250"

        private const val SESSION_ID = "session_id"
    }

    private val sharedPreferences = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)

    override fun stashSessionId(sessionId: String) =
        sharedPreferences.edit { putString(SESSION_ID, sessionId) }

    override fun peekSessionId() = sharedPreferences.getString(SESSION_ID, "") ?: ""

    override fun popSessionId(): String = peekSessionId().also {
        stashSessionId("")
    }

    override val syncDestinationSettings: List<SyncDestinationSetting>
        get() = corePreferencesManager.syncDestinationSettings

    override val cosyncSyncSettings: List<CosyncSetting>
        get() = corePreferencesManager.cosyncSyncSettings

    override val modalities: List<Modality>
        get() = corePreferencesManager.modalities
}
