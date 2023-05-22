package com.simprints.infra.security.keyprovider

interface MasterKeyProvider {
    fun provideMasterKey(): String
}
