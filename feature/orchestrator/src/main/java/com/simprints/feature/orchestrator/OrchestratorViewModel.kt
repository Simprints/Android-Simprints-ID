package com.simprints.feature.orchestrator

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.feature.orchestrator.cache.OrchestratorCache
import com.simprints.feature.orchestrator.model.OrchestratorResult
import com.simprints.feature.orchestrator.model.responses.AppRefusalResponse
import com.simprints.feature.orchestrator.steps.Step
import com.simprints.feature.orchestrator.steps.StepStatus
import com.simprints.feature.orchestrator.steps.StepsBuilder
import com.simprints.feature.orchestrator.usecases.AppResponseBuilderUseCase
import com.simprints.feature.orchestrator.usecases.CreatePersonEventUseCase
import com.simprints.feature.orchestrator.usecases.ShouldCreatePersonUseCase
import com.simprints.feature.setup.LocationStore
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.logging.Simber
import com.simprints.infra.orchestration.data.ActionRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class OrchestratorViewModel @Inject constructor(
    private val configManager: ConfigManager,
    private val cache: OrchestratorCache,
    private val locationStore: LocationStore,
    private val stepsBuilder: StepsBuilder,
    private val shouldCreatePerson: ShouldCreatePersonUseCase,
    private val createPersonEvent: CreatePersonEventUseCase,
    private val appResponseBuilder: AppResponseBuilderUseCase,
) : ViewModel() {

    private var modalities = emptySet<GeneralConfiguration.Modality>()
    private var steps = emptyList<Step>()

    val currentStep: LiveData<LiveDataEventWithContent<Step?>>
        get() = _currentStep
    private val _currentStep = MutableLiveData<LiveDataEventWithContent<Step?>>()

    val appResponse: LiveData<LiveDataEventWithContent<OrchestratorResult>>
        get() = _appResponse
    private val _appResponse = MutableLiveData<LiveDataEventWithContent<OrchestratorResult>>()


    fun handleAction(action: ActionRequest) = viewModelScope.launch {
        val projectConfiguration = configManager.getProjectConfiguration()

        modalities = projectConfiguration.general.modalities.toSet()
        steps = stepsBuilder.build(action, projectConfiguration)

        cache.actionRequest = action

        // TODO figure out restoring state for active session and follow-up actions

        doNextStep()
    }

    fun handleResult(result: Parcelable) {
        Simber.i(result.toString())
        if (result is ExitFormResult) {
            _appResponse.send(OrchestratorResult(
                cache.actionRequest,
                AppRefusalResponse.fromResult(result)
            ))
            // TODO cleanup?
            return
        }

        steps.firstOrNull { it.resultType == result::class.java }?.let {
            it.status = StepStatus.COMPLETED
            it.result = result
        }

        if (shouldCreatePerson(cache.actionRequest, modalities, steps)) {
            viewModelScope.launch { createPersonEvent(steps.mapNotNull { it.result }) }
        }
        doNextStep()
    }

    override fun onCleared() {
        super.onCleared()
        cache.steps = steps
        // TODO cleanup?
    }

    private fun doNextStep() {
        if (steps.all { it.status != StepStatus.IN_PROGRESS }) {
            val nextStep = steps.firstOrNull { it.status == StepStatus.NOT_STARTED }
            if (nextStep != null) {
                nextStep.status = StepStatus.IN_PROGRESS
                _currentStep.send(nextStep)
            } else {
                // TODO move this to more appropriate spot??
                // Acquiring location info could take long time, so we should stop location tracker
                // before returning to the caller app to avoid creating empty sessions.
                locationStore.cancelLocationCollection()
                buildAppResponse()
            }
        }
    }

    private fun buildAppResponse() = viewModelScope.launch {
        val cachedActionRequest = cache.actionRequest
        val appResponse = appResponseBuilder(cachedActionRequest, steps.mapNotNull { it.result })

        _appResponse.send(OrchestratorResult(cachedActionRequest, appResponse))
        // TODO update daily activity
    }

}

