package com.simprints.id

import com.google.firebase.analytics.FirebaseAnalytics
import com.simprints.id.data.DataManager
import com.simprints.id.data.DataManagerImpl
import com.simprints.id.data.db.analytics.AnalyticsManager
import com.simprints.id.data.db.analytics.FirebaseAnalyticsManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.RealmDbManager
import com.simprints.id.data.db.remote.FirebaseRtdbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.network.ApiManager
import com.simprints.id.data.network.ApiManagerImpl
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.PreferencesManagerImpl
import android.app.Application as AndroidApplication

class Application: AndroidApplication() {

    // TODO: dependency injection with Dagger 2!

    val preferencesManager: PreferencesManager by lazy { PreferencesManagerImpl(this) }
    val localDbManager: LocalDbManager by lazy { RealmDbManager(this) }
    val remoteDbManager: RemoteDbManager by lazy { FirebaseRtdbManager(this) }
    val apiManager: ApiManager by lazy { ApiManagerImpl(this) }
    private val firebaseAnalytics: FirebaseAnalytics by lazy {
        val analytics = FirebaseAnalytics.getInstance(this)
        analytics.setAnalyticsCollectionEnabled(true)
        analytics.setMinimumSessionDuration(0)
        analytics
    }
    val analyticsManager: AnalyticsManager by lazy { FirebaseAnalyticsManager(firebaseAnalytics) }
    val dataManager: DataManager by lazy { DataManagerImpl(this, preferencesManager, localDbManager, remoteDbManager, apiManager, analyticsManager) }

}