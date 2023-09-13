package com.simprints.id.domain.moduleapi.app

import com.google.common.truth.Truth.assertThat
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow.*
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFollowUp.AppConfirmIdentityRequest
import com.simprints.id.testtools.moduleApi.AppConfirmationConfirmIdentityRequestModuleApi
import com.simprints.id.testtools.moduleApi.AppEnrolRequestModuleApi
import com.simprints.id.testtools.moduleApi.AppIdentifyRequestModuleApi
import com.simprints.id.testtools.moduleApi.AppVerifyRequestModuleApi
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_METADATA
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_USER_ID
import org.junit.Test
import java.util.*

class ModuleApiToDomainAppRequestKtTest {

    @Test
    fun fromIAppEnrolRequestToAppEnrolRequest() {
        val iAppRequest = AppEnrolRequestModuleApi(
            projectId = DEFAULT_PROJECT_ID,
            userId = DEFAULT_USER_ID.value,
            moduleId = DEFAULT_MODULE_ID.value,
            metadata = DEFAULT_METADATA
        )
        val domainRequest = iAppRequest.fromModuleApiToDomain() as AppEnrolRequest
        with(domainRequest) {
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(userId).isEqualTo(DEFAULT_USER_ID)
            assertThat(moduleId).isEqualTo(DEFAULT_MODULE_ID)
            assertThat(metadata).isEqualTo(DEFAULT_METADATA)
        }
    }

    @Test
    fun fromIAppVerifyRequestToAppVerifyRequest() {
        val toVerify = UUID.randomUUID().toString()
        val iAppRequest = AppVerifyRequestModuleApi(
            projectId = DEFAULT_PROJECT_ID,
            userId = DEFAULT_USER_ID.value,
            moduleId = DEFAULT_MODULE_ID.value,
            metadata = DEFAULT_METADATA,
            verifyGuid = toVerify
        )
        val domainRequest = iAppRequest.fromModuleApiToDomain() as AppVerifyRequest
        with(domainRequest) {
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(userId).isEqualTo(DEFAULT_USER_ID)
            assertThat(moduleId).isEqualTo(DEFAULT_MODULE_ID)
            assertThat(metadata).isEqualTo(DEFAULT_METADATA)
            assertThat(verifyGuid).isEqualTo(toVerify)
        }
    }

    @Test
    fun fromIAppIdentityRequestToAppIdentifyRequest() {
        val iAppRequest = AppIdentifyRequestModuleApi(
            projectId = DEFAULT_PROJECT_ID,
            userId = DEFAULT_USER_ID.value,
            moduleId = DEFAULT_MODULE_ID.value,
            metadata = DEFAULT_METADATA
        )
        val domainRequest = iAppRequest.fromModuleApiToDomain() as AppIdentifyRequest
        with(domainRequest) {
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(userId).isEqualTo(DEFAULT_USER_ID)
            assertThat(moduleId).isEqualTo(DEFAULT_MODULE_ID)
            assertThat(metadata).isEqualTo(DEFAULT_METADATA)
        }
    }

    @Test
    fun fromIAppConfirmIdentityRequestToAppConfirmIdentityRequest() {
        val selectedGuid = UUID.randomUUID().toString()
        val sessionId = UUID.randomUUID().toString()
        val iAppRequest = AppConfirmationConfirmIdentityRequestModuleApi(
            projectId = DEFAULT_PROJECT_ID,
            userId = DEFAULT_USER_ID.value,
            sessionId = sessionId,
            selectedGuid = selectedGuid
        )
        val domainRequest = iAppRequest.fromModuleApiToDomain() as AppConfirmIdentityRequest
        with(domainRequest) {
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(userId).isEqualTo(DEFAULT_USER_ID)
            assertThat(sessionId).isEqualTo(sessionId)
            assertThat(selectedGuid).isEqualTo(selectedGuid)
        }
    }
}
