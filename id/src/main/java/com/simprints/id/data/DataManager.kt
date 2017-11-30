package com.simprints.id.data

import com.simprints.id.data.db.analytics.AnalyticsManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.network.ApiManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.model.ALERT_TYPE

/**
 * @author: Etienne Thiery (etienne@simprints.com)
 */
interface DataManager : PreferencesManager, LocalDbManager, RemoteDbManager, ApiManager, AnalyticsManager {

    val androidSdkVersion: Int
    val deviceModel: String
    val deviceId: String
    val appVersionName: String

    fun logAlert(alertType: ALERT_TYPE)

}
