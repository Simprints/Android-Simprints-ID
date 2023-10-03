package domain.requests

import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.domain.requests.ConfirmIdentityRequest
import com.simprints.clientapi.requestFactories.RequestFactory
import com.simprints.core.domain.tokenization.asTokenized
import org.junit.Test

class ConfirmIdentityRequestTest {

    @Test
    fun `when converting to app request, tokenization flags are set correctly to true`() {
        val result = buildRequest(isTokenized = true).convertToAppRequest()
        assertThat(result.isUserIdTokenized).isTrue()
    }

    @Test
    fun `when converting to app request, tokenization flags are set correctly to false`() {
        val result = buildRequest(isTokenized = false).convertToAppRequest()
        assertThat(result.isUserIdTokenized).isFalse()
    }

    private fun buildRequest(isTokenized: Boolean) = ConfirmIdentityRequest(
        userId = "userId".asTokenized(isTokenized),
        projectId = RequestFactory.MOCK_PROJECT_ID,
        sessionId = RequestFactory.MOCK_SESSION_ID,
        selectedGuid = RequestFactory.MOCK_SELECTED_GUID,
        unknownExtras = emptyMap()
    )
}
