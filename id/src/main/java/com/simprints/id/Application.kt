package com.simprints.id

import android.content.SharedPreferences
import com.crashlytics.android.Crashlytics
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
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
import com.simprints.id.data.model.CalloutType
import com.simprints.id.data.network.ApiManager
import com.simprints.id.data.network.ApiManagerImpl
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.PreferencesManagerImpl
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferencesImpl
import com.simprints.id.tools.AppState
import com.simprints.id.tools.serializers.BooleanSerializer
import com.simprints.id.tools.serializers.EnumSerializer
import com.simprints.id.tools.serializers.MapSerializer
import com.simprints.id.tools.serializers.Serializer
import com.simprints.libdata.tools.Constants
import com.simprints.libsimprints.FingerIdentifier
import io.fabric.sdk.android.Fabric
import timber.log.Timber
import android.app.Application as AndroidApplication



class Application: AndroidApplication() {

    // TODO: dependency injection with Dagger 2!

    private val gson: Gson by lazy { Gson() }
    private val booleanSerializer: Serializer<Boolean> by lazy { BooleanSerializer() }
    private val fingerIdentifierSerializer: Serializer<FingerIdentifier>
            by lazy { EnumSerializer(FingerIdentifier::class.java) }
    private val calloutTypeSerializer: Serializer<CalloutType>
            by lazy { EnumSerializer(CalloutType::class.java) }
    private val groupSerializer: Serializer<Constants.GROUP>
            by lazy { EnumSerializer(Constants.GROUP::class.java) }
    private val fingerIdentifierToBooleanSerializer: Serializer<Map<FingerIdentifier, Boolean>>
            by lazy { MapSerializer(fingerIdentifierSerializer, booleanSerializer, gson) }

    private val basePrefs: SharedPreferences
            by lazy { this.getSharedPreferences(PreferencesManagerImpl.PREF_FILE_NAME, PreferencesManagerImpl.PREF_MODE)}
    private val prefs: ImprovedSharedPreferences
            by lazy { ImprovedSharedPreferencesImpl(basePrefs) }
    private val preferencesManager: PreferencesManager
            by lazy { PreferencesManagerImpl(prefs, fingerIdentifierToBooleanSerializer,
                    calloutTypeSerializer, groupSerializer) }
    private val localDbManager: LocalDbManager
            by lazy { RealmDbManager() }
    private val remoteDbManager: RemoteDbManager by lazy { FirebaseRtdbManager() }
    private val apiManager: ApiManager by lazy { ApiManagerImpl() }
    private val fabric: Fabric by lazy {
        Fabric.Builder(this).kits(Crashlytics()).debuggable(BuildConfig.DEBUG).build()
    }
    private val firebaseAnalytics: FirebaseAnalytics by lazy {
         FirebaseAnalytics.getInstance(this).apply {
             setAnalyticsCollectionEnabled(true)
             setMinimumSessionDuration(0)
         }
    }
    private val analyticsManager: AnalyticsManager by lazy {
        FirebaseAnalyticsManager(firebaseAnalytics)
    }
    val dataManager: DataManager by lazy { DataManagerImpl(this, preferencesManager, localDbManager, remoteDbManager, apiManager, analyticsManager) }

    // TODO: These are all the singletons that are used in Simprints ID right now. This is temporary, until we get rid of all these singletons
    val appState: AppState by lazy { AppState.getInstance() }
    val setup: Setup by lazy { Setup.getInstance(dataManager, appState) }
    val syncService: SyncService by lazy { SyncService.getInstance(dataManager) }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Fabric.with(fabric)
    }
}