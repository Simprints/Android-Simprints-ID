package com.simprints.id.testtools.di

import android.content.SharedPreferences
import com.simprints.infra.security.SecurityManager
import com.simprints.infra.security.SecurityModule
import com.simprints.infra.security.keyprovider.LocalDbKey
import dagger.Binds
import dagger.Module
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import javax.inject.Inject

@Module
@TestInstallIn(
    components = [ActivityComponent::class],
    replaces = [SecurityModule::class]
)
abstract class TestSecurityModule {

    @Binds
    internal abstract fun bindSecurityManager(impl: MockSecurityManager): SecurityManager

}

class MockSecurityManager @Inject constructor(): SecurityManager {

    override fun buildEncryptedSharedPreferences(filename: String): SharedPreferences = mockk()

    override fun createLocalDatabaseKeyIfMissing(dbName: String) {

    }

    override fun getLocalDbKeyOrThrow(dbName: String): LocalDbKey = mockk()

    override fun checkIfDeviceIsRooted() {

    }


}
