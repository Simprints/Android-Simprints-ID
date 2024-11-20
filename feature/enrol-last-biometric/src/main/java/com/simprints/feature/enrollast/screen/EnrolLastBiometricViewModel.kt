package com.simprints.feature.enrollast.screen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.time.TimeHelper
import com.simprints.feature.enrollast.EnrolLastBiometricParams
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.feature.enrollast.screen.EnrolLastState.ErrorType.DUPLICATE_ENROLMENTS
import com.simprints.feature.enrollast.screen.EnrolLastState.ErrorType.GENERAL_ERROR
import com.simprints.feature.enrollast.screen.usecase.BuildSubjectUseCase
import com.simprints.feature.enrollast.screen.usecase.HasDuplicateEnrolmentsUseCase
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.store.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.store.domain.models.Subject
import com.simprints.infra.enrolment.records.store.domain.models.SubjectAction
import com.simprints.infra.events.SessionEventRepository
import com.simprints.infra.events.event.domain.models.EnrolmentEventV2
import com.simprints.infra.events.event.domain.models.PersonCreationEvent
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ENROLMENT
import com.simprints.infra.logging.Simber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class EnrolLastBiometricViewModel @Inject constructor(
    private val timeHelper: TimeHelper,
    private val configManager: ConfigManager,
    private val eventRepository: SessionEventRepository,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val hasDuplicateEnrolments: HasDuplicateEnrolmentsUseCase,
    private val buildSubject: BuildSubjectUseCase,
) : ViewModel() {

    val finish: LiveData<LiveDataEventWithContent<EnrolLastState>>
        get() = _finish
    private var _finish = MutableLiveData<LiveDataEventWithContent<EnrolLastState>>()

    private var enrolWasAttempted = false

    fun onViewCreated(params: EnrolLastBiometricParams) {
        if (!enrolWasAttempted) {
            enrolBiometric(params)
        }
    }

    fun enrolBiometric(params: EnrolLastBiometricParams) = viewModelScope.launch {
        enrolWasAttempted = true

        val projectConfig = configManager.getProjectConfiguration()
        val modalities = projectConfig.general.modalities

        val previousLastEnrolmentResult = getPreviousEnrolmentResult(params.steps)
        if (previousLastEnrolmentResult != null) {
            _finish.send(
                previousLastEnrolmentResult.subjectId
                    ?.let { EnrolLastState.Success(it) }
                    ?: EnrolLastState.Failed(GENERAL_ERROR,modalities)
            )
            return@launch
        }
        if (hasDuplicateEnrolments(projectConfig, params.steps)) {
            _finish.send(EnrolLastState.Failed(DUPLICATE_ENROLMENTS, modalities))
            return@launch
        }

        try {
            val subject = buildSubject(params)

            Simber.tag(ENROLMENT.name).d("Enrolment in progress")
            registerEvent(subject)
            Simber.tag(ENROLMENT.name).d("Create a subject record")
            enrolmentRecordRepository.performActions(listOf(SubjectAction.Creation(subject)))
            Simber.tag(ENROLMENT.name).d("Done!")

            _finish.send(EnrolLastState.Success(subject.subjectId))
        } catch (t: Throwable) {
            Simber.tag(ENROLMENT.name).e(t)
            _finish.send(EnrolLastState.Failed(GENERAL_ERROR, modalities))
        }
    }

    private fun getPreviousEnrolmentResult(steps: List<EnrolLastBiometricStepResult>) =
        steps.filterIsInstance<EnrolLastBiometricStepResult.EnrolLastBiometricsResult>().firstOrNull()

    private suspend fun registerEvent(subject: Subject) {
        Simber.tag(ENROLMENT.name).d("Register events for enrolments")

        val personCreationEvent = eventRepository.getEventsInCurrentSession()
            .filterIsInstance<PersonCreationEvent>()
            .sortedByDescending { it.payload.createdAt }
            .first()

        eventRepository.addOrUpdateEvent(EnrolmentEventV2(
            timeHelper.now(),
            subject.subjectId,
            subject.projectId,
            subject.moduleId,
            subject.attendantId,
            personCreationEvent.id
        ))
    }

}
