package com.simprints.face.capture.screens

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.time.Timestamp
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.face.capture.models.FaceDetection
import com.simprints.face.capture.usecases.BitmapToByteArrayUseCase
import com.simprints.face.capture.usecases.SaveFaceImageUseCase
import com.simprints.face.capture.usecases.SimpleCaptureEventReporter
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.facebiosdk.initialization.FaceBioSdkInitializer
import com.simprints.infra.license.LicenseRepository
import com.simprints.infra.license.LicenseStatus
import com.simprints.infra.license.SaveLicenseCheckEventUseCase
import com.simprints.infra.license.Vendor
import com.simprints.infra.license.determineLicenseStatus
import com.simprints.infra.logging.LoggingConstants.CrashReportTag
import com.simprints.infra.logging.Simber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
internal class FaceCaptureViewModel @Inject constructor(
    private val configManager: ConfigManager,
    private val saveFaceImage: SaveFaceImageUseCase,
    private val eventReporter: SimpleCaptureEventReporter,
    private val bitmapToByteArray: BitmapToByteArrayUseCase,
    private val licenseRepository: LicenseRepository,
    private val faceBioSdkInitializer: FaceBioSdkInitializer,
    private val saveLicenseCheckEvent: SaveLicenseCheckEventUseCase,
) : ViewModel() {

    // Updated in live feedback screen
    var attemptNumber: Int = 0
    var samplesToCapture = 1

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
        val license = licenseRepository.getCachedLicense(Vendor.RANK_ONE)
        var licenseStatus = license.determineLicenseStatus()

        if (licenseStatus == LicenseStatus.VALID) {
            if (!faceBioSdkInitializer.tryInitWithLicense(activity, license!!.data)) {
                // License is valid but the SDK failed to initialize
                // This is should reported as an error
                licenseStatus = LicenseStatus.ERROR
            }
        }
        if (licenseStatus != LicenseStatus.VALID) {
            Simber.tag(CrashReportTag.LICENSE.name).i("Face license is $licenseStatus")
            licenseRepository.deleteCachedLicense(Vendor.RANK_ONE)
            _invalidLicense.send()
        }
        saveLicenseCheckEvent(Vendor.RANK_ONE, licenseStatus)

    }

    fun getSampleDetection() = faceDetections.firstOrNull()

    fun flowFinished() {
        viewModelScope.launch {
            val projectConfiguration = configManager.getProjectConfiguration()
            if (projectConfiguration.face?.imageSavingStrategy == FaceConfiguration.ImageSavingStrategy.ONLY_GOOD_SCAN) {
                saveFaceDetections()
            }

            val items = faceDetections.mapIndexed { index, detection ->
                FaceCaptureResult.Item(
                    captureEventId = detection.id,
                    index = index,
                    sample = FaceCaptureResult.Sample(
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
            faceDetection.securedImageRef =
                saveFaceImage(bitmapToByteArray(faceDetection.bitmap), captureEventId)
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
