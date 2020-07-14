package com.simprints.face.configuration

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.face.license.data.repository.LicenseRepository
import kotlinx.coroutines.launch

class ConfigurationViewModel(private val licenseRepository: LicenseRepository) : ViewModel() {
    val licenseRetrieved: MutableLiveData<LiveDataEventWithContent<String?>> = MutableLiveData()

    fun retrieveLicense(projectId: String, deviceId: String) = viewModelScope.launch {
        val newLicense = licenseRepository.getLicense(projectId, deviceId)
        licenseRetrieved.send(newLicense)
    }

}
