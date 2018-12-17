package com.simprints.id.services.scheduledSync.peopleUpsync.uploader

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.simprints.id.Application
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.exceptions.safe.sync.TransientSyncFailureException
import timber.log.Timber
import javax.inject.Inject
import androidx.work.Result

// TODO: uncomment userId when multitenancy is properly implemented

class PeopleUpSyncUploaderWorker(context: Context, params: WorkerParameters)
    : Worker(context, params) {

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

    /*val userId by lazy {
        inputData.getString(USER_ID_KEY) ?: throw IllegalArgumentException("User Id required")
    }*/

    override fun doWork(): Result {
        Timber.d("PeopleUpSyncUploaderWorker doWork()")
        injectDependencies()

        val task = PeopleUpSyncUploaderTask(
            loginInfoManager, localDbManager, remoteDbManager,
            projectId, /*userId, */PATIENT_UPLOAD_BATCH_SIZE
        )

        return try {
            task.execute()
            Result.success()
        } catch (exception: TransientSyncFailureException) {
            Timber.e(exception)
            Result.retry()
        } catch (throwable: Throwable) {
            Timber.e(throwable)
            analyticsManager.logThrowable(throwable)
            Result.failure()
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
