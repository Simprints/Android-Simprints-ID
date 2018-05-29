package com.simprints.id.data

import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.secure.SecureDataManager

interface DataManager {

    val preferences: PreferencesManager
    val loginInfo: LoginInfoManager
    val secure: SecureDataManager
    val analytics: AnalyticsManager
    val db: DbManager

    fun saveSession()
}
