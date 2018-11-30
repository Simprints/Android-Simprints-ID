package com.simprints.id.services.scheduledSync.peopleUpsync.uploader

import androidx.work.Worker
import com.simprints.id.Application
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.exceptions.safe.sync.TransientSyncFailureException
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.room.NewSyncStatusDatabase
import timber.log.Timber
import javax.inject.Inject

// TODO: uncomment userId when multitenancy is properly implemented

class PeopleUpSyncUploaderWorker : Worker() {

    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var localDbManager: LocalDbManager
    @Inject lateinit var remoteDbManager: RemoteDbManager
    @Inject lateinit var analyticsManager: AnalyticsManager
    @Inject lateinit var newSyncStatusDatabase: NewSyncStatusDatabase

    val projectId by lazy {
        inputData.getString(PROJECT_ID_KEY) ?: throw IllegalArgumentException("Project Id required")
    }

    /*val userId by lazy {
        inputData.getString(USER_ID_KEY) ?: throw IllegalArgumentException("User Id required")
    }*/

    override fun doWork(): Result {
        Timber.d("PeopleUpSyncUploaderWorker doWork()")
        injectDependencies()

        val task = PeopleUpSyncUploaderTask(
            loginInfoManager, localDbManager, remoteDbManager,
            projectId, /*userId, */PATIENT_UPLOAD_BATCH_SIZE,
            newSyncStatusDatabase.upSyncStatusModel
        )

        return try {
            task.execute()
            Result.SUCCESS
        } catch (exception: TransientSyncFailureException) {
            Timber.e(exception)
            Result.RETRY
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
