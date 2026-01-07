package com.simprints.feature.logincheck.usecases

import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.orchestration.data.ActionRequest
import javax.inject.Inject

class EnsureActionFieldsTokenizedUseCase @Inject constructor(
    private val configManager: ConfigManager,
    private val tokenizationProcessor: TokenizationProcessor,
) {
    suspend operator fun invoke(action: ActionRequest): ActionRequest {
        val project = configManager.getProject() ?: return action

        // There is no automatic `.copy()` on the the interfaces, so we have to enumerate all sub-classes separately
        return when (action) {
            is ActionRequest.EnrolActionRequest -> action.copy(
                userId = tokenizationProcessor.tokenizeIfNecessary(action.userId, TokenKeyType.AttendantId, project),
                moduleId = tokenizationProcessor.tokenizeIfNecessary(action.moduleId, TokenKeyType.ModuleId, project),
            )

            is ActionRequest.IdentifyActionRequest -> action.copy(
                userId = tokenizationProcessor.tokenizeIfNecessary(action.userId, TokenKeyType.AttendantId, project),
                moduleId = tokenizationProcessor.tokenizeIfNecessary(action.moduleId, TokenKeyType.ModuleId, project),
            )

            is ActionRequest.VerifyActionRequest -> action.copy(
                userId = tokenizationProcessor.tokenizeIfNecessary(action.userId, TokenKeyType.AttendantId, project),
                moduleId = tokenizationProcessor.tokenizeIfNecessary(action.moduleId, TokenKeyType.ModuleId, project),
            )

            is ActionRequest.EnrolLastBiometricActionRequest -> action.copy(
                userId = tokenizationProcessor.tokenizeIfNecessary(action.userId, TokenKeyType.AttendantId, project),
                moduleId = tokenizationProcessor.tokenizeIfNecessary(action.moduleId, TokenKeyType.ModuleId, project),
            )

            is ActionRequest.ConfirmIdentityActionRequest -> action.copy(
                userId = tokenizationProcessor.tokenizeIfNecessary(action.userId, TokenKeyType.AttendantId, project),
            )
        }
    }
}
