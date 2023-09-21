package domain.requests

import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.domain.requests.IdentifyRequest
import com.simprints.clientapi.requestFactories.RequestFactory
import com.simprints.core.domain.tokenization.asTokenized
import com.simprints.moduleapi.app.requests.IAppIdentifyRequest
import org.junit.Test

class IdentifyRequestTest {

    @Test
    fun `when converting to app request, tokenization flags are set correctly`() {
        val result = identifyRequest.convertToAppRequest() as IAppIdentifyRequest
        assertThat(result.isUserIdTokenized).isTrue()
        assertThat(result.isModuleIdTokenized).isTrue()
    }

    companion object {
        private val USER_ID_TOKENIZED = "userId".asTokenized(isTokenized = true)
        private val MODULE_ID_TOKENIZED = "moduleId".asTokenized(isTokenized = true)
        private val identifyRequest = IdentifyRequest(
            projectId = RequestFactory.MOCK_PROJECT_ID,
            moduleId = MODULE_ID_TOKENIZED,
            userId = USER_ID_TOKENIZED,
            metadata = RequestFactory.MOCK_METADATA,
            unknownExtras = emptyMap()
        )
    }

}
