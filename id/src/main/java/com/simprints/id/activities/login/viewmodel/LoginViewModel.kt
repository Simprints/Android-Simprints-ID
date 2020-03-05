package com.simprints.id.activities.login.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.id.secure.ProjectAuthenticator
import com.simprints.id.secure.models.NonceScope
import kotlinx.coroutines.launch

class LoginViewModel(private val projectAuthenticator: ProjectAuthenticator) : ViewModel() {

    fun signIn(suppliedProjectId: String,
               suppliedUserId: String,
               suppliedProjectSecret: String) {
        viewModelScope.launch {
            val nonceScope = NonceScope(suppliedProjectId, suppliedUserId)
            projectAuthenticator.authenticate2(nonceScope, suppliedProjectSecret)
        }
    }

}
