package com.simprints.id.activities.login.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.id.data.db.event.domain.events.AuthenticationEvent.AuthenticationPayload.Result
import com.simprints.id.secure.AuthenticationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel(private val authenticationHelper: AuthenticationHelper) : ViewModel() {

    private val signInResultLiveData = MutableLiveData<Result>()

    fun getSignInResult(): LiveData<Result> = signInResultLiveData

    fun signIn(userId: String, projectId: String, projectSecret: String, deviceId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
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
}
