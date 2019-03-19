package com.simprints.id.data.secure.keystore

import com.simprints.id.exceptions.unexpected.MissingPrivateKeyInKeystoreException
import com.simprints.id.exceptions.safe.callout.InvalidDecryptionData

interface KeystoreManager {

    /**
     * @throws MissingPrivateKeyInKeystoreException
     * @throws InvalidDecryptionData
     **/
    fun decryptString(string: String): String

    fun encryptString(string: String): String
}
