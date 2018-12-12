package com.simprints.id.data.secure.keystore

import com.simprints.id.exceptions.unsafe.MissingPrivateKeyInKeystoreError
import com.simprints.id.exceptions.unsafe.InvalidDecryptionData

interface KeystoreManager {

    /**
     * @throws MissingPrivateKeyInKeystoreError
     * @throws InvalidDecryptionData
     **/
    fun decryptString(string: String): String

    fun encryptString(string: String): String
}
