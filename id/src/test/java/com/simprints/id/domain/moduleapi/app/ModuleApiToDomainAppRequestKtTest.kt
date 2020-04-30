package com.simprints.id.domain.moduleapi.app

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_METADATA
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.moduleApi.AppConfirmaConfirmIdentityRequestModuleApi
import com.simprints.id.commontesttools.moduleApi.AppEnrollRequestModuleApi
import com.simprints.id.commontesttools.moduleApi.AppIdentifyRequestModuleApi
import com.simprints.id.commontesttools.moduleApi.AppVerifyRequestModuleApi
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow.*
import org.junit.Test
import java.util.*

class ModuleApiToDomainAppRequestKtTest {

    @Test
    fun fromIAppEnrolRequestToAppEnrolRequest() {
        val iAppRequest = AppEnrollRequestModuleApi(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, DEFAULT_METADATA)
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
        val iAppRequest = AppVerifyRequestModuleApi(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, DEFAULT_METADATA, toVerify)
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
        val iAppRequest = AppIdentifyRequestModuleApi(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, DEFAULT_METADATA)
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
        val iAppRequest = AppConfirmaConfirmIdentityRequestModuleApi(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, sessionId, selectedGuid)
        val domainRequest = iAppRequest.fromModuleApiToDomain() as AppRequest.AppConfirmIdentityRequest
        with(domainRequest) {
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(userId).isEqualTo(DEFAULT_USER_ID)
            assertThat(sessionId).isEqualTo(sessionId)
            assertThat(selectedGuid).isEqualTo(selectedGuid)
        }
    }
}
