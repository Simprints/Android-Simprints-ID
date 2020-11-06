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
import com.simprints.id.services.sync.events.common.SYNC_LOG_TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import timber.log.Timber
import java.io.InputStream

@OptIn(ExperimentalCoroutinesApi::class)
class EventRemoteDataSourceImpl(private val simApiClientFactory: SimApiClientFactory,
                                private val jsonHelper: JsonHelper) : EventRemoteDataSource {

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

    override suspend fun getEvents(query: ApiRemoteEventQuery, scope: CoroutineScope): ReceiveChannel<Event> {
        val streaming = takeStreaming(query)
        Timber.tag(SYNC_LOG_TAG).d("[EVENT_REMOTE_SOURCE] Stream taken")

        return scope.produce(capacity = CHANNEL_CAPACITY_FOR_PROPAGATION) {
            parseStreamAndEmitEvents(streaming, this)
        }
    }

    @VisibleForTesting
    suspend fun parseStreamAndEmitEvents(streaming: InputStream, channel: ProducerScope<Event>) {
        val parser: JsonParser = JsonFactory().createParser(streaming)
        check(parser.nextToken() == START_ARRAY) { "Expected an array" }

        Timber.tag(SYNC_LOG_TAG).d("[EVENT_REMOTE_SOURCE] Start parsing stream")

        try {
            while (parser.nextToken() == START_OBJECT) {
                val event = jsonHelper.jackson.readValue(parser, ApiEvent::class.java)
                channel.send(event.fromApiToDomain())
            }

            parser.close()
            channel.close()

        } catch (t: Throwable) {
            Timber.d(t)
            parser.close()
            channel.close(t)
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
                    eventType = types.map { it.name }
                )
            }
        }.byteStream()

    override suspend fun post(projectId: String, events: List<Event>) {
        executeCall("EventUpload") {
            it.uploadEvents(projectId, ApiUploadEventsBody(events.map {
                it.fromDomainToApi()
            }))
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
        private const val CHANNEL_CAPACITY_FOR_PROPAGATION = 2000
    }
}
