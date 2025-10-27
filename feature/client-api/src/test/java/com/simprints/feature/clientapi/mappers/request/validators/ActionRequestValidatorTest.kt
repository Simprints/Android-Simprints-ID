package com.simprints.feature.clientapi.mappers.request.validators

import com.simprints.feature.clientapi.exceptions.InvalidRequestException
import com.simprints.feature.clientapi.mappers.request.requestFactories.RequestActionFactory
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.every
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal abstract class ActionRequestValidatorTest(
    private val mockFactory: RequestActionFactory,
) {
    @Test
    open fun `valid ActionRequest should not fail`() = runTest {
        mockFactory.getValidator(mockFactory.getMockExtractor()).validate()
    }

    @Test
    open fun `should fail if no projectId`() = runTest {
        val extractor = mockFactory.getMockExtractor()
        every { extractor.getProjectId() } returns ""

        assertThrows<InvalidRequestException> { mockFactory.getValidator(extractor).validate() }
    }

    @Test
    open fun `should fail with projectId of invalid length`() = runTest {
        val extractor = mockFactory.getMockExtractor()
        every { extractor.getProjectId() } returns "a".repeat(19)

        assertThrows<InvalidRequestException> { mockFactory.getValidator(extractor).validate() }
    }

    @Test
    open fun `should fail if no userId`() = runTest {
        val extractor = mockFactory.getMockExtractor()
        every { extractor.getUserId() } returns ""

        assertThrows<InvalidRequestException> { mockFactory.getValidator(extractor).validate() }
    }

    @Test
    open fun `should fail if no moduleId`() = runTest {
        val extractor = mockFactory.getMockExtractor()
        every { extractor.getModuleId() } returns ""

        assertThrows<InvalidRequestException> { mockFactory.getValidator(extractor).validate() }
    }

    @Test
    open fun `should fail with illegal moduleId`() = runTest {
        val extractor = mockFactory.getMockExtractor()
        every { extractor.getModuleId() } returns "moduleId|moduleId"

        assertThrows<InvalidRequestException> { mockFactory.getValidator(extractor).validate() }
    }

    @Test
    open fun `should not fail if no metadata`() = runTest {
        val extractor = mockFactory.getMockExtractor()
        every { extractor.getMetadata() } returns ""

        mockFactory.getValidator(extractor).validate()
    }

    @Test
    open fun `should not fail if valid metadata`() = runTest {
        val extractor = mockFactory.getMockExtractor()
        every { extractor.getMetadata() } returns "{}"

        mockFactory.getValidator(extractor).validate()
    }

    @Test
    open fun `should fail with illegal metadata`() = runTest {
        val extractor = mockFactory.getMockExtractor()
        every { extractor.getMetadata() } returns "{illegalJson"

        assertThrows<InvalidRequestException> { mockFactory.getValidator(extractor).validate() }
    }
}
