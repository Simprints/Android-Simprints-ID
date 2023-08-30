package com.simprints.feature.clientapi.mappers.request

import com.google.common.truth.Truth.assertThat
import com.simprints.feature.clientapi.exceptions.InvalidRequestException
import com.simprints.feature.clientapi.models.ActionRequest
import com.simprints.feature.clientapi.session.ClientSessionManager
import com.simprints.libsimprints.Constants.SIMPRINTS_MODULE_ID
import com.simprints.libsimprints.Constants.SIMPRINTS_PROJECT_ID
import com.simprints.libsimprints.Constants.SIMPRINTS_SELECTED_GUID
import com.simprints.libsimprints.Constants.SIMPRINTS_SESSION_ID
import com.simprints.libsimprints.Constants.SIMPRINTS_USER_ID
import com.simprints.libsimprints.Constants.SIMPRINTS_VERIFY_GUID
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class IntentToActionMapperTest {

    @get:Rule
    val coroutinesTestRule = TestCoroutineRule()

    @MockK
    private lateinit var sessionManager: ClientSessionManager

    private lateinit var mapper: IntentToActionMapper

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { sessionManager.getCurrentSessionId() } returns SESSION_ID
        coEvery { sessionManager.sessionHasIdentificationCallback(any()) } returns true
        coEvery { sessionManager.isCurrentSessionAnIdentificationOrEnrolment() } returns true

        mapper = IntentToActionMapper(sessionManager)
    }

    @Test
    fun `correctly handles valid ODK intent actions`() = runTest {
        mapOf(
            "com.simprints.simodkadapter.REGISTER" to ActionRequest.EnrolActionRequest::class,
            "com.simprints.simodkadapter.IDENTIFY" to ActionRequest.IdentifyActionRequest::class,
            "com.simprints.simodkadapter.VERIFY" to ActionRequest.VerifyActionRequest::class,
            "com.simprints.simodkadapter.CONFIRM_IDENTITY" to ActionRequest.ConfirmActionRequest::class,
            "com.simprints.simodkadapter.REGISTER_LAST_BIOMETRICS" to ActionRequest.EnrolLastBiometricActionRequest::class,
        ).forEach { (action, expectedClass) ->
            assertThat(mapper(action, defaultExtras)).isInstanceOf(expectedClass.java)
        }
    }

    @Test
    fun `throws exception for invalid ODK intent actions`() = runTest {
        assertThrows<InvalidRequestException> {
            mapper("com.simprints.simodkadapter.INVALID", defaultExtras)
        }
    }

    @Test
    fun `correctly handles valid CommCare intent actions`() = runTest {
        mapOf(
            "com.simprints.commcare.REGISTER" to ActionRequest.EnrolActionRequest::class,
            "com.simprints.commcare.IDENTIFY" to ActionRequest.IdentifyActionRequest::class,
            "com.simprints.commcare.VERIFY" to ActionRequest.VerifyActionRequest::class,
            "com.simprints.commcare.CONFIRM_IDENTITY" to ActionRequest.ConfirmActionRequest::class,
            "com.simprints.commcare.REGISTER_LAST_BIOMETRICS" to ActionRequest.EnrolLastBiometricActionRequest::class,
        ).forEach { (action, expectedClass) ->
            assertThat(mapper(action, defaultExtras)).isInstanceOf(expectedClass.java)
        }
    }

    @Test
    fun `throws exception for invalid CommCare intent actions`() = runTest {
        assertThrows<InvalidRequestException> {
            mapper("com.simprints.commcare.INVALID", defaultExtras)
        }
    }

    @Test
    fun `correctly handles  valid LibSimprints intent actions`() = runTest {
        mapOf(
            "com.simprints.id.REGISTER" to ActionRequest.EnrolActionRequest::class,
            "com.simprints.id.IDENTIFY" to ActionRequest.IdentifyActionRequest::class,
            "com.simprints.id.VERIFY" to ActionRequest.VerifyActionRequest::class,
            "com.simprints.id.CONFIRM_IDENTITY" to ActionRequest.ConfirmActionRequest::class,
            "com.simprints.id.REGISTER_LAST_BIOMETRICS" to ActionRequest.EnrolLastBiometricActionRequest::class,
        ).forEach { (action, expectedClass) ->
            assertThat(mapper(action, defaultExtras)).isInstanceOf(expectedClass.java)
        }
    }

    @Test
    fun `throws exception for invalid LibSimprints intent actions`() = runTest {
        assertThrows<InvalidRequestException> {
            mapper("com.simprints.id.INVALID", defaultExtras)
        }
    }

    @Test
    fun `throws exception for invalid package intent actions`() = runTest {
        assertThrows<InvalidRequestException> {
            mapper("com.unknown.package.INVALID", defaultExtras)
        }
    }

    @Test
    fun `throws exception for empty package intent actions`() = runTest {
        assertThrows<InvalidRequestException> {
            mapper("", defaultExtras)
        }
    }


    companion object {
        private const val SESSION_ID = "1d3a92c1-3410-40fb-9e88-4570c9abd150"

        private val defaultExtras = mapOf(
            SIMPRINTS_PROJECT_ID to "projectId-1111111111",
            SIMPRINTS_USER_ID to "userId",
            SIMPRINTS_MODULE_ID to "moduleId",
            SIMPRINTS_SESSION_ID to SESSION_ID,
            SIMPRINTS_SELECTED_GUID to SESSION_ID,
            SIMPRINTS_VERIFY_GUID to SESSION_ID,
        )
    }
}

