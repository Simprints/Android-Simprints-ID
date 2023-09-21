package domain.requests

import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.domain.requests.EnrolRequest
import com.simprints.clientapi.requestFactories.RequestFactory
import com.simprints.core.domain.tokenization.asTokenized
import com.simprints.moduleapi.app.requests.IAppEnrolRequest
import org.junit.Test

class EnrollRequestTest {

    @Test
    fun `when converting to app request, tokenization flags are set correctly`() {
        val result = enrolRequest.convertToAppRequest() as IAppEnrolRequest
        assertThat(result.isUserIdTokenized).isTrue()
        assertThat(result.isModuleIdTokenized).isTrue()
    }

    companion object {
        private val USER_ID_TOKENIZED = "userId".asTokenized(isTokenized = true)
        private val MODULE_ID_TOKENIZED = "moduleId".asTokenized(isTokenized = true)
        private val enrolRequest = EnrolRequest(
            projectId = RequestFactory.MOCK_PROJECT_ID,
            moduleId = MODULE_ID_TOKENIZED,
            userId = USER_ID_TOKENIZED,
            metadata = RequestFactory.MOCK_METADATA,
            unknownExtras = emptyMap()
        )
    }

}
