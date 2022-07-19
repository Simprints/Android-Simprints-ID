package com.simprints.id.secure

import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import com.simprints.id.exceptions.safe.secure.AuthRequestInvalidCredentialsException
import com.simprints.id.exceptions.safe.secure.SafetyNetException
import com.simprints.id.secure.models.NonceScope
import java.io.IOException

interface ProjectAuthenticator {

    /**
     * @throws IOException
     * @throws AuthRequestInvalidCredentialsException
     * @throws BackendMaintenanceException
     * @throws SyncCloudIntegrationException
     * @throws SafetyNetException
     */
    suspend fun authenticate(nonceScope: NonceScope, projectSecret: String, deviceId: String)

}
