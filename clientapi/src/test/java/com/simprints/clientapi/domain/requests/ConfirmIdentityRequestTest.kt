package com.simprints.clientapi.domain.requests

import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.requestFactories.RequestFactory.Companion.MOCK_PROJECT_ID
import com.simprints.clientapi.requestFactories.RequestFactory.Companion.MOCK_SELECTED_GUID
import com.simprints.clientapi.requestFactories.RequestFactory.Companion.MOCK_SESSION_ID
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import org.junit.Test


class ConfirmIdentityRequestTest {

    @Test
    fun `when request is converted to app request, tokenization flag is set correctly`() {
        listOf(true, false).forEach { isTokenized ->
            val userId = with("userId") {
                if (isTokenized) asTokenizableEncrypted() else asTokenizableRaw()
            }
            val request = ConfirmIdentityRequest(
                projectId = MOCK_PROJECT_ID,
                userId = userId,
                sessionId = MOCK_SESSION_ID,
                selectedGuid = MOCK_SELECTED_GUID,
                unknownExtras = emptyMap()
            )
            val result = request.convertToAppRequest()
            assertThat(result.isUserIdTokenized).isEqualTo(isTokenized)
        }
    }
}