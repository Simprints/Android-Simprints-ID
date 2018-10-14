package com.simprints.id.services.scheduledSync.peopleUpsync

import androidx.work.*
import com.simprints.id.Application
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.realm.models.rl_Person
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.loginInfo.LoginInfoManager
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.lang.IllegalArgumentException
import javax.inject.Inject

class PatientBatchUploader: Worker() {

    @Inject
    lateinit var loginInfoManager: LoginInfoManager

    @Inject
    lateinit var localDbManager: LocalDbManager

    @Inject
    lateinit var remoteDbManager: RemoteDbManager

    val projectId by lazy {
        inputData.getString(PROJECT_ID_KEY) ?: throw IllegalArgumentException("Project Id required")
    }

    val userId by lazy {
        inputData.getString(USER_ID_KEY) ?: throw IllegalArgumentException("User Id required")

    }

    override fun doWork(): Result {
        Timber.d("PatientBatchUploader - doWork")
        injectDependencies()

        return try {
            uploadNewPeople(PATIENT_UPLOAD_BATCH_SIZE)
            Result.SUCCESS
        } catch (exception: IllegalStateException) {
            Result.FAILURE
        } catch (exception: Exception) {
            Timber.e(exception)
            Result.FAILURE
        }
    }

    private fun injectDependencies() {
        val context = applicationContext
        if (context is Application) {
            context.component.inject(this)
        }
    }

    private fun uploadNewPeople(batchSize: Int) {
        if (projectId != loginInfoManager.signedInProjectId || userId != loginInfoManager.signedInUserId) {
            throw IllegalStateException("Only only people enrolled by the currently signed in user can be up-synced")
        }

        localDbManager.loadPeopleFromLocalRx(userId = userId, toSync = true)
            .map { fb_Person(it) }
            .buffer(batchSize)
            .blockingForEach(uploadBatchToProject(projectId))
    }

    private fun uploadBatchToProject(projectId: String): (List<fb_Person>) -> Unit =
        { people: List<fb_Person> ->
            remoteDbManager.uploadPeople(projectId, people).blockingAwait()
            val updatedPeople = people.map { fbPerson ->
                rl_Person(fbPerson, toSync = false)
            }
            localDbManager.insertOrUpdatePeopleInLocal(updatedPeople).blockingAwait()
        }

    companion object {

        const val PATIENT_UPLOAD_BATCH_SIZE = 80
        const val PROJECT_ID_KEY = "projectId"
        const val USER_ID_KEY = "userId"

        fun schedule(projectId: String, userId: String) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val data = workDataOf(
                PROJECT_ID_KEY to projectId,
                USER_ID_KEY to userId
            )

            val workRequest = OneTimeWorkRequestBuilder<PatientBatchUploader>()
                .setConstraints(constraints)
                .setInputData(data)
                .build()

            WorkManager.getInstance()
                .beginUniqueWork(
                    "$projectId-$userId",
                    ExistingWorkPolicy.KEEP,
                    workRequest
                )
                .enqueue()
        }

    }

}
