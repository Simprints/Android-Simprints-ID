package com.simprints.infra.license.local

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import java.io.File
import javax.inject.Inject

class MasterKeyHelper @Inject constructor() {
    private fun getMasterKeyAlias() = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    fun getEncryptedFileBuilder(
        file: File,
        context: Context,
    ): EncryptedFile = EncryptedFile
        .Builder(
            file,
            context,
            getMasterKeyAlias(),
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB,
        ).build()
}
