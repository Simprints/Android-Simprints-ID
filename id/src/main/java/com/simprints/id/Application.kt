package com.simprints.id

import com.crashlytics.android.answers.Answers
import com.google.firebase.analytics.FirebaseAnalytics
import com.simprints.id.backgroundSync.SyncService
import com.simprints.id.controllers.Setup
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
import com.simprints.id.tools.Analytics
import com.simprints.id.tools.AppState
import android.app.Application as AndroidApplication

class Application: AndroidApplication() {

    // TODO: dependency injection with Dagger 2!

    private val preferencesManager: PreferencesManager by lazy { PreferencesManagerImpl(this) }
    private val localDbManager: LocalDbManager by lazy { RealmDbManager(this) }
    private val remoteDbManager: RemoteDbManager by lazy { FirebaseRtdbManager(this) }
    private val apiManager: ApiManager by lazy { ApiManagerImpl(this) }
    private val firebaseAnalytics: FirebaseAnalytics by lazy {
        val analytics = FirebaseAnalytics.getInstance(this)
        analytics.setAnalyticsCollectionEnabled(true)
        analytics.setMinimumSessionDuration(0)
        analytics
    }
    private val analyticsManager: AnalyticsManager by lazy { FirebaseAnalyticsManager(firebaseAnalytics) }
    val dataManager: DataManager by lazy { DataManagerImpl(this, preferencesManager, localDbManager, remoteDbManager, apiManager, analyticsManager) }

    // TODO: These are all the singletons that are used in Simprints ID right now. This is temporary, until we get rid of all these singletons
    val appState: AppState by lazy { AppState.getInstance(dataManager) }
    val analytics: Analytics by lazy { Analytics.getInstance(this, dataManager, appState) }
    val setup: Setup by lazy { Setup.getInstance(dataManager, appState, analytics) }
    val answers: Answers by lazy { Answers.getInstance() }
    val syncService: SyncService by lazy { SyncService.getInstance() }
}