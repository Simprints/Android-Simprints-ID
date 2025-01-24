package com.simprints.feature.clientapi.mappers.request

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.clientapi.exceptions.InvalidRequestException
import com.simprints.feature.clientapi.models.ClientApiConstants
import com.simprints.feature.clientapi.usecases.GetCurrentSessionIdUseCase
import com.simprints.feature.clientapi.usecases.SessionHasIdentificationCallbackUseCase
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.libsimprints.Constants.SIMPRINTS_LIB_VERSION
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
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.any

class IntentToActionMapperTest {
    @get:Rule
    val coroutinesTestRule = TestCoroutineRule()

    @MockK
    private lateinit var getCurrentSessionIdUseCase: GetCurrentSessionIdUseCase

    @MockK
    private lateinit var sessionHasIdentificationCallback: SessionHasIdentificationCallbackUseCase

    @MockK
    private lateinit var tokenizationProcessor: TokenizationProcessor

    @MockK
    private lateinit var timeHelper: TimeHelper

    private lateinit var mapper: IntentToActionMapper

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { getCurrentSessionIdUseCase.invoke() } returns SESSION_ID
        coEvery { sessionHasIdentificationCallback.invoke(any()) } returns true
        every { timeHelper.now() } returns Timestamp(0L)

        mapper = IntentToActionMapper(
            getCurrentSessionIdUseCase,
            sessionHasIdentificationCallback,
            tokenizationProcessor,
            timeHelper,
        )
    }

    @Test
    fun `correctly handles valid ODK intent actions`() = runTest {
        mapOf(
            "com.simprints.simodkadapter.REGISTER" to ActionRequest.EnrolActionRequest::class,
            "com.simprints.simodkadapter.IDENTIFY" to ActionRequest.IdentifyActionRequest::class,
            "com.simprints.simodkadapter.VERIFY" to ActionRequest.VerifyActionRequest::class,
            "com.simprints.simodkadapter.CONFIRM_IDENTITY" to ActionRequest.ConfirmIdentityActionRequest::class,
            "com.simprints.simodkadapter.REGISTER_LAST_BIOMETRICS" to ActionRequest.EnrolLastBiometricActionRequest::class,
        ).forEach { (action, expectedClass) ->
            assertThat(mapper(action, defaultExtras, any())).isInstanceOf(expectedClass.java)
        }
    }

    @Test
    fun `throws exception for invalid ODK intent actions`() = runTest {
        assertThrows<InvalidRequestException> {
            mapper("com.simprints.simodkadapter.INVALID", defaultExtras, any())
        }
    }

    @Test
    fun `correctly handles valid CommCare intent actions`() = runTest {
        mapOf(
            "com.simprints.commcare.REGISTER" to ActionRequest.EnrolActionRequest::class,
            "com.simprints.commcare.IDENTIFY" to ActionRequest.IdentifyActionRequest::class,
            "com.simprints.commcare.VERIFY" to ActionRequest.VerifyActionRequest::class,
            "com.simprints.commcare.CONFIRM_IDENTITY" to ActionRequest.ConfirmIdentityActionRequest::class,
            "com.simprints.commcare.REGISTER_LAST_BIOMETRICS" to ActionRequest.EnrolLastBiometricActionRequest::class,
        ).forEach { (action, expectedClass) ->
            assertThat(mapper(action, defaultExtras, any())).isInstanceOf(expectedClass.java)
        }
    }

    @Test
    fun `correctly handles CommCare intent without session ID`() = runTest {
        mapOf(
            "com.simprints.commcare.CONFIRM_IDENTITY" to ActionRequest.ConfirmIdentityActionRequest::class,
            "com.simprints.commcare.REGISTER_LAST_BIOMETRICS" to ActionRequest.EnrolLastBiometricActionRequest::class,
        ).forEach { (action, expectedClass) ->
            assertThat(mapper(action, noSessionExtras, any())).isInstanceOf(expectedClass.java)
        }
    }

    @Test
    fun `correctly handles CommCare intent with blank session ID`() = runTest {
        mapOf(
            "com.simprints.commcare.CONFIRM_IDENTITY" to ActionRequest.ConfirmIdentityActionRequest::class,
            "com.simprints.commcare.REGISTER_LAST_BIOMETRICS" to ActionRequest.EnrolLastBiometricActionRequest::class,
        ).forEach { (action, expectedClass) ->
            assertThat(mapper(action, blankSessionExtras, any())).isInstanceOf(expectedClass.java)
        }
    }

    @Test
    fun `throws exception for invalid CommCare intent actions`() = runTest {
        assertThrows<InvalidRequestException> {
            mapper("com.simprints.commcare.INVALID", defaultExtras, any())
        }
    }

    @Test
    fun `correctly handles  valid LibSimprints intent actions`() = runTest {
        mapOf(
            "com.simprints.id.REGISTER" to ActionRequest.EnrolActionRequest::class,
            "com.simprints.id.IDENTIFY" to ActionRequest.IdentifyActionRequest::class,
            "com.simprints.id.VERIFY" to ActionRequest.VerifyActionRequest::class,
            "com.simprints.id.CONFIRM_IDENTITY" to ActionRequest.ConfirmIdentityActionRequest::class,
            "com.simprints.id.REGISTER_LAST_BIOMETRICS" to ActionRequest.EnrolLastBiometricActionRequest::class,
        ).forEach { (action, expectedClass) ->
            assertThat(mapper(action, defaultExtras, any())).isInstanceOf(expectedClass.java)
        }
    }

    @Test
    fun `throws exception for invalid LibSimprints intent actions`() = runTest {
        assertThrows<InvalidRequestException> {
            mapper("com.simprints.id.INVALID", defaultExtras, any())
        }
    }

    @Test
    fun `throws exception for invalid package intent actions`() = runTest {
        assertThrows<InvalidRequestException> {
            mapper("com.unknown.package.INVALID", defaultExtras, any())
        }
    }

    @Test
    fun `throws exception for empty package intent actions`() = runTest {
        assertThrows<InvalidRequestException> {
            mapper("", defaultExtras, any())
        }
    }

    @Test
    fun `handles empty meta info extra`() = runTest {
        val extras = mapOf(
            SIMPRINTS_PROJECT_ID to "projectId-1111111111",
            SIMPRINTS_USER_ID to "userId",
            SIMPRINTS_MODULE_ID to "moduleId",
        )

        val action = mapper("com.simprints.id.REGISTER", extras, any())
        assertThat(action.actionIdentifier.callerPackageName).isEmpty()
        assertThat(action.actionIdentifier.contractVersion).isEqualTo(1)
    }

    @Test
    fun `correctly parses meta info extra`() = runTest {
        val action = mapper("com.simprints.id.REGISTER", defaultExtras, any())
        assertThat(action.actionIdentifier.callerPackageName).isNotEmpty()
        assertThat(action.actionIdentifier.contractVersion).isGreaterThan(1)
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
            ClientApiConstants.CALLER_PACKAGE_NAME to "com.package.name",
            SIMPRINTS_LIB_VERSION to 5,
        )

        private val noSessionExtras = mapOf(
            SIMPRINTS_PROJECT_ID to "projectId-1111111111",
            SIMPRINTS_USER_ID to "userId",
            SIMPRINTS_MODULE_ID to "moduleId",
            SIMPRINTS_SELECTED_GUID to SESSION_ID,
            SIMPRINTS_VERIFY_GUID to SESSION_ID,
        )

        private val blankSessionExtras = mapOf(
            SIMPRINTS_PROJECT_ID to "projectId-1111111111",
            SIMPRINTS_USER_ID to "userId",
            SIMPRINTS_MODULE_ID to "moduleId",
            SIMPRINTS_SESSION_ID to "",
            SIMPRINTS_SELECTED_GUID to SESSION_ID,
            SIMPRINTS_VERIFY_GUID to SESSION_ID,
        )
    }
}
