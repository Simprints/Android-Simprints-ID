package com.simprints.feature.logincheck.usecases

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.authstore.AuthStore
import javax.inject.Inject

class UpdateStoredUserIdUseCase @Inject constructor(
    private val authStore: AuthStore,
) {
    operator fun invoke(userId: TokenizableString) {
        // This is a hack to make sure that there is a stored user ID if user logged in before 2024.1.0.
        // It could be removed after all we drop support for older versions.
        if (authStore.signedInUserId == null) {
            authStore.signedInUserId = userId
        }
    }
}
