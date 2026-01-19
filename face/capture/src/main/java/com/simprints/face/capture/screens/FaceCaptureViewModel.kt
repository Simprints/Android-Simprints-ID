package com.simprints.face.capture.screens

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.DeviceID
import com.simprints.core.domain.capture.BiometricReferenceCapture
import com.simprints.core.domain.capture.BiometricTemplateCapture
import com.simprints.core.domain.common.Modality
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.time.Timestamp
import com.simprints.face.capture.models.FaceDetection
import com.simprints.face.capture.usecases.BitmapToByteArrayUseCase
import com.simprints.face.capture.usecases.SaveFaceSampleUseCase
import com.simprints.face.capture.usecases.ShouldShowInstructionsScreenUseCase
import com.simprints.face.capture.usecases.SimpleCaptureEventReporter
import com.simprints.face.infra.biosdkresolver.ResolveFaceBioSdkUseCase
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ModalitySdkType
import com.simprints.infra.license.LicenseRepository
import com.simprints.infra.license.LicenseStatus
import com.simprints.infra.license.SaveLicenseCheckEventUseCase
import com.simprints.infra.license.determineLicenseStatus
import com.simprints.infra.license.models.License
import com.simprints.infra.license.models.License.Companion.NO_LICENSE
import com.simprints.infra.license.models.LicenseState
import com.simprints.infra.license.models.LicenseVersion
import com.simprints.infra.license.models.Vendor
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.FACE_CAPTURE
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.LICENSE
import com.simprints.infra.logging.Simber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
internal class FaceCaptureViewModel @Inject constructor(
    private val authStore: AuthStore,
    private val configRepository: ConfigRepository,
    private val saveFaceImage: SaveFaceSampleUseCase,
    private val eventReporter: SimpleCaptureEventReporter,
    private val bitmapToByteArray: BitmapToByteArrayUseCase,
    private val licenseRepository: LicenseRepository,
    private val resolveFaceBioSdk: ResolveFaceBioSdkUseCase,
    private val saveLicenseCheckEvent: SaveLicenseCheckEventUseCase,
    private val shouldShowInstructions: ShouldShowInstructionsScreenUseCase,
    @param:DeviceID private val deviceID: String,
) : ViewModel() {
    // Updated in live feedback screen
    var attemptNumber: Int = 0
    var samplesToCapture = 1
    var initialised = false
    lateinit var bioSDK: ModalitySdkType

    var shouldCheckCameraPermissions = AtomicBoolean(true)

    private var faceDetections = listOf<FaceDetection>()

    val recaptureEvent: LiveData<LiveDataEvent>
        get() = _recaptureEvent
    private val _recaptureEvent = MutableLiveData<LiveDataEvent>()

    val exitFormEvent: LiveData<LiveDataEvent>
        get() = _exitFormEvent
    private val _exitFormEvent = MutableLiveData<LiveDataEvent>()

    val unexpectedErrorEvent: LiveData<LiveDataEvent>
        get() = _unexpectedErrorEvent
    private val _unexpectedErrorEvent = MutableLiveData<LiveDataEvent>()

    val finishFlowEvent: LiveData<LiveDataEventWithContent<BiometricReferenceCapture>>
        get() = _finishFlowEvent
    private val _finishFlowEvent = MutableLiveData<LiveDataEventWithContent<BiometricReferenceCapture>>()

    val invalidLicense: LiveData<LiveDataEvent>
        get() = _invalidLicense
    private val _invalidLicense = MutableLiveData<LiveDataEvent>()

    fun setupCapture(samplesToCapture: Int) {
        this.samplesToCapture = samplesToCapture
    }

    fun initFaceBioSdk(
        activity: Activity,
        sdk: ModalitySdkType,
    ) = viewModelScope.launch {
        if (initialised) {
            Simber.i("Face bio SDK already initialised", tag = FACE_CAPTURE)
            return@launch
        }
        this@FaceCaptureViewModel.bioSDK = sdk

        Simber.i("Starting face capture flow", tag = FACE_CAPTURE)
        if (sdk == ModalitySdkType.RANK_ONE) {
            val licenseVendor = Vendor.RankOne
            val license = licenseRepository.getCachedLicense(licenseVendor)
            var licenseStatus = license.determineLicenseStatus()
            if (licenseStatus == LicenseStatus.VALID) {
                licenseStatus = initialize(activity, license!!)
            }

            // In some cases license is invalidated on initialisation attempt
            if (licenseStatus != LicenseStatus.VALID) {
                Simber.i("Face license is $licenseStatus - attempting download", tag = LICENSE)
                licenseStatus = refreshLicenceAndRetry(
                    activity,
                    licenseVendor,
                    LicenseVersion(
                        configRepository
                            .getProjectConfiguration()
                            .face
                            ?.rankOne
                            ?.version
                            .orEmpty(),
                    ),
                )
            }
            // Still invalid after attempted refresh
            if (licenseStatus != LicenseStatus.VALID) {
                Simber.i("Face license is $licenseStatus", tag = LICENSE)
                licenseRepository.deleteCachedLicense(Vendor.RankOne)
                _invalidLicense.send()
            }
            saveLicenseCheckEvent(licenseVendor, licenseStatus)
        } else {
            initialize(activity, NO_LICENSE)
        }
    }

    fun shouldShowInstructionsScreen(): Boolean = shouldShowInstructions()

    private suspend fun initialize(
        activity: Activity,
        license: License,
    ): LicenseStatus {
        val initializer = resolveFaceBioSdk(bioSDK).initializer
        if (!initializer.tryInitWithLicense(activity, license.data)) {
            // License is valid but the SDK failed to initialize
            // This is should reported as an error
            return LicenseStatus.ERROR
        }
        initialised = true
        return LicenseStatus.VALID
    }

    private suspend fun refreshLicenceAndRetry(
        activity: Activity,
        licenseVendor: Vendor,
        licenseVersion: LicenseVersion,
    ) = licenseRepository
        .redownloadLicence(authStore.signedInProjectId, deviceID, licenseVendor, licenseVersion)
        .map { state ->
            when (state) {
                is LicenseState.FinishedWithSuccess -> initialize(activity, state.license)
                is LicenseState.FinishedWithBackendMaintenanceError, is LicenseState.FinishedWithError -> LicenseStatus.MISSING
                else -> null
            }
        }.filterNotNull()
        .last()

    fun getSampleDetection() = faceDetections.firstOrNull()

    fun flowFinished() {
        Simber.i("Finishing capture flow", tag = FACE_CAPTURE)
        viewModelScope.launch {
            val faceConfiguration = configRepository.getProjectConfiguration().face?.getSdkConfiguration(bioSDK)
            if (faceConfiguration?.imageSavingStrategy?.shouldSaveImage() == true) {
                saveFaceDetections()
            }

            val items = faceDetections.map { detection ->
                BiometricTemplateCapture(
                    captureEventId = detection.id,
                    template = detection.face?.template ?: ByteArray(0),
                )
            }
            val referenceId = UUID.randomUUID().toString()
            eventReporter.addBiometricReferenceCreationEvents(referenceId, items.map { it.captureEventId })

            val format = faceDetections
                .firstOrNull()
                ?.face
                ?.format
                .orEmpty()
            _finishFlowEvent.send(BiometricReferenceCapture(referenceId, Modality.FACE, format, items))
        }
    }

    fun captureFinished(newFaceDetections: List<FaceDetection>) {
        faceDetections = newFaceDetections
    }

    fun handleBackButton() {
        _exitFormEvent.send()
    }

    fun recapture() {
        Simber.i("Starting face recapture flow", tag = FACE_CAPTURE)
        faceDetections = listOf()
        _recaptureEvent.send()
    }

    private fun saveFaceDetections() {
        Simber.i("Saving captures to disk", tag = FACE_CAPTURE)
        faceDetections.forEach { saveImage(it, it.id) }
    }

    private fun saveImage(
        faceDetection: FaceDetection,
        captureEventId: String,
    ) {
        runBlocking {
            faceDetection.securedImageRef = saveFaceImage(bitmapToByteArray(faceDetection.bitmap), captureEventId)
        }
    }

    fun submitError(throwable: Throwable) {
        Simber.e("Face capture failed", throwable, FACE_CAPTURE)
        _unexpectedErrorEvent.send()
    }

    fun addOnboardingComplete(startTime: Timestamp) {
        Simber.i("Face capture onboarding complete", tag = FACE_CAPTURE)
        eventReporter.addOnboardingCompleteEvent(startTime)
    }

    fun addCaptureConfirmationAction(
        startTime: Timestamp,
        isContinue: Boolean,
    ) {
        eventReporter.addCaptureConfirmationEvent(startTime, isContinue)
    }
}
