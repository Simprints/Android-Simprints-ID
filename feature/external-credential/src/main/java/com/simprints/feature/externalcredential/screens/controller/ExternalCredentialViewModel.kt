package com.simprints.feature.externalcredential.screens.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.domain.common.FlowType
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.feature.externalcredential.model.ExternalCredentialParams
import com.simprints.feature.externalcredential.model.ExternalCredentialSaveResponse
import com.simprints.feature.externalcredential.model.ExternalCredentialSearchResponse
import com.simprints.infra.external.credential.store.model.ExternalCredential
import com.simprints.infra.external.credential.store.repository.ExternalCredentialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.Serializable
import javax.inject.Inject

@HiltViewModel
internal class ExternalCredentialViewModel @Inject constructor(
    private val externalCredentialRepository: ExternalCredentialRepository<ExternalCredential.QrCode>
) : ViewModel() {

    private var subjectId: String? = null
    private var flowType: FlowType? = null

    val externalCredentialSaveResponse: LiveData<LiveDataEventWithContent<Serializable>>
        get() = _externalCredentialSaveResponse
    private val _externalCredentialSaveResponse = MutableLiveData<LiveDataEventWithContent<Serializable>>()

    fun setConfig(params: ExternalCredentialParams) {
        subjectId = params.subjectId
        flowType = params.flowType
    }

    fun processExternalCredentialAndFinish(data: String) = viewModelScope.launch {
        when (flowType) {
            // When enrolling new subject, it is necessary to link the external credential with subjectId, therefore, saving it.
            FlowType.ENROL -> saveCredentialAndFinish(data)
            // During any other flow type, it is necessary to find the subject ID linked to the external credential
            else -> searchSubjectIdAndFinish(data)
        }
    }

    private suspend fun saveCredentialAndFinish(data: String) {
        externalCredentialRepository.save(
            credential = ExternalCredential.QrCode(
                data = data,
                subjectId = subjectId!!
            )
        )

        _externalCredentialSaveResponse.send(
            ExternalCredentialSaveResponse(subjectId = subjectId!!, externalCredential = data)
        )
    }

    private suspend fun searchSubjectIdAndFinish(data: String) {
        val subjectId = externalCredentialRepository.findByCredential(
            credential = ExternalCredential.QrCode(
                data = data,
                subjectId = subjectId
            )
        )?.subjectId

        _externalCredentialSaveResponse.send(
            ExternalCredentialSearchResponse(subjectId = subjectId, externalCredential = data)
        )
    }

}
