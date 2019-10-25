package com.simprints.fingerprint.integration

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.simprints.fingerprint.controllers.core.flow.Action
import com.simprints.id.Application
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessorImpl
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import com.simprints.moduleapi.fingerprint.requests.*
import kotlinx.android.parcel.Parcelize

fun createFingerprintCaptureRequestIntent() = Intent()
    .setClassName(ApplicationProvider.getApplicationContext<Application>().packageName,
        FingerprintStepProcessorImpl.ACTIVITY_CLASS_NAME)
    .putExtra(IFingerprintRequest.BUNDLE_KEY,
        TestFingerprintCaptureRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID,
            DEFAULT_MODULE_ID, DEFAULT_META_DATA, DEFAULT_LANGUAGE, DEFAULT_FINGER_STATUS,
            DEFAULT_LOGO_EXISTS, DEFAULT_PROGRAM_NAME, DEFAULT_ORGANISATION_NAME, DEFAULT_ACTIVITY_TITLE))

fun createFingerprintRequestIntent(action: Action): Intent = Intent()
    .setClassName(ApplicationProvider.getApplicationContext<Application>().packageName,
        FingerprintStepProcessorImpl.ACTIVITY_CLASS_NAME)
    .putExtra(IFingerprintRequest.BUNDLE_KEY, when (action) {
        Action.IDENTIFY -> TestFingerprintIdentifyRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID,
            DEFAULT_MODULE_ID, DEFAULT_META_DATA, DEFAULT_LANGUAGE, DEFAULT_FINGER_STATUS,
            DEFAULT_LOGO_EXISTS, DEFAULT_ORGANISATION_NAME, DEFAULT_PROGRAM_NAME,
            DEFAULT_MATCH_GROUP, DEFAULT_NUMBER_OF_ID_RETURNS)
        Action.VERIFY -> TestFingerprintVerifyRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID,
            DEFAULT_MODULE_ID, DEFAULT_META_DATA, DEFAULT_LANGUAGE, DEFAULT_FINGER_STATUS,
            DEFAULT_LOGO_EXISTS, DEFAULT_PROGRAM_NAME, DEFAULT_ORGANISATION_NAME,
            DEFAULT_VERIFY_GUID)
    })

const val DEFAULT_PROJECT_ID = "some_project_id"
const val DEFAULT_USER_ID = "some_user_id"
const val DEFAULT_MODULE_ID = "some_module_id"
const val DEFAULT_META_DATA = ""
const val DEFAULT_LANGUAGE = "en"
const val DEFAULT_LOGO_EXISTS = true
const val DEFAULT_PROGRAM_NAME = "This program"
const val DEFAULT_ORGANISATION_NAME = "This organisation"
const val DEFAULT_ACTIVITY_TITLE = "Enrolment"
const val DEFAULT_VERIFY_GUID = "verify_guid"
const val DEFAULT_NUMBER_OF_ID_RETURNS = 10
val DEFAULT_MATCH_GROUP = IMatchGroup.GLOBAL
val DEFAULT_FINGER_STATUS = mapOf(
    IFingerIdentifier.RIGHT_THUMB to false,
    IFingerIdentifier.RIGHT_INDEX_FINGER to false,
    IFingerIdentifier.RIGHT_3RD_FINGER to false,
    IFingerIdentifier.RIGHT_4TH_FINGER to false,
    IFingerIdentifier.RIGHT_5TH_FINGER to false,
    IFingerIdentifier.LEFT_THUMB to true,
    IFingerIdentifier.LEFT_INDEX_FINGER to true,
    IFingerIdentifier.LEFT_3RD_FINGER to false,
    IFingerIdentifier.LEFT_4TH_FINGER to false,
    IFingerIdentifier.LEFT_5TH_FINGER to false
)

@Parcelize
data class TestFingerprintCaptureRequest(override val projectId: String,
                                         override val userId: String,
                                         override val moduleId: String,
                                         override val metadata: String,
                                         override val language: String,
                                         override val fingerStatus: Map<IFingerIdentifier, Boolean>,
                                         override val logoExists: Boolean,
                                         override val programName: String,
                                         override val organizationName: String) : IFingerprintCaptureRequest

@Parcelize
data class TestFingerprintIdentifyRequest(override val projectId: String,
                                          override val userId: String,
                                          override val moduleId: String,
                                          override val metadata: String,
                                          override val language: String,
                                          override val fingerStatus: Map<IFingerIdentifier, Boolean>,
                                          override val logoExists: Boolean,
                                          override val programName: String,
                                          override val organizationName: String,
                                          override val matchGroup: IMatchGroup,
                                          override val returnIdCount: Int) : IFingerprintIdentifyRequest

@Parcelize
data class TestFingerprintVerifyRequest(override val projectId: String,
                                        override val userId: String,
                                        override val moduleId: String,
                                        override val metadata: String,
                                        override val language: String,
                                        override val fingerStatus: Map<IFingerIdentifier, Boolean>,
                                        override val logoExists: Boolean,
                                        override val programName: String,
                                        override val organizationName: String,
                                        override val verifyGuid: String) : IFingerprintVerifyRequest
