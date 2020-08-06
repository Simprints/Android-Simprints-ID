package com.simprints.id.data.db.event.remote

import androidx.annotation.VisibleForTesting
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken.START_ARRAY
import com.fasterxml.jackson.core.JsonToken.START_OBJECT
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.event.domain.EventCount
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.remote.models.ApiEvent
import com.simprints.id.data.db.event.remote.models.fromApiToDomain
import com.simprints.id.data.db.event.remote.models.fromDomainToApi
import com.simprints.id.network.SimApiClient
import com.simprints.id.network.SimApiClientFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import okhttp3.internal.toImmutableList
import timber.log.Timber
import java.io.InputStream

class EventRemoteDataSourceImpl(private val simApiClientFactory: SimApiClientFactory) : EventRemoteDataSource {

    override suspend fun count(query: ApiRemoteEventQuery): List<EventCount> =
        with(query) {
            executeCall("EventCount") { eventsRemoteInterface ->
                eventsRemoteInterface.countEvents(
                    projectId = projectId,
                    moduleIds = moduleIds,
                    attendantId = userId,
                    subjectId = subjectId,
                    modes = modes,
                    lastEventId = lastEventId,
                    eventType = types
                ).map { it.fromApiToDomain() }
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getEvents(query: ApiRemoteEventQuery, scope: CoroutineScope): ReceiveChannel<List<Event>> {
        val streaming = takeStreaming(query)
        return scope.produce {
            parseStreamAndEmitEvents(streaming, this)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @VisibleForTesting
    suspend fun parseStreamAndEmitEvents(streaming: InputStream, channel: ProducerScope<List<Event>>, batchSize: Int = BATCH_SIZE_FOR_DOWNLOADING) {
        val parser: JsonParser = JsonFactory().createParser(streaming)
        check(parser.nextToken() == START_ARRAY) { "Expected an array" }
        val buffer = mutableListOf<ApiEvent>()

        try {
            while (parser.nextToken() == START_OBJECT) {

                val event = JsonHelper.jackson.readValue(parser, ApiEvent::class.java)
                buffer.add(event)

                if (buffer.size >= batchSize) {
                    if (!channel.isClosedForSend) {
                        channel.send(buffer.toImmutableList().map { it.fromApiToDomain() })
                    }
                    buffer.clear()
                }
            }

            channel.send(buffer.toImmutableList().map { it.fromApiToDomain() })

        } catch (t: Throwable) {
            Timber.d(t)
            if (!channel.isClosedForSend) {
                channel.send(buffer.toImmutableList().map { it.fromApiToDomain() })
                channel.close(t)
            }
            parser.close()
        }
    }

    private suspend fun takeStreaming(query: ApiRemoteEventQuery) =
        with(query) {
            executeCall("EventDownload") { eventsRemoteInterface ->
                eventsRemoteInterface.downloadEvents(
                    projectId = projectId,
                    moduleIds = moduleIds,
                    attendantId = userId,
                    subjectId = subjectId,
                    modes = modes,
                    lastEventId = lastEventId,
                    eventType = types.map { it.key }
                )
            }
        }.byteStream()

    override suspend fun post(projectId: String, events: List<Event>) {
        executeCall("EventUpload") {
            it.uploadEvents(projectId, ApiUploadEventsBody(events.map { it.fromDomainToApi() }))
        }
    }

    private suspend fun <T> executeCall(nameCall: String, block: suspend (EventRemoteInterface) -> T): T =
        with(getEventsApiClient()) {
            executeCall(nameCall) {
                block(it)
            }
        }

    private suspend fun getEventsApiClient(): SimApiClient<EventRemoteInterface> =
        simApiClientFactory.buildClient(EventRemoteInterface::class)

    companion object {
        const val BATCH_SIZE_FOR_DOWNLOADING = 200
    }
}
