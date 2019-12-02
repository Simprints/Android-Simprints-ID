package com.simprints.id.data.secure.keystore

import com.simprints.id.exceptions.safe.callout.InvalidDecryptionData
import com.simprints.id.exceptions.unexpected.MissingPrivateKeyInKeystoreException

@Deprecated("use androidx.security.crypto")
interface KeystoreManager {

    /**
     * @throws MissingPrivateKeyInKeystoreException
     * @throws InvalidDecryptionData
     **/
    fun decryptString(string: String): String
}
