package com.simprints.feature.login.screens.form

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.infra.network.SimNetwork
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class LoginFormViewModel @Inject constructor(
    private val simNetwork: SimNetwork,
) : ViewModel() {

    // TODO change to proper result class
    val signInResult: LiveData<Boolean?>
        get() = _signInResult
    private val _signInResult = MutableLiveData<Boolean?>(null)

    fun init() {
        simNetwork.resetApiBaseUrl()

        // TODO check google play availability

    }

    fun signInClicked() {
        // TODO process sign in
        _signInResult.postValue(false)
    }

}
