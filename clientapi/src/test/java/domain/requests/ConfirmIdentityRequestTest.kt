package domain.requests

import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.domain.requests.ConfirmIdentityRequest
import com.simprints.clientapi.requestFactories.RequestFactory
import com.simprints.core.domain.tokenization.asTokenized
import org.junit.Test

class ConfirmIdentityRequestTest {

    @Test
    fun `when converting to app request, tokenization flags are set correctly`() {
        val result = confirmIdentityRequest.convertToAppRequest()
        assertThat(result.isUserIdTokenized).isTrue()
    }

    companion object {
        private val USER_ID_TOKENIZED = "userId".asTokenized(isTokenized = true)
        private val confirmIdentityRequest = ConfirmIdentityRequest(
            userId = USER_ID_TOKENIZED,
            projectId = RequestFactory.MOCK_PROJECT_ID,
            sessionId = RequestFactory.MOCK_SESSION_ID,
            selectedGuid = RequestFactory.MOCK_SELECTED_GUID,
            unknownExtras = emptyMap()
        )
    }

}
