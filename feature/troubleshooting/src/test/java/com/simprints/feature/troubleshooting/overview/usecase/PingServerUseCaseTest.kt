package com.simprints.feature.troubleshooting.overview.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.troubleshooting.overview.usecase.PingServerUseCase.PingResult
import com.simprints.infra.network.url.BaseUrlProvider
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.net.HttpURLConnection
import java.net.URL

class PingServerUseCaseTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var baseUrlProvider: BaseUrlProvider

    @MockK
    private lateinit var connection: HttpURLConnection

    @MockK
    private lateinit var urlFactory: PingServerUseCase.UrlFactory

    private lateinit var useCase: PingServerUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { baseUrlProvider.getApiBaseUrlPrefix() } returns "baseUrl"
        every { urlFactory.invoke(any()) } returns mockk<URL> {
            every { openConnection() } returns connection
        }

        useCase = PingServerUseCase(
            dispatcherIO = testCoroutineRule.testCoroutineDispatcher,
            baseUrlProvider = baseUrlProvider,
            urlFactory = urlFactory,
        )
    }

    @Test
    fun `returns success when response is 404`() = runTest {
        every { connection.responseCode } returns 404
        val result = useCase().toList()

        assertThat(result.first()).isInstanceOf(PingResult.InProgress::class.java)
        assertThat(result.last()).isInstanceOf(PingResult.Success::class.java)
    }

    @Test
    fun `returns failure when response is 502`() = runTest {
        every { connection.responseCode } returns 502
        val result = useCase().toList()

        assertThat(result.first()).isInstanceOf(PingResult.InProgress::class.java)
        assertThat(result.last()).isInstanceOf(PingResult.Failure::class.java)
    }

    @Test
    fun `returns failure when connection throws exception`() = runTest {
        every { connection.connect() } throws RuntimeException("reason")
        val result = useCase().toList()

        assertThat(result.first()).isInstanceOf(PingResult.InProgress::class.java)
        assertThat(result.last()).isInstanceOf(PingResult.Failure::class.java)
    }
}
