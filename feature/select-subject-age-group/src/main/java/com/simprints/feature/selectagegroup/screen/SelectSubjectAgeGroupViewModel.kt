package com.simprints.feature.selectagegroup.screen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.ExternalScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.exitform.ExitFormConfigurationBuilder
import com.simprints.feature.exitform.exitFormConfiguration
import com.simprints.feature.exitform.scannerOptions
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.AgeGroup
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.events.SessionEventRepository
import com.simprints.infra.events.event.domain.models.AgeGroupSelectionEvent
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.SESSION
import com.simprints.infra.logging.Simber
import com.simprints.infra.resources.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SelectSubjectAgeGroupViewModel @Inject constructor(
    private val timeHelper: TimeHelper,
    private val eventRepository: SessionEventRepository,
    private val buildAgeGroups: BuildAgeGroupsUseCase,
    private val configurationRepo: ConfigRepository,
    @ExternalScope private val externalScope: CoroutineScope,
) : ViewModel() {

    val finish: LiveData<LiveDataEventWithContent<AgeGroup>>
        get() = _finish
    private var _finish = MutableLiveData<LiveDataEventWithContent<AgeGroup>>()
    val ageGroups: LiveData<List<AgeGroup>>
        get() = _ageGroups
    private var _ageGroups = MutableLiveData<List<AgeGroup>>()
    private lateinit var startTime: Timestamp

    val showExitForm: LiveData<LiveDataEventWithContent<ExitFormConfigurationBuilder>>
        get() = _showExitForm
    private val _showExitForm =
        MutableLiveData<LiveDataEventWithContent<ExitFormConfigurationBuilder>>()

    fun start() = viewModelScope.launch {
        startTime = timeHelper.now()
        val ageGroups = buildAgeGroups()
        // notify the adapter
        _ageGroups.value = ageGroups
    }

    fun saveAgeGroupSelection(ageRange: AgeGroup) = externalScope.launch {
        val event = AgeGroupSelectionEvent(
            startTime,
            timeHelper.now(),
            AgeGroupSelectionEvent.AgeGroup(ageRange.startInclusive, ageRange.endExclusive)
        )
        eventRepository.addOrUpdateEvent(event)
        Simber.tag(SESSION.name).i("Added Age Group Selection Event")
        _finish.send(ageRange)
    }

    fun onBackPressed() {
        viewModelScope.launch {
            val projectConfig = configurationRepo.getProjectConfiguration()
            _showExitForm.send(getExitFormFromModalities(projectConfig.general.modalities))
        }
    }

    private fun getExitFormFromModalities(modalities: List<GeneralConfiguration.Modality>) = when {
        modalities.size != 1 -> exitFormConfiguration {
            titleRes = R.string.exit_form_title_biometrics
            backButtonRes = R.string.exit_form_continue_fingerprints_button
        }

        modalities.first() == GeneralConfiguration.Modality.FACE -> exitFormConfiguration {
            titleRes = R.string.exit_form_title_face
            backButtonRes = R.string.exit_form_continue_face_button
        }

        else -> exitFormConfiguration {
            titleRes = R.string.exit_form_title_fingerprinting
            backButtonRes = R.string.exit_form_continue_fingerprints_button
            visibleOptions = scannerOptions()
        }
    }
}
