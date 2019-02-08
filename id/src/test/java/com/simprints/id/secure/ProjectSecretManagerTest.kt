package com.simprints.id.secure

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.testUtils.di.AppModuleForTests
import com.simprints.id.testUtils.di.DaggerForUnitTests
import com.simprints.id.secure.models.PublicKeyString
import com.simprints.id.shared.DependencyRule.MockRule
import com.simprints.testframework.unit.reactive.RxJavaTest
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.tools.delegates.lazyVar
import com.simprints.id.testUtils.roboletric.RobolectricDaggerTestConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class ProjectSecretManagerTest : RxJavaTest, DaggerForUnitTests() {

    @Inject lateinit var loginInfoManager: LoginInfoManager

    override var module by lazyVar {
        AppModuleForTests(app,
            remoteDbManagerRule = MockRule)
    }

    @Before
    fun setUp() {
        RobolectricDaggerTestConfig(this).setupAllAndFinish()
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
