package com.simprints.feature.login

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.feature.login.screens.form.LoginFormFragmentArgs

object LoginContract {
    val DESTINATION = R.id.loginFormFragment

    fun toArgs(
        projectId: String,
        userId: TokenizableString,
    ) = LoginFormFragmentArgs(
        LoginParams(projectId, userId),
    ).toBundle()
}
