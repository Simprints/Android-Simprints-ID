package com.simprints.id.data

import android.content.Context
import android.os.Build
import com.simprints.id.data.db.analytics.AnalyticsManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.network.ApiManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.tools.extensions.deviceId
import com.simprints.id.tools.extensions.packageVersionName

/**
 * @author: Etienne Thiery (etienne@simprints.com)
 */
class DataManagerImpl(private val context: Context,
                      private val preferencesManager: PreferencesManager,
                      private val localDbManager: LocalDbManager,
                      private val remoteDbManager: RemoteDbManager,
                      private val apiManager: ApiManager,
                      private val analyticsManager: AnalyticsManager)
    : DataManager,
        PreferencesManager by preferencesManager,
        AnalyticsManager by analyticsManager{

    override val androidSdkVersion: Int
        get() = Build.VERSION.SDK_INT

    override val deviceModel: String
        get() = "${Build.MANUFACTURER} ${Build.MODEL}"

    override val deviceId: String
        get() = context.deviceId

    override val appVersionName: String
        get() = context.packageVersionName

    override fun logAlert(alertType: ALERT_TYPE) {
        analyticsManager.logAlert(alertType.name, apiKey, moduleId, userId, deviceId)
    }
}