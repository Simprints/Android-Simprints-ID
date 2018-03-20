package com.simprints.id.secure

import com.google.android.gms.safetynet.SafetyNetClient
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.exceptions.safe.secure.AuthRequestInvalidCredentialsException
import com.simprints.id.exceptions.safe.secure.DifferentProjectIdReceivedFromIntentException
import com.simprints.id.exceptions.safe.secure.InvalidLegacyProjectIdReceivedFromIntentException
import com.simprints.id.exceptions.safe.secure.SimprintsInternalServerException
import com.simprints.id.secure.cryptography.Hasher
import com.simprints.id.secure.models.NonceScope
import com.simprints.id.secure.models.ProjectId
import io.reactivex.Completable
import io.reactivex.Single


class LegacyCompatibleProjectAuthenticator(secureDataManager: SecureDataManager,
                                           dbManager: DbManager,
                                           safetyNetClient: SafetyNetClient,
                                           apiClient: ApiServiceInterface = ApiService().api,
                                           attestationManager: AttestationManager = AttestationManager()
) : ProjectAuthenticator(secureDataManager, dbManager, safetyNetClient, apiClient, attestationManager) {

    private val legacyProjectIdManager = LegacyProjectIdManager(apiClient)

    @Throws(
        DifferentProjectIdReceivedFromIntentException::class,
        InvalidLegacyProjectIdReceivedFromIntentException::class,
        AuthRequestInvalidCredentialsException::class,
        SimprintsInternalServerException::class)
    fun authenticate(nonceScope: NonceScope, projectSecret: String, legacyProjectId: String?): Completable =
        if (legacyProjectId != null)
            checkLegacyProjectIdMatchesProjectId(nonceScope.projectId, legacyProjectId)
                .andThen(authenticate(nonceScope, projectSecret))
        else
            authenticate(nonceScope, projectSecret)

    private fun checkLegacyProjectIdMatchesProjectId(expectedProjectId: String, legacyProjectId: String): Completable {
        val hashedLegacyProjectId = Hasher().hash(legacyProjectId)
        return legacyProjectIdManager.requestProjectId(hashedLegacyProjectId)
            .checkReceivedProjectIdIsAsExpected(expectedProjectId)
    }

    private fun Single<out ProjectId>.checkReceivedProjectIdIsAsExpected(expectedProjectId: String): Completable =
        flatMapCompletable { projectId ->
            val receivedProjectId = projectId.value
            if (receivedProjectId != expectedProjectId)
                throw DifferentProjectIdReceivedFromIntentException.withProjectIds(expectedProjectId, receivedProjectId)
            Completable.complete()
        }
}
