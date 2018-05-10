package com.simprints.id.data.secure.keystore


interface KeystoreManager {

    fun decryptString(string: String): String

    fun encryptString(string: String): String

}
