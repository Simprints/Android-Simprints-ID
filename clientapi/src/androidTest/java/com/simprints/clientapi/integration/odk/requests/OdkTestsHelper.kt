package com.simprints.clientapi.integration.odk.requests

import android.app.Application
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.simprints.clientapi.activities.errors.ErrorActivity
import com.simprints.clientapi.activities.odk.OdkActivity
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.session.SessionEvents
import com.simprints.moduleapi.app.requests.IAppEnrollRequest
import com.simprints.moduleapi.app.requests.IAppIdentifyRequest
import com.simprints.moduleapi.app.requests.IAppVerifyRequest
import com.simprints.moduleapi.app.requests.confirmations.IAppIdentifyConfirmation
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


internal const val appEnrolAction = "com.simprints.clientapp.REGISTER"
internal const val appIdentifyAction = "com.simprints.clientapp.IDENTIFY"
internal const val appVerifyAction = "com.simprints.clientapp.VERIFY"
internal const val appConfirmIdentityAction = "com.simprints.clientapp.CONFIRM_IDENTITY"
internal val clientApiAlertActivityClassName = ErrorActivity::javaClass.name

internal const val odkEnrolAction = "com.simprints.simodkadapter.REGISTER"
internal const val odkIdentifyAction = "com.simprints.simodkadapter.IDENTIFY"
internal const val odkVerifyAction = "com.simprints.simodkadapter.VERIFY"
internal const val odkConfirmIdentityAction = "com.simprints.simodkadapter.CONFIRM_IDENTITY"

internal val projectIdField = "projectId" to "some_project"
internal val userIdField = "userId" to "some_user_id"
internal val moduleIdField = "moduleId" to "some_module_id"
internal val metadataField = "metadata" to "some_metadata"
internal val verifyGuidField = "verifyGuid" to "8b3f577c-b6c7-4677-9af2-b08cd7f71b79"
internal val sessionIdField = "sessionId" to "some_sessionid"
internal val selectedGuidField = "selectedGuid" to "8b3f577c-b6c7-4677-9af2-b08cd7f71b79"

internal val extraField = "extra" to "some_extra"

internal val packageName = ApplicationProvider.getApplicationContext<Application>().packageName
internal val commCareActivityName = OdkActivity::class.qualifiedName!!


internal val baseIntentRequest = Intent().apply {
    setClassName(packageName, commCareActivityName)
    putExtra(projectIdField.key(), projectIdField.value())
    putExtra(userIdField.key(), userIdField.value())
    putExtra(moduleIdField.key(), moduleIdField.value())
    putExtra(metadataField.key(), metadataField.value())
}

internal val invalidIntentRequest = Intent().apply {
    setClassName(packageName, commCareActivityName)
    putExtra(projectIdField.key() + "_wrong", projectIdField.value())
    putExtra(userIdField.key(), userIdField.value())
    putExtra(moduleIdField.key(), moduleIdField.value())
}

internal val suspiciousIntentRequest = baseIntentRequest.apply {
    putExtra(extraField.key(), extraField.value())
}

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

