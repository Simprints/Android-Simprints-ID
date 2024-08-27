package com.simprints.face.capture.screens

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.DeviceID
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.time.Timestamp
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.face.capture.models.FaceDetection
import com.simprints.face.capture.usecases.BitmapToByteArrayUseCase
import com.simprints.face.capture.usecases.SaveFaceImageUseCase
import com.simprints.face.capture.usecases.SimpleCaptureEventReporter
import com.simprints.face.infra.biosdkresolver.ResolveFaceBioSdkUseCase
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.license.LicenseRepository
import com.simprints.infra.license.LicenseState
import com.simprints.infra.license.LicenseStatus
import com.simprints.infra.license.SaveLicenseCheckEventUseCase
import com.simprints.infra.license.Vendor
import com.simprints.infra.license.determineLicenseStatus
import com.simprints.infra.license.remote.License
import com.simprints.infra.logging.LoggingConstants.CrashReportTag
import com.simprints.infra.logging.Simber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
internal class FaceCaptureViewModel @Inject constructor(
    private val authStore: AuthStore,
    private val configManager: ConfigManager,
    private val saveFaceImage: SaveFaceImageUseCase,
    private val eventReporter: SimpleCaptureEventReporter,
    private val bitmapToByteArray: BitmapToByteArrayUseCase,
    private val licenseRepository: LicenseRepository,
    private val resolveFaceBioSdk: ResolveFaceBioSdkUseCase,
    private val saveLicenseCheckEvent: SaveLicenseCheckEventUseCase,
    @DeviceID private val deviceID: String,
) : ViewModel() {

    // Updated in live feedback screen
    var attemptNumber: Int = 0
    var samplesToCapture = 1

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

    val finishFlowEvent: LiveData<LiveDataEventWithContent<FaceCaptureResult>>
        get() = _finishFlowEvent
    private val _finishFlowEvent = MutableLiveData<LiveDataEventWithContent<FaceCaptureResult>>()

    val invalidLicense: LiveData<LiveDataEvent>
        get() = _invalidLicense
    private val _invalidLicense = MutableLiveData<LiveDataEvent>()

    init {
        Simber.tag(CrashReportTag.FACE_CAPTURE.name).i("Starting face capture flow")
    }

    fun setupCapture(samplesToCapture: Int) {
        this.samplesToCapture = samplesToCapture
    }

    fun initFaceBioSdk(activity: Activity) = viewModelScope.launch {
        val licenseVendor = Vendor.RANK_ONE
        val license = licenseRepository.getCachedLicense(licenseVendor)
        var licenseStatus = license.determineLicenseStatus()
        if (licenseStatus == LicenseStatus.VALID) {
            licenseStatus = initialize(activity, license!!)
        }

        // In some cases license is invalidated on initialisation attempt
        if (licenseStatus != LicenseStatus.VALID) {
            Simber.tag(CrashReportTag.LICENSE.name).i("Face license is $licenseStatus - attempting download")
            licenseStatus = refreshLicenceAndRetry(activity, licenseVendor)
        }
        // Still invalid after attempted refresh
        if (licenseStatus != LicenseStatus.VALID) {
            Simber.tag(CrashReportTag.LICENSE.name).i("Face license is $licenseStatus")
            licenseRepository.deleteCachedLicense(Vendor.RANK_ONE)
            _invalidLicense.send()
        }
        saveLicenseCheckEvent(Vendor.RANK_ONE, licenseStatus)
    }

    private suspend fun initialize(activity: Activity, license: License): LicenseStatus {
        val initializer = resolveFaceBioSdk().initializer
        if (!initializer.tryInitWithLicense(activity, license.data)) {
            // License is valid but the SDK failed to initialize
            // This is should reported as an error
            return LicenseStatus.ERROR
        }
        return LicenseStatus.VALID
    }

    private suspend fun refreshLicenceAndRetry(activity: Activity, licenseVendor: Vendor) = licenseRepository
        .redownloadLicence(authStore.signedInProjectId, deviceID, licenseVendor)
        .map { state ->
            when (state) {
                is LicenseState.FinishedWithSuccess -> initialize(activity, state.license)
                is LicenseState.FinishedWithBackendMaintenanceError, is LicenseState.FinishedWithError -> LicenseStatus.MISSING
                else -> null
            }
        }
        .filterNotNull()
        .last()

    fun getSampleDetection() = faceDetections.firstOrNull()

    fun flowFinished() {
        viewModelScope.launch {
            val projectConfiguration = configManager.getProjectConfiguration()
            if (projectConfiguration.face?.imageSavingStrategy?.shouldSaveImage() == true) {
                saveFaceDetections()
            }

            val items = faceDetections.mapIndexed { index, detection ->
                FaceCaptureResult.Item(
                    captureEventId = detection.id, index = index, sample = FaceCaptureResult.Sample(
                        faceId = detection.id,
                        template = detection.face?.template ?: ByteArray(0),
                        imageRef = detection.securedImageRef,
                        format = detection.face?.format ?: "",
                    )
                )
            }

            _finishFlowEvent.send(FaceCaptureResult(items))
        }
    }

    fun captureFinished(newFaceDetections: List<FaceDetection>) {
        faceDetections = newFaceDetections
    }

    fun handleBackButton() {
        _exitFormEvent.send()
    }

    fun recapture() {
        Simber.tag(CrashReportTag.FACE_CAPTURE.name).i("Starting face recapture flow")
        faceDetections = listOf()
        _recaptureEvent.send()
    }

    private fun saveFaceDetections() {
        Simber.tag(CrashReportTag.FACE_CAPTURE.name).i("Saving captures to disk")
        faceDetections.forEach { saveImage(it, it.id) }
    }

    private fun saveImage(faceDetection: FaceDetection, captureEventId: String) {
        runBlocking {
            faceDetection.securedImageRef = saveFaceImage(bitmapToByteArray(faceDetection.bitmap), captureEventId)
        }
    }

    fun submitError(throwable: Throwable) {
        Simber.e(throwable)
        _unexpectedErrorEvent.send()
    }

    fun addOnboardingComplete(startTime: Timestamp) {
        eventReporter.addOnboardingCompleteEvent(startTime)
    }

    fun addCaptureConfirmationAction(startTime: Timestamp, isContinue: Boolean) {
        eventReporter.addCaptureConfirmationEvent(startTime, isContinue)
    }

}
