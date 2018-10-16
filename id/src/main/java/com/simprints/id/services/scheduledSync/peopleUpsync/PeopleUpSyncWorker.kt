package com.simprints.id.services.scheduledSync.peopleUpsync

import androidx.work.Worker
import com.simprints.id.Application
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import timber.log.Timber
import javax.inject.Inject

class PeopleUpSyncWorker: Worker() {

    @Inject
    lateinit var loginInfoManager: LoginInfoManager

    @Inject
    lateinit var localDbManager: LocalDbManager

    @Inject
    lateinit var remoteDbManager: RemoteDbManager

    @Inject
    lateinit var analyticsManager: AnalyticsManager

    val projectId by lazy {
        inputData.getString(PROJECT_ID_KEY) ?: throw IllegalArgumentException("Project Id required")
    }

    val userId by lazy {
        inputData.getString(USER_ID_KEY) ?: throw IllegalArgumentException("User Id required")

    }

    override fun doWork(): Result {
        Timber.d("Reporting for duty!")
        injectDependencies()

        val task = PeopleUpSyncTask(
            loginInfoManager, localDbManager, remoteDbManager,
            projectId, userId, PATIENT_UPLOAD_BATCH_SIZE
        )

        return try {
            task.execute()
            Result.SUCCESS
        } catch (throwable: Throwable) {
            Timber.e(throwable)
            analyticsManager.logThrowable(throwable)
            Result.FAILURE
        }
    }

    private fun injectDependencies() {
        val context = applicationContext
        if (context is Application) {
            context.component.inject(this)
        }
    }

    companion object {

        const val PATIENT_UPLOAD_BATCH_SIZE = 80
        const val PROJECT_ID_KEY = "projectId"
        const val USER_ID_KEY = "userId"

    }

}
