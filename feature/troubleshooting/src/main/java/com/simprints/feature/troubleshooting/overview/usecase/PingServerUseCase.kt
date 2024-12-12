package com.simprints.feature.troubleshooting.overview.usecase

import com.simprints.core.DispatcherIO
import com.simprints.infra.network.url.BaseUrlProvider
import com.simprints.infra.uibase.annotations.ExcludedFromGeneratedTestCoverageReports
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import kotlin.time.measureTimedValue

internal class PingServerUseCase @Inject constructor(
    @DispatcherIO private val dispatcherIO: CoroutineDispatcher,
    private val baseUrlProvider: BaseUrlProvider,
    private val urlFactory: UrlFactory,
) {
    /**
     * Does the most bare-bones version of HTTP request to access the root URL
     * of the last configured BFSID server. Any response code below 500 is considered
     * a success as it means that the server has been reached.
     */
    operator fun invoke(): Flow<PingResult> = flow {
        emit(PingResult.InProgress)

        val urlToPing = urlFactory(baseUrlProvider.getApiBaseUrlPrefix())
        val executionTime = measureTimedValue {
            try {
                val connection = urlToPing.openConnection().let { it as HttpURLConnection }
                connection.connect()

                val response = connection.responseCode
                val result = if (response < HttpURLConnection.HTTP_INTERNAL_ERROR) "" else "$response"
                connection.disconnect()

                result
            } catch (e: Throwable) {
                e.message ?: e.localizedMessage ?: "Error during request"
            }
        }
        if (executionTime.value.isEmpty()) {
            emit(PingResult.Success("${executionTime.duration}"))
        } else {
            emit(PingResult.Failure(executionTime.value))
        }
    }.flowOn(dispatcherIO)

    internal sealed class PingResult {
        data object NotDone : PingResult()

        data object InProgress : PingResult()

        data class Success(
            val message: String,
        ) : PingResult()

        data class Failure(
            val message: String,
        ) : PingResult()
    }

    @ExcludedFromGeneratedTestCoverageReports(
        "Wrapper to allow substituting the URL constructor for testing purposes",
    )
    internal class UrlFactory @Inject constructor() {
        operator fun invoke(baseUrl: String) = URL(baseUrl)
    }
}
