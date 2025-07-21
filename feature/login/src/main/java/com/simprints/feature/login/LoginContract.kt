package com.simprints.feature.login

import com.simprints.core.domain.tokenization.TokenizableString

object LoginContract {
    val DESTINATION = R.id.loginFormFragment

    fun getParams(
        projectId: String,
        userId: TokenizableString,
    ) = LoginParams(projectId, userId)
}
