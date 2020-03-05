package com.simprints.id.activities.login.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.secure.ProjectAuthenticator

class LoginViewModelFactory(
    private val projectAuthenticator: ProjectAuthenticator
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java))
            return LoginViewModel(projectAuthenticator) as T
        else
            throw IllegalArgumentException("View model not found")
    }
}
