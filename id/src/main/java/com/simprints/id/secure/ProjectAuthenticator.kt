package com.simprints.id.secure

import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.exceptions.safe.secure.AuthRequestInvalidCredentialsException
import com.simprints.id.secure.models.NonceScope
import java.io.IOException

interface ProjectAuthenticator {

    /**
     * @throws IOException
     * @throws AuthRequestInvalidCredentialsException
     * @throws SimprintsInternalServerException
     * @throws com.simprints.id.exceptions.safe.secure.SafetyNetException
     */
    suspend fun authenticate(nonceScope: NonceScope, projectSecret: String)
}
