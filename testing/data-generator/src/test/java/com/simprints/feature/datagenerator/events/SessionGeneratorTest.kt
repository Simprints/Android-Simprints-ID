package com.simprints.feature.datagenerator.events

import com.google.common.truth.Truth.*
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test

class SessionGeneratorTest {
    @MockK
    private lateinit var mockSqlLoader: SqlEventTemplateLoader

    private lateinit var sessionGenerator: SessionGenerator

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        sessionGenerator = SessionGenerator(mockSqlLoader)
    }

    @Test
    fun `generateIdentificationRoc3 SHOULD request sql for each event in its list`() {
        // GIVEN
        val projectId = "project-1"
        val attendantId = "attendant-1"
        val moduleId = "module-1"
        val scopeId = "scope-1"

        val expectedEventNames = SessionGenerator.IDENTIFICATION_ROC3_EVENTS

        expectedEventNames.forEach { eventName ->
            every {
                mockSqlLoader.getSql(
                    eventName = eventName,
                    projectId = projectId,
                    attendantId = attendantId,
                    moduleId = moduleId,
                    scopeId = scopeId,
                )
            } returns "SQL for $eventName"
        }

        // WHEN
        val resultSqlList = sessionGenerator.generateIdentificationRoc3(
            projectId = projectId,
            attendantId = attendantId,
            moduleId = moduleId,
            scopeId = scopeId,
        )

        // THEN

        expectedEventNames.forEach { eventName ->
            verify(exactly = 1) {
                mockSqlLoader.getSql(
                    eventName = eventName,
                    projectId = projectId,
                    attendantId = attendantId,
                    moduleId = moduleId,
                    scopeId = scopeId,
                )
            }
        }

        assertThat(resultSqlList).hasSize(expectedEventNames.size)

        assertThat(resultSqlList.first()).isEqualTo("SQL for ${expectedEventNames.first()}")
        assertThat(resultSqlList.last()).isEqualTo("SQL for ${expectedEventNames.last()}")
    }

    @Test
    fun `generateEnrolmentIso SHOULD request sql for each event in its list`() {
        // GIVEN
        val projectId = "project-1"
        val attendantId = "attendant-1"
        val moduleId = "module-1"
        val scopeId = "scope-1"

        val expectedEventNames = SessionGenerator.ENROLMENT_ISO_EVENTS

        every { mockSqlLoader.getSql(any(), any(), any(), any(), any()) } returns "some generated SQL"

        // WHEN
        val resultSqlList = sessionGenerator.generateEnrolmentIso(
            projectId = projectId,
            attendantId = attendantId,
            moduleId = moduleId,
            scopeId = scopeId,
        )

        // THEN
        expectedEventNames.forEach { eventName ->
            verify {
                mockSqlLoader.getSql(
                    eventName = eventName,
                    projectId = projectId,
                    attendantId = attendantId,
                    moduleId = moduleId,
                    scopeId = scopeId,
                )
            }
        }

        assertThat(resultSqlList).hasSize(expectedEventNames.size)
    }

    @Test
    fun `clearCache SHOULD delegate the call to the sql event loader`() {
        // GIVEN
        every { mockSqlLoader.clearCache() } returns Unit

        // WHEN
        sessionGenerator.clearCache()

        // THEN
        verify(exactly = 1) { mockSqlLoader.clearCache() }
    }
}
