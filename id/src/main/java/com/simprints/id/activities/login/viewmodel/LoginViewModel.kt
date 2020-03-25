package com.simprints.id.activities.login.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.id.activities.login.repository.LoginRepository
import com.simprints.id.data.db.session.domain.models.events.AuthenticationEvent
import kotlinx.coroutines.launch

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val signInResultLiveData = MutableLiveData<AuthenticationEvent.Result>()

    fun getSignInResult(): LiveData<AuthenticationEvent.Result> = signInResultLiveData

    fun signIn(projectId: String, userId: String, projectSecret: String, apiBaseUrl: String?) {
        viewModelScope.launch {
            val result = loginRepository.authenticate(projectId, userId, projectSecret, apiBaseUrl)
            signInResultLiveData.postValue(result)
        }
    }

}
