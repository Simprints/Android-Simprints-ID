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
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.experimental
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
    private val configRepository: ConfigRepository,
    private val eventRepository: SessionEventRepository,
    @param:SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
) : ViewModel() {
    private val startConsentEventTime = timeHelper.now()
    private var selectedTab: Int = TAB_NOT_SELECTED

    val viewState: LiveData<ConsentViewState>
        field = MutableLiveData(ConsentViewState())

    val showExitForm: LiveData<LiveDataEvent>
        field = MutableLiveData<LiveDataEvent>()
    val returnConsentResult: LiveData<LiveDataEventWithContent<Serializable>>
        field = MutableLiveData<LiveDataEventWithContent<Serializable>>()

    fun loadConfiguration(consentType: ConsentType) {
        viewModelScope.launch {
            val projectConfig = configRepository.getProjectConfiguration()
            viewState.postValue(
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
        returnConsentResult.send(ConsentResult(true))
    }

    fun declineClicked(currentConsentTab: ConsentTab) {
        saveConsentEvent(currentConsentTab, ConsentEvent.ConsentPayload.Result.DECLINED)
        showExitForm.send()
    }

    fun handleExitFormResponse(exitResult: ExitFormResult) {
        if (exitResult.wasSubmitted) {
            deleteLocationInfoFromSession()
            returnConsentResult.send(exitResult)
        }
    }

    private fun mapConfigToViewState(
        projectConfig: ProjectConfiguration,
        consentType: ConsentType,
        selectedTabIndex: Int,
    ): ConsentViewState {
        val allowParentalConsent = projectConfig.consent.allowParentalConsent
        val isMultiFactorIdEnabled = projectConfig.multifactorId?.allowedExternalCredentials?.isNotEmpty() ?: false

        return ConsentViewState(
            showLogo = projectConfig.consent.displaySimprintsLogo,
            showParentalConsent = allowParentalConsent,
            consentTextBuilder = GeneralConsentTextHelper(
                config = projectConfig.consent,
                modalities = projectConfig.general.modalities,
                consentType = consentType,
                isMultiFactorIdEnabled = isMultiFactorIdEnabled,
            ),
            parentalTextBuilder = if (allowParentalConsent) {
                ParentalConsentTextHelper(
                    config = projectConfig.consent,
                    modalities = projectConfig.general.modalities,
                    consentType = consentType,
                    isMultiFactorIdEnabled = isMultiFactorIdEnabled,
                )
            } else {
                null
            },
            selectedTab = if (selectedTabIndex == TAB_NOT_SELECTED) {
                if (allowParentalConsent && projectConfig.experimental().useParentalConsentAsDefault) {
                    PARENTAL_CONSENT_TAB
                } else {
                    GENERAL_CONSENT_TAB
                }
            } else {
                selectedTabIndex
            },
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

    companion object {
        const val TAB_NOT_SELECTED = -1
        const val GENERAL_CONSENT_TAB = 0
        const val PARENTAL_CONSENT_TAB = 1
    }
}
