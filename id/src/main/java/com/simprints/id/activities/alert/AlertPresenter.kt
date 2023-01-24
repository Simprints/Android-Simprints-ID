package com.simprints.id.activities.alert

import com.simprints.core.ExternalScope
import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.models.AlertScreenEvent
import com.simprints.id.domain.alert.AlertActivityViewModel
import com.simprints.id.domain.alert.AlertActivityViewModel.ButtonAction
import com.simprints.id.domain.alert.AlertActivityViewModel.ENROLMENT_LAST_BIOMETRICS_FAILED
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.domain.alert.fromAlertToAlertTypeEvent
import com.simprints.id.exitformhandler.ExitFormHelper
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.GeneralConfiguration.Modality
import com.simprints.infra.config.domain.models.GeneralConfiguration.Modality.FACE
import com.simprints.infra.config.domain.models.GeneralConfiguration.Modality.FINGERPRINT
import com.simprints.infra.logging.LoggingConstants.CrashReportTag
import com.simprints.infra.logging.Simber
import com.simprints.infra.resources.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// TODO refactor to use ViewModel in order to remove the runBlocking
class AlertPresenter @AssistedInject constructor(
    @Assisted private val view: AlertContract.View,
    @Assisted private val alertType: AlertType,
    private val eventRepository: EventRepository,
    private val configManager: ConfigManager,
    private val timeHelper: TimeHelper,
    private val exitFormHelper: ExitFormHelper,
    @ExternalScope private val externalScope: CoroutineScope,
) : AlertContract.Presenter {

    private val alertViewModel = AlertActivityViewModel.fromAlertToAlertViewModel(alertType)

    override fun start() {
        logToCrashReport()

        initButtons()
        initColours()
        initTextAndDrawables()

        alertType.fromAlertToAlertTypeEvent().let {
            externalScope.launch {
                eventRepository.addOrUpdateEvent(
                    AlertScreenEvent(
                        timeHelper.now(),
                        it
                    )
                )
            }
        }
    }

    private fun initButtons() {
        view.initLeftButton(alertViewModel.leftButton)
        view.initRightButton(alertViewModel.rightButton)
    }

    private fun initColours() {
        val color = view.getColorForColorRes(alertViewModel.backgroundColor)
        view.setLayoutBackgroundColor(color)
        view.setLeftButtonBackgroundColor(color)
        view.setRightButtonBackgroundColor(color)
    }

    private fun initTextAndDrawables() {
        view.setAlertTitleWithStringRes(alertViewModel.title)
        view.setAlertImageWithDrawableId(alertViewModel.mainDrawable)
        view.setAlertHintImageWithDrawableId(alertViewModel.hintDrawable)
        view.setAlertMessageWithStringRes(
            alertViewModel.message,
            getParamsForMessageString().toTypedArray()
        )
    }

    private fun getParamsForMessageString(): List<Any> {
        return when (alertViewModel) {
            ENROLMENT_LAST_BIOMETRICS_FAILED -> {
                getParamsForLastBiometricsFailedAlert()
            }
            else -> {
                emptyList()
            }
        }
    }

    private fun getParamsForLastBiometricsFailedAlert() = runBlocking {
        with(configManager.getProjectConfiguration().general.modalities) {
            when {
                isFingerprintAndFace() -> {
                    listOf(view.getTranslatedString(R.string.enrol_last_biometrics_alert_message_all_param))
                }
                isFace() -> {
                    listOf(view.getTranslatedString(R.string.enrol_last_biometrics_alert_message_face_param))
                }
                isFingerprint() -> {
                    listOf(view.getTranslatedString(R.string.enrol_last_biometrics_alert_message_fingerprint_param))
                }
                else -> {
                    emptyList()
                }
            }
        }
    }

    private fun List<Modality>.isFingerprintAndFace() = containsAll(
        listOf(
            FACE,
            FINGERPRINT
        )
    )

    private fun List<Modality>.isFingerprint() = contains(FINGERPRINT) && this.size == 1
    private fun List<Modality>.isFace() = contains(FACE) && this.size == 1

    override fun handleButtonClick(buttonAction: ButtonAction) {
        when (buttonAction) {
            is ButtonAction.None -> Unit
            is ButtonAction.Close -> view.closeActivityAfterCloseButton()
            is ButtonAction.TryAgain -> view.finishWithTryAgain()
            is ButtonAction.WifiSettings -> view.openWifiSettings()
            ButtonAction.WifiSettingsWithFinish -> view.openWifiSettingsAndFinishWithTryAgain()
        }
    }

    override fun handleBackButton() {
        when (alertType) {
            AlertType.UNEXPECTED_ERROR,
            AlertType.GUID_NOT_FOUND_ONLINE,
            AlertType.DIFFERENT_PROJECT_ID_SIGNED_IN,
            AlertType.DIFFERENT_USER_ID_SIGNED_IN,
            AlertType.ENROLMENT_LAST_BIOMETRICS_FAILED,
            AlertType.SAFETYNET_ERROR -> {
                view.closeActivityAfterCloseButton()
            }
            AlertType.GUID_NOT_FOUND_OFFLINE -> {
                startExitFormActivity()
            }
        }
    }

    private fun startExitFormActivity() {
        runBlocking {
            val modalities = configManager.getProjectConfiguration().general.modalities
            view.startExitForm(exitFormHelper.getExitFormActivityClassFromModalities(modalities))
        }
    }

    private fun logToCrashReport() {
        Simber.tag(CrashReportTag.ALERT.name).i(alertViewModel.name)
    }
}
