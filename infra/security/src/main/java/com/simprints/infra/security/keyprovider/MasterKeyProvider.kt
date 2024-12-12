package com.simprints.infra.security.keyprovider

import androidx.security.crypto.MasterKeys

internal class MasterKeyProvider {
    fun provideMasterKey(): String = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
}
