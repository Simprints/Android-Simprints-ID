package com.simprints.id.activities.alert

import com.simprints.core.domain.modality.Modality
import com.simprints.core.tools.extentions.inBackground
import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.models.AlertScreenEvent
import com.simprints.infraresources.R
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.domain.alert.AlertActivityViewModel
import com.simprints.id.domain.alert.AlertActivityViewModel.*
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.domain.alert.fromAlertToAlertTypeEvent
import com.simprints.id.exitformhandler.ExitFormHelper
import com.simprints.infra.logging.LoggingConstants.CrashReportTag
import com.simprints.infra.logging.Simber
import javax.inject.Inject

class AlertPresenter(
    val view: AlertContract.View,
    val component: AppComponent,
    private val alertType: AlertType
) : AlertContract.Presenter {

    @Inject
    lateinit var eventRepository: EventRepository
    @Inject
    lateinit var preferencesManager: IdPreferencesManager
    @Inject
    lateinit var timeHelper: TimeHelper
    @Inject
    lateinit var exitFormHelper: ExitFormHelper

    private val alertViewModel = AlertActivityViewModel.fromAlertToAlertViewModel(alertType)

    init {
        component.inject(this)
    }

    override fun start() {
        logToCrashReport()

        initButtons()
        initColours()
        initTextAndDrawables()

        alertType.fromAlertToAlertTypeEvent()?.let {
            inBackground {
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
            MODALITY_DOWNLOAD_CANCELLED -> {
                getParamsForModalityDownloadCancelledAlert()
            }
            else -> {
                emptyList()
            }
        }
    }

    private fun getParamsForLastBiometricsFailedAlert() = with(preferencesManager.modalities) {
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

    private fun getParamsForModalityDownloadCancelledAlert() = with(preferencesManager.modalities) {
        when {
            isFingerprintAndFace() -> {
                listOf(view.getTranslatedString(R.string.fingerprint_face_feature_alert))
            }
            isFace() -> {
                listOf(view.getTranslatedString(R.string.face_feature_alert))
            }
            isFingerprint() -> {
                listOf(view.getTranslatedString(R.string.fingerprint_feature_alert))
            }
            else -> {
                emptyList()
            }
        }
    }

    private fun List<Modality>.isFingerprintAndFace() = containsAll(
        listOf(
            Modality.FACE,
            Modality.FINGER
        )
    )

    private fun List<Modality>.isFingerprint() = contains(Modality.FINGER) && this.size == 1
    private fun List<Modality>.isFace() = contains(Modality.FACE) && this.size == 1

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
            AlertType.OFFLINE_DURING_SETUP,
            AlertType.SETUP_MODALITY_DOWNLOAD_CANCELLED -> view.closeActivityAfterCloseButton()
        }
    }

    private fun startExitFormActivity() {
        view.startExitForm(exitFormHelper.getExitFormActivityClassFromModalities(preferencesManager.modalities))
    }

    private fun logToCrashReport() {
        Simber.tag(CrashReportTag.ALERT.name).i(alertViewModel.name)
    }
}
