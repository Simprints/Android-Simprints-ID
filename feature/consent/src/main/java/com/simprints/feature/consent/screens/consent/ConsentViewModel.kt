package com.simprints.feature.consent.screens.consent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.SessionCoroutineScope
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.time.TimeHelper
import com.simprints.feature.consent.ConsentResult
import com.simprints.feature.consent.ConsentType
import com.simprints.feature.consent.screens.consent.helpers.GeneralConsentTextHelper
import com.simprints.feature.consent.screens.consent.helpers.ParentalConsentTextHelper
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.events.event.domain.models.ConsentEvent
import com.simprints.infra.events.session.SessionEventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.Serializable
import javax.inject.Inject

@HiltViewModel
internal class ConsentViewModel @Inject constructor(
    private val timeHelper: TimeHelper,
    private val configManager: ConfigManager,
    private val eventRepository: SessionEventRepository,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
) : ViewModel() {
    private val startConsentEventTime = timeHelper.now()

    val viewState: LiveData<ConsentViewState>
        get() = _viewState
    private val _viewState = MutableLiveData(ConsentViewState())
    private var selectedTab: Int = 0

    val showExitForm: LiveData<LiveDataEvent>
        get() = _showExitForm
    private val _showExitForm = MutableLiveData<LiveDataEvent>()

    val returnConsentResult: LiveData<LiveDataEventWithContent<Serializable>>
        get() = _returnConsentResult
    private val _returnConsentResult = MutableLiveData<LiveDataEventWithContent<Serializable>>()

    fun loadConfiguration(consentType: ConsentType) {
        viewModelScope.launch {
            val projectConfig = configManager.getProjectConfiguration()
            _viewState.postValue(
                mapConfigToViewState(
                    projectConfig = projectConfig,
                    consentType = consentType,
                    selectedTabIndex = selectedTab,
                ),
            )
        }
    }

    fun acceptClicked(currentConsentTab: ConsentTab) {
        saveConsentEvent(currentConsentTab, ConsentEvent.ConsentPayload.Result.ACCEPTED)
        _returnConsentResult.send(ConsentResult(true))
    }

    fun declineClicked(currentConsentTab: ConsentTab) {
        saveConsentEvent(currentConsentTab, ConsentEvent.ConsentPayload.Result.DECLINED)
        _showExitForm.send()
    }

    fun handleExitFormResponse(exitResult: ExitFormResult) {
        if (exitResult.wasSubmitted) {
            deleteLocationInfoFromSession()
            _returnConsentResult.send(exitResult)
        }
    }

    private fun mapConfigToViewState(
        projectConfig: ProjectConfiguration,
        consentType: ConsentType,
        selectedTabIndex: Int,
    ): ConsentViewState {
        val allowParentalConsent = projectConfig.consent.allowParentalConsent

        return ConsentViewState(
            showLogo = projectConfig.consent.displaySimprintsLogo,
            showParentalConsent = allowParentalConsent,
            consentTextBuilder = GeneralConsentTextHelper(
                projectConfig.consent,
                projectConfig.general.modalities,
                consentType,
            ),
            parentalTextBuilder = if (allowParentalConsent) {
                ParentalConsentTextHelper(
                    projectConfig.consent,
                    projectConfig.general.modalities,
                    consentType,
                )
            } else {
                null
            },
            selectedTab = selectedTabIndex,
        )
    }

    private fun saveConsentEvent(
        currentConsentTab: ConsentTab,
        result: ConsentEvent.ConsentPayload.Result,
    ) = sessionCoroutineScope.launch {
        eventRepository.addOrUpdateEvent(
            ConsentEvent(
                startConsentEventTime,
                timeHelper.now(),
                currentConsentTab.asEventPayload(),
                result,
            ),
        )
    }

    private fun deleteLocationInfoFromSession() = sessionCoroutineScope.launch {
        eventRepository.removeLocationDataFromCurrentSession()
    }

    fun setSelectedTab(index: Int) {
        selectedTab = index
    }
}
