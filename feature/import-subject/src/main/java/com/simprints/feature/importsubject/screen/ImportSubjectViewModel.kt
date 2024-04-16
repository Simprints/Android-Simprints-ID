package com.simprints.feature.importsubject.screen

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.feature.importsubject.usecase.ImportBase64ImageUseCase
import com.simprints.feature.importsubject.usecase.SaveSubjectUseCase
import com.simprints.face.infra.biosdkresolver.ResolveFaceBioSdkUseCase
import com.simprints.infra.license.LicenseRepository
import com.simprints.infra.license.LicenseStatus
import com.simprints.infra.license.determineLicenseStatus
import com.simprints.infra.license.models.Vendor
import com.simprints.infra.logging.Simber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ImportSubjectViewModel @Inject constructor(
    private val importImage: ImportBase64ImageUseCase,
    private val licenseRepository: LicenseRepository,
    private val faceSdkResolver: ResolveFaceBioSdkUseCase,
    private val saveSubject: SaveSubjectUseCase,
) : ViewModel() {

    val subjectState: LiveData<LiveDataEventWithContent<ImportSubjectState>>
        get() = _subjectState
    private var _subjectState = MutableLiveData<LiveDataEventWithContent<ImportSubjectState>>()
    private var importAttemted = false

    fun onViewCreated(activity: Activity, projectId: String, subjectId: String, image64: String) {
        if (!importAttemted) {
            importAttemted = true
            viewModelScope.launch {
                checkLicence(activity) { importSubject(projectId, subjectId, image64) }
            }
        }
    }

    private suspend inline fun checkLicence(
        activity: Activity,
        crossinline onSuccess: suspend () -> Unit,
    ) {
        val license = licenseRepository.getCachedLicense(Vendor.RankOne)
        val licenseStatus = license.determineLicenseStatus()
        val faceBioSdkInitializer = faceSdkResolver().initializer

        if (licenseStatus == LicenseStatus.VALID) {
            if (faceBioSdkInitializer.tryInitWithLicense(activity, license!!.data)) {
                onSuccess()
            } else {
                _subjectState.send(ImportSubjectState.Error("SDK init failed"))
            }
        } else {
            _subjectState.send(ImportSubjectState.Error("Licence invalid"))
        }
    }

    private suspend fun importSubject(projectId: String, subjectId: String, image64: String) {
        val faceDetector = faceSdkResolver().detector
        // import image bitmap
        val image = importImage(image64)
        if (image == null) {
            _subjectState.send(ImportSubjectState.Error("Image import failed"))
            return
        }
        _subjectState.send(ImportSubjectState.Imported(image))
        delay(1000) // To be able to see the image before it is processed

        val face = faceDetector.analyze(image)
        Simber.tag("POC").d("face detection: $face")
        if (face == null) {
            _subjectState.send(ImportSubjectState.Error("Face template extraction failed"))
            return
        }
        saveSubject(projectId, subjectId, face)

        _subjectState.send(ImportSubjectState.Complete)
    }

}
