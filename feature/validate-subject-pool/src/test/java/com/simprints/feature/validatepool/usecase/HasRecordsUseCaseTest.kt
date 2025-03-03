package com.simprints.feature.validatepool.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class HasRecordsUseCaseTest {
    @MockK
    private lateinit var repository: EnrolmentRecordRepository

    private lateinit var usecase: HasRecordsUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        usecase = HasRecordsUseCase(repository)
    }

    @Test
    fun `Returns false if there are no records`() = runTest {
        coEvery { repository.count(any()) }.returns(0)
        assertThat(usecase(SubjectQuery())).isFalse()
    }

    @Test
    fun `Returns true if there are records`() = runTest {
        coEvery { repository.count(any()) }.returns(1)
        assertThat(usecase(SubjectQuery())).isTrue()
    }
}
