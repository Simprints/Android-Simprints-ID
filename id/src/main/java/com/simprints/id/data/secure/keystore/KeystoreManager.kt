package com.simprints.id.data.secure.keystore

interface KeystoreManager {

    /**
     * @throws MissingPrivateKeyInKeystore
     * @throws InvalidDecryptionData
     **/
    fun decryptString(string: String): String

    fun encryptString(string: String): String
}
