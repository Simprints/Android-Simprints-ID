package com.simprints.feature.clientapi.mappers.response

import com.simprints.feature.clientapi.exceptions.InvalidRequestException
import com.simprints.feature.clientapi.mappers.request.requestFactories.ConfirmIdentityActionFactory
import com.simprints.feature.clientapi.models.ActionResponse
import com.simprints.feature.clientapi.models.CommCareConstants
import com.simprints.feature.clientapi.models.LibSimprintsConstants
import com.simprints.feature.clientapi.models.OdkConstants
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class ActionToIntentMapperTest {

    @MockK
    private lateinit var odkResponseMapper: OdkResponseMapper

    @MockK
    private lateinit var commCareResponseMapper: CommCareResponseMapper

    @MockK
    private lateinit var libSimprintsResponseMapper: LibSimprintsResponseMapper

    private lateinit var mapper: ActionToIntentMapper

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        mapper = ActionToIntentMapper(
            mapOdkResponse = odkResponseMapper,
            mapCommCareResponse = commCareResponseMapper,
            mapLibSimprintsResponse = libSimprintsResponseMapper
        )
    }

    @Test
    fun `Maps ODK package name to correct mapper`() {
        mapper(stupActionResponse(OdkConstants.PACKAGE_NAME))

        verify(exactly = 1) { odkResponseMapper(any()) }
        verify(exactly = 0) { commCareResponseMapper(any()) }
        verify(exactly = 0) { libSimprintsResponseMapper(any()) }
    }


    @Test
    fun `Maps CommCare package name to correct mapper`() {
        mapper(stupActionResponse(CommCareConstants.PACKAGE_NAME))

        verify(exactly = 0) { odkResponseMapper(any()) }
        verify(exactly = 1) { commCareResponseMapper(any()) }
        verify(exactly = 0) { libSimprintsResponseMapper(any()) }
    }

    @Test
    fun `Maps LibSimprints package name to correct mapper`() {
        mapper(stupActionResponse(LibSimprintsConstants.PACKAGE_NAME))

        verify(exactly = 0) { odkResponseMapper(any()) }
        verify(exactly = 0) { commCareResponseMapper(any()) }
        verify(exactly = 1) { libSimprintsResponseMapper(any()) }
    }

    @Test
    fun `Throws exception for invalid package name`() {
        assertThrows<InvalidRequestException> {
            mapper(stupActionResponse("invalid"))
        }
    }

    private fun stupActionResponse(packageName: String) = ActionResponse.ConfirmActionResponse(
        request = ConfirmIdentityActionFactory.getValidSimprintsRequest().copy(packageName = packageName),
        sessionId = "sessionId",
        eventsJson = null,
        confirmed = true,
    )

}
