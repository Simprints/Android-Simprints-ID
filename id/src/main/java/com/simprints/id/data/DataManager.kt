package com.simprints.id.data

import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.loginInfo.LoginInfoManager

interface DataManager : PreferencesManager {

    val loginInfo: LoginInfoManager
    val analytics: AnalyticsManager
    val db: DbManager

    // Data transfer


    fun saveSession()
}
