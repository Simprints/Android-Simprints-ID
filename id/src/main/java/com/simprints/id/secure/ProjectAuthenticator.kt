package com.simprints.id.secure

import com.simprints.id.secure.models.NonceScope
import com.simprints.infra.login.exceptions.AuthRequestInvalidCredentialsException
import com.simprints.infra.login.exceptions.RequestingIntegrityTokenException
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import java.io.IOException

interface ProjectAuthenticator {

    /**
     * @throws IOException
     * @throws AuthRequestInvalidCredentialsException
     * @throws BackendMaintenanceException
     * @throws SyncCloudIntegrationException
     * @throws RequestingIntegrityTokenException
     */
    suspend fun authenticate(nonceScope: NonceScope, projectSecret: String, deviceId: String)

}
