package com.simprints.id.secure

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.commontesttools.di.DependencyRule.MockRule
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.secure.models.PublicKeyString
import com.simprints.id.testtools.di.AppModuleForTests
import com.simprints.id.testtools.roboletric.RobolectricDaggerTestConfig
import com.simprints.id.testtools.roboletric.TestApplication
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class ProjectSecretManagerTest {

    private val app = ApplicationProvider.getApplicationContext() as TestApplication

    @Inject lateinit var loginInfoManager: LoginInfoManager

    private val module by lazy {
        AppModuleForTests(app,
            remoteDbManagerRule = MockRule)
    }

    @Before
    fun setUp() {
        RobolectricDaggerTestConfig(this, module).setupAllAndFinish()
    }

    @Test
    fun validPublicKeyAndProjectSecret_shouldEncryptAndStoreIt() {
       assertEquals(loginInfoManager.getEncryptedProjectSecretOrEmpty(), "")

       val projectSecretManager = ProjectSecretManager(loginInfoManager)
       val projectSecret = "project_secret"
       val publicKey = PublicKeyString("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCAmxhSp1nSNOkRianJtMEP6uEznURRKeLmnr5q/KJnMosVeSHCtFlsDeNrjaR9r90sUgn1oA++ixcu3h6sG4nq4BEgDHi0aHQnZrFNq+frd002ji5sb9dUM2n6M7z8PPjMNiy7xl//qDIbSuwMz9u5G1VjovE4Ej0E9x1HLmXHRQIDAQAB")

       val projectSecretEncrypted = projectSecretManager.encryptAndStoreAndReturnProjectSecret(projectSecret, publicKey)
       assertTrue(projectSecretEncrypted.isNotEmpty())
   }
}
