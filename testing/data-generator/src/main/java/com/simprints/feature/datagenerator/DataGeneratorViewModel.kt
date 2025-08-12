package com.simprints.feature.datagenerator

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.feature.datagenerator.enrollmentrecords.InsertEnrollmentRecordsUseCase
import com.simprints.feature.datagenerator.events.InsertSessionEventsUseCase
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.logging.Simber
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class DataGeneratorViewModel @Inject constructor(
    private val insertEnrollmentRecords: InsertEnrollmentRecordsUseCase,
    private val insertEvents: InsertSessionEventsUseCase,
    private val authStore: AuthStore,
) : ViewModel() {
    companion object {
        private const val TAG = "DataGeneratorViewModel"

        // Intent Actions
        private const val ACTION_GENERATE_ENROLLMENT_RECORDS = "com.simprints.test.GENERATE_ENROLLMENT_RECORDS"
        private const val ACTION_GENERATE_SESSION_EVENTS = "com.simprints.test.GENERATE_SESSION_EVENTS"

        // Common Extras
        private const val EXTRA_PROJECT_ID = "EXTRA_PROJECT_ID"
        private const val EXTRA_MODULE_ID = "EXTRA_MODULE_ID"
        private const val EXTRA_ATTENDANT_ID = "EXTRA_ATTENDANT_ID"

        // Enrollment Record Extras
        private const val EXTRA_NUM_RECORDS = "EXTRA_NUM_RECORDS"
        private const val EXTRA_TEMPLATES_PER_FORMAT = "EXTRA_TEMPLATES_PER_FORMAT"
        private const val EXTRA_FIRST_SUBJECT_ID = "EXTRA_FIRST_SUBJECT_ID"
        private const val EXTRA_FINGER_ORDER = "EXTRA_FINGER_ORDER"

        // Session Event Extras
        private const val EXTRA_ENROL_COUNT = "EXTRA_ENROL_COUNT"
        private const val EXTRA_IDENTIFY_COUNT = "EXTRA_IDENTIFY_COUNT"
        private const val EXTRA_CONFIRM_IDENTIFY_COUNT = "EXTRA_CONFIRM_IDENTIFY_COUNT"
        private const val EXTRA_ENROL_LAST_COUNT = "EXTRA_ENROL_LAST_COUNT"
        private const val EXTRA_VERIFY_COUNT = "EXTRA_VERIFY_COUNT"
    }

    private val _statusMessage = MutableLiveData<String>()
    val statusMessage: LiveData<String> = _statusMessage

    suspend fun handleIntent(intent: Intent?) {
        Simber.i("Handling intent: ${intent?.action}", tag = TAG)
        if (intent?.action == null) {
            Simber.i("Cannot handle null intent or action.", tag = TAG)
            throw IllegalArgumentException("Intent or action cannot be null")
        }
        if (authStore.signedInProjectId.isEmpty()) {
            Simber.i("No project signed in, cannot handle intent.", tag = TAG)
            throw IllegalStateException("No project signed in")
        }

        when (intent.action) {
            ACTION_GENERATE_ENROLLMENT_RECORDS -> parseAndGenerateEnrollmentRecords(intent)
            ACTION_GENERATE_SESSION_EVENTS -> parseAndGenerateSessionEvents(intent)
            else -> {
                Simber.i("Unknown action received: ${intent.action}", tag = TAG)
                throw IllegalArgumentException("Unknown action: ${intent.action}")
            }
        }
    }

    /**
     * Parses extras for generating enrollment records and calls the data creation function.
     */
    private suspend fun parseAndGenerateEnrollmentRecords(intent: Intent) {
        val projectId = intent.getStringExtra(EXTRA_PROJECT_ID)
        val moduleId = intent.getStringExtra(EXTRA_MODULE_ID)
        val attendantId = intent.getStringExtra(EXTRA_ATTENDANT_ID)
        val numRecords = intent.getIntExtra(EXTRA_NUM_RECORDS, 0)
        val templatesPerFormat =
            intent.getBundleExtra(EXTRA_TEMPLATES_PER_FORMAT) ?: extractBundleFromFlatExtras(intent, EXTRA_TEMPLATES_PER_FORMAT)
        val firstSubjectId = intent.getStringExtra(EXTRA_FIRST_SUBJECT_ID)
        val fingerOrder = intent.getBundleExtra(EXTRA_FINGER_ORDER) ?: extractBundleFromFlatExtras(intent, EXTRA_FINGER_ORDER)

        if (projectId == null || moduleId == null || attendantId == null || numRecords <= 0) {
            Simber.i("Missing required extras for generating enrollment records.", tag = TAG)
            throw IllegalArgumentException(
                "Required extras missing: projectId, moduleId, attendantId, or numRecords",
            )
        }

        Simber.i("Calling generateEnrollmentRecordsInDb with $numRecords records.", tag = TAG)
        insertEnrollmentRecords(
            projectId = projectId,
            moduleId = moduleId,
            attendantId = attendantId,
            numRecords = numRecords,
            templatesPerFormat = templatesPerFormat,
            firstSubjectId = firstSubjectId ?: "",
            fingerOrder = fingerOrder,
        ).collect {
            _statusMessage.postValue(it)
        }
    }

    /**
     * Parses extras for generating session events and calls the data creation function.
     */
    private suspend fun parseAndGenerateSessionEvents(intent: Intent) {
        val projectId = intent.getStringExtra(EXTRA_PROJECT_ID)
        val moduleId = intent.getStringExtra(EXTRA_MODULE_ID)
        val attendantId = intent.getStringExtra(EXTRA_ATTENDANT_ID)
        val enrolCount = intent.getIntExtra(EXTRA_ENROL_COUNT, 0)
        val identifyCount = intent.getIntExtra(EXTRA_IDENTIFY_COUNT, 0)
        val confirmIdentifyCount = intent.getIntExtra(EXTRA_CONFIRM_IDENTIFY_COUNT, 0)
        val enrolLastCount = intent.getIntExtra(EXTRA_ENROL_LAST_COUNT, 0)
        val verifyCount = intent.getIntExtra(EXTRA_VERIFY_COUNT, 0)

        if (projectId == null || moduleId == null || attendantId == null) {
            Simber.i("Missing required extras for generating session events.", tag = TAG)
            throw IllegalArgumentException(
                "Required extras missing: projectId, moduleId, or attendantId",
            )
        }

        insertEvents(
            projectId = projectId,
            moduleId = moduleId,
            attendantId = attendantId,
            enrolCount = enrolCount,
            identifyCount = identifyCount,
            confirmIdentifyCount = confirmIdentifyCount,
            enrolLastCount = enrolLastCount,
            verifyCount = verifyCount,
        ).collect {
            _statusMessage.postValue(it)
        }
    }

    private fun extractBundleFromFlatExtras(
        intent: Intent,
        prefix: String,
    ): Bundle {
        val bundle = Bundle()
        intent.extras?.keySet()?.forEach { key ->
            if (key.startsWith("$prefix.")) {
                val subKey = key.removePrefix("$prefix.")
                // Only expecting Int or String values
                when (val value = intent.extras?.get(key)) {
                    is Int -> bundle.putInt(subKey, value)
                    is String -> bundle.putString(subKey, value)
                }
            }
        }
        return bundle
    }
}
