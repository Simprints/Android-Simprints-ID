package com.simprints.id.activities.login.viewmodel

import androidx.lifecycle.ViewModel
import com.simprints.id.activities.login.repository.LoginRepository
import com.simprints.id.data.db.session.domain.models.events.AuthenticationEvent

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    suspend fun signIn(
        suppliedProjectId: String,
        suppliedUserId: String,
        suppliedProjectSecret: String
    ): AuthenticationEvent.Result = loginRepository.authenticate(
        suppliedProjectId,
        suppliedUserId,
        suppliedProjectSecret
    )

}
