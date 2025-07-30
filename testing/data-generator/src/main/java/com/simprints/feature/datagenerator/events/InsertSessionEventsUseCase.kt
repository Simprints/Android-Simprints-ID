package com.simprints.feature.datagenerator.events

import com.simprints.core.DispatcherIO
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import kotlin.random.Random

class InsertSessionEventsUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val sessionGenerator: SessionGenerator,
    @DispatcherIO private val dispatcher: CoroutineDispatcher,
) {
    operator fun invoke(
        projectId: String,
        moduleId: String,
        attendantId: String,
        enrolCount: Int,
        identifyCount: Int,
        verifyCount: Int,
        confirmIdentifyCount: Int,
        enrolLastCount: Int,
    ) = flow {
        val eventsCount = eventRepository.observeEventCount(null).first()
        insertEnrollmentEvents(projectId, moduleId, attendantId, enrolCount)
        emit("$enrolCount Enrollment sessions inserted successfully")
        insertIdentifyEvents(projectId, moduleId, attendantId, identifyCount)
        emit("$identifyCount Identify sessions inserted successfully")
        insertVerifyEvents(projectId, moduleId, attendantId, verifyCount)
        emit("$verifyCount Verify sessions inserted successfully")
        insertConfirmIdentifyEvents(projectId, moduleId, attendantId, confirmIdentifyCount)
        emit("$confirmIdentifyCount Confirm Identify sessions inserted successfully")
        insertEnrolLastEvents(projectId, moduleId, attendantId, enrolLastCount)
        emit("$enrolLastCount Enrol Last sessions inserted successfully")

        val newEventsCount = eventRepository.observeEventCount(null).first()
        Simber.i(
            "Generated ${newEventsCount - eventsCount} events",
            tag = "InsertSessionEventsUseCase",
        )

        emit("Generated a total of ${newEventsCount - eventsCount} new events")
        sessionGenerator.clearCache()
    }.flowOn(dispatcher)

    private suspend fun insertEnrollmentEvents(
        projectId: String,
        moduleId: String,
        attendantId: String,
        enrolCount: Int,
    ) {
        batchInsertEvents(count = enrolCount) { scopeId ->
            when (Random.nextInt(2)) {
                0 -> sessionGenerator.generateEnrolmentIso(
                    projectId = projectId,
                    moduleId = moduleId,
                    attendantId = attendantId,
                    scopeId = scopeId,
                )

                else -> sessionGenerator.generateEnrolmentSimFace(
                    projectId = projectId,
                    moduleId = moduleId,
                    attendantId = attendantId,
                    scopeId = scopeId,
                )
            }
        }
    }

    private suspend fun insertIdentifyEvents(
        projectId: String,
        moduleId: String,
        attendantId: String,
        identifyCount: Int,
    ) {
        batchInsertEvents(count = identifyCount) { scopeId ->
            sessionGenerator.generateIdentificationRoc3(
                projectId = projectId,
                moduleId = moduleId,
                attendantId = attendantId,
                scopeId = scopeId,
            )
        }
    }

    private suspend fun insertVerifyEvents(
        projectId: String,
        moduleId: String,
        attendantId: String,
        verifyCount: Int,
    ) {
        batchInsertEvents(count = verifyCount) { scopeId ->
            sessionGenerator.generateVerificationRoc3(
                projectId = projectId,
                moduleId = moduleId,
                attendantId = attendantId,
                scopeId = scopeId,
            )
        }
    }

    private suspend fun insertConfirmIdentifyEvents(
        projectId: String,
        moduleId: String,
        attendantId: String,
        confirmIdentifyCount: Int,
    ) {
        batchInsertEvents(count = confirmIdentifyCount) { scopeId ->
            sessionGenerator.generateConfirmationRoc3(
                projectId = projectId,
                moduleId = moduleId,
                attendantId = attendantId,
                scopeId = scopeId,
            )
        }
    }

    private suspend fun insertEnrolLastEvents(
        projectId: String,
        moduleId: String,
        attendantId: String,
        enrolLastCount: Int,
    ) {
        batchInsertEvents(count = enrolLastCount) { scopeId ->
            sessionGenerator.generateEnrolLastBioRoc3(
                projectId = projectId,
                moduleId = moduleId,
                attendantId = attendantId,
                scopeId = scopeId,
            )
        }
    }

    private suspend fun batchInsertEvents(
        count: Int,
        generateCommands: (scopeId: String) -> List<String>,
    ) {
        val eventsToInsert = mutableListOf<String>()
        repeat(count) {
            val scopeId = createEventScope()
            val dbInsertionCommands = generateCommands(scopeId)
            eventsToInsert.addAll(dbInsertionCommands)
            if (eventsToInsert.size >= BATCH_SIZE) {
                eventRepository.executeRawEventInsertions(eventsToInsert)
                eventsToInsert.clear()
            }
        }
        if (eventsToInsert.isNotEmpty()) {
            eventRepository.executeRawEventInsertions(eventsToInsert)
        }
    }

    private suspend fun createEventScope(): String {
        val eventScope = eventRepository.createEventScope(EventScopeType.SESSION)
        eventRepository.closeEventScope(eventScope, null)
        return eventScope.id
    }

    companion object {
        const val BATCH_SIZE = 200
    }
}
