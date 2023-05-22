package com.simprints.infra.security.keyprovider

internal fun interface MasterKeyProvider {
    fun provideMasterKey(): String
}
