package com.simprints.id.data

import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.loginInfo.LoginInfoManager

interface DataManager {

    val preferences: PreferencesManager
    val loginInfo: LoginInfoManager
    val analytics: AnalyticsManager
    val db: DbManager

    fun saveSession()
}
