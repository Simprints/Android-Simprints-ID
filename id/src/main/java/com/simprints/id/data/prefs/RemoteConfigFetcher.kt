package com.simprints.id.data.prefs

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.simprints.id.BuildConfig
import timber.log.Timber

class RemoteConfigFetcher(private val remoteConfig: FirebaseRemoteConfig) {

    init {
        remoteConfig.setConfigSettings(FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(BuildConfig.DEBUG).build())
    }

    fun forceDoFetchInBackgroundAndActivate() =
        doFetchInBackgroundAndActivate(0L)

    /**
     * The default cache time is 12 hours
     */
    fun doFetchInBackgroundAndActivateUsingDefaultCacheTime() =
        doFetchInBackgroundAndActivate()

    private fun doFetchInBackgroundAndActivate(cacheExpirationSeconds: Long? = null) {
        getFetchTask(cacheExpirationSeconds).addOnSuccessListener {
            remoteConfig.activateFetched()
            Timber.d("FirebaseRemoteConfig : Fetched and activated settings")
        }
        Timber.d("FirebaseRemoteConfig : Fetching settings...")
    }

    private fun getFetchTask(cacheExpirationSeconds: Long?) =
        if (cacheExpirationSeconds == null)
            remoteConfig.fetch()
        else
            remoteConfig.fetch(cacheExpirationSeconds)
}
