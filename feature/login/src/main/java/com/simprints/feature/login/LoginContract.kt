package com.simprints.feature.login

import com.simprints.feature.login.screens.form.LoginFormFragmentArgs

object LoginContract {

    val DESTINATION = R.id.loginFormFragment

    fun toArgs(projectId: String, userId: String) = LoginFormFragmentArgs(
        LoginParams(projectId, userId)
    ).toBundle()
}
