package com.simprints.clientapi.integration

import android.app.Application
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.session.SessionEvents
import com.simprints.moduleapi.app.requests.IAppEnrollRequest
import com.simprints.moduleapi.app.requests.IAppIdentifyRequest
import com.simprints.moduleapi.app.requests.IAppVerifyRequest
import com.simprints.moduleapi.app.requests.confirmations.IAppIdentifyConfirmation
import com.simprints.moduleapi.app.responses.*
import com.simprints.testtools.common.syntax.*
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.android.parcel.Parcelize

internal fun buildDummySessionEventsManagerMock(): SessionEventsManager =
    mock<SessionEventsManager>().apply {
        val sessionMock = mock<SessionEvents>().apply {
            whenever(this) { id } thenReturn ""
        }

        whenever(this) { createSession(anyNotNull()) } thenReturn Single.just(sessionMock)
        whenever(this) { addEvent(anyNotNull()) } thenReturn Completable.complete()
    }


internal const val skipCheckValueForFlowCompleted = false
internal const val appEnrolAction = "com.simprints.clientapp.REGISTER"
internal const val appIdentifyAction = "com.simprints.clientapp.IDENTIFY"
internal const val appVerifyAction = "com.simprints.clientapp.VERIFY"
internal const val appConfirmIdentityAction = "com.simprints.clientapp.CONFIRM_IDENTITY"

internal val projectIdField = "projectId" to "some_project"
internal val userIdField = "userId" to "some_user_id"
internal val moduleIdField = "moduleId" to "some_module_id"
internal val metadataField = "metadata" to "some_metadata"
internal val verifyGuidField = "verifyGuid" to "8b3f577c-b6c7-4677-9af2-b08cd7f71b79"
internal val sessionIdField = "sessionId" to "some_sessionid"
internal val selectedGuidField = "selectedGuid" to "8b3f577c-b6c7-4677-9af2-b08cd7f71b79"

internal val extraField = "extra" to "some_extra"

internal val packageName = ApplicationProvider.getApplicationContext<Application>().packageName


internal val baseIntentRequest = Intent().apply {
    putExtra(projectIdField.key(), projectIdField.value())
    putExtra(userIdField.key(), userIdField.value())
    putExtra(moduleIdField.key(), moduleIdField.value())
    putExtra(metadataField.key(), metadataField.value())
}

internal val invalidIntentRequest = Intent().apply {
    putExtra(projectIdField.key() + "_wrong",  projectIdField.value())
    putExtra(userIdField.key(),  userIdField.value())
    putExtra(moduleIdField.key(),  moduleIdField.value())
}

internal val suspiciousIntentRequest =  baseIntentRequest.apply {
    putExtra( extraField.key(),  extraField.value())
}

@Parcelize
internal data class AppEnrolResponse(
    override val guid: String,
    override val type: IAppResponseType = IAppResponseType.ENROL
) : IAppEnrolResponse

@Parcelize
internal data class AppVerifyResponse(
    override val matchResult: AppMatchResult,
    override val type: IAppResponseType = IAppResponseType.VERIFY
) : IAppVerifyResponse

@Parcelize
internal data class AppMatchResult(
    override val guid: String,
    override val confidence: Int,
    override val tier: IAppResponseTier
) : IAppMatchResult

@Parcelize
internal data class AppRefusalResponse(
    override val reason: String,
    override val extra: String,
    override val type: IAppResponseType = IAppResponseType.REFUSAL
) : IAppRefusalFormResponse

@Parcelize
internal data class AppIdentifyResponse(
    override val identifications: List<IAppMatchResult>,
    override val sessionId: String,
    override val type: IAppResponseType = IAppResponseType.IDENTIFY
) : IAppIdentifyResponse


@Parcelize
internal data class AppErrorResponse(
    override val reason: IAppErrorReason,
    override val type: IAppResponseType = IAppResponseType.ERROR
) : IAppErrorResponse

@Parcelize
internal data class AppEnrollRequest(
    override val projectId: String,
    override val userId: String,
    override val moduleId: String,
    override val metadata: String
) : IAppEnrollRequest

@Parcelize
internal data class AppIdentifyRequest(
    override val projectId: String,
    override val userId: String,
    override val moduleId: String,
    override val metadata: String
) : IAppIdentifyRequest

@Parcelize
internal data class AppVerifyRequest(
    override val projectId: String,
    override val userId: String,
    override val moduleId: String,
    override val metadata: String,
    override val verifyGuid: String
) : IAppVerifyRequest

@Parcelize
internal data class AppIdentifyConfirmationRequest(
    override val projectId: String,
    override val sessionId: String,
    override val selectedGuid: String
) : IAppIdentifyConfirmation
