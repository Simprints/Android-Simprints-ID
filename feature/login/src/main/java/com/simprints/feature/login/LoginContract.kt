package com.simprints.feature.login

import com.simprints.feature.login.screens.form.LoginFormFragmentArgs

object LoginContract {

    val LOGIN_DESTINATION_ID = R.id.loginFormFragment

    fun toArgs(projectId: String, userId: String) = LoginFormFragmentArgs(
        LoginParams(projectId, userId)
    ).toBundle()
}
