package com.simprints.feature.setup.location

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.simprints.feature.setup.LocationStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class LocationStoreWorkerScheduler @Inject constructor(
    @ApplicationContext private val appContext: Context,
) : LocationStore {
    override fun collectLocationInBackground() {
        val request = OneTimeWorkRequest
            .Builder(StoreUserLocationIntoCurrentSessionWorker::class.java)
            .addTag(STORE_USER_LOCATION_WORKER_TAG)
            .build()
        WorkManager.getInstance(appContext).enqueue(request)
    }

    override fun cancelLocationCollection() {
        WorkManager.getInstance(appContext).cancelAllWorkByTag(STORE_USER_LOCATION_WORKER_TAG)
    }

    companion object {
        private const val STORE_USER_LOCATION_WORKER_TAG = "StoreUserLocationWorkerTag"
    }
}
