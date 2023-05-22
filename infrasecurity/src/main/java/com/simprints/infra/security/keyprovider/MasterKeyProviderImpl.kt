package com.simprints.infra.security.keyprovider

import androidx.security.crypto.MasterKeys
import javax.inject.Inject


internal class MasterKeyProviderImpl @Inject constructor() : MasterKeyProvider {
    override fun provideMasterKey(): String = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
}
