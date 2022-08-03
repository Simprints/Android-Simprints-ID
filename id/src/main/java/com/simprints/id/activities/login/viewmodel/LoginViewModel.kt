package com.simprints.id.activities.login.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result
import com.simprints.id.secure.AuthenticationHelper
import com.simprints.id.secure.models.AuthenticateDataResult
import com.simprints.id.secure.models.toDomainResult
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel(
    private val authenticationHelper: AuthenticationHelper,
    private val dispatcher: DispatcherProvider
) : ViewModel() {

    private val signInResultLiveData = MutableLiveData<Result>()
    private val _estimatedOutage = MutableLiveData<Long?>()

    fun getSignInResult(): LiveData<Result> = signInResultLiveData
    val estimatedOutage : LiveData<Long?> = _estimatedOutage

    fun signIn(userId: String, projectId: String, projectSecret: String, deviceId: String) {
        viewModelScope.launch {
            withContext(dispatcher.io()) {
                val result = authenticationHelper.authenticateSafely(
                    userId,
                    projectId,
                    projectSecret,
                    deviceId
                )
                if (result is AuthenticateDataResult.BackendMaintenanceError) {
                    _estimatedOutage.postValue(result.estimatedOutage)
                }
                signInResultLiveData.postValue(result.toDomainResult())
            }
        }
    }
}
