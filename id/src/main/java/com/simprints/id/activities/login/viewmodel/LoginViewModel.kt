package com.simprints.id.activities.login.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.id.secure.AuthenticationHelper
import com.simprints.id.secure.models.AuthenticateDataResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authenticationHelper: AuthenticationHelper,
) : ViewModel() {

    private val signInResultLiveData = MutableLiveData<AuthenticateDataResult>()

    fun getSignInResult(): LiveData<AuthenticateDataResult> = signInResultLiveData

    fun signIn(userId: String, projectId: String, projectSecret: String, deviceId: String) {
        viewModelScope.launch {
            val result = authenticationHelper.authenticateSafely(
                userId,
                projectId,
                projectSecret,
                deviceId
            )
            signInResultLiveData.postValue(result)
        }
    }
}
