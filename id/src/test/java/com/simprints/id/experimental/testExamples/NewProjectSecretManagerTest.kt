package com.simprints.id.experimental.testExamples

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.di.AppModuleForTests
import com.simprints.id.experimental.testTools.NewDaggerForTests
import com.simprints.id.experimental.testTools.DaggerTestConfig
import com.simprints.id.secure.ProjectSecretManager
import com.simprints.id.secure.models.PublicKeyString
import com.simprints.id.shared.DependencyRule
import com.simprints.id.testUtils.base.RxJavaTest
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.tools.delegates.lazyVar
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class NewProjectSecretManagerTest : RxJavaTest, NewDaggerForTests() {

    @Inject lateinit var loginInfoManager: LoginInfoManager

    override var module by lazyVar {
        AppModuleForTests(app,
            remoteDbManagerRule = DependencyRule.MockRule)
    }

    @Before
    fun setUp() {
        DaggerTestConfig(this).finish()
    }

    @Test
    fun validPublicKeyAndProjectSecret_shouldEncryptAndStoreIt() {
        Assert.assertEquals(loginInfoManager.getEncryptedProjectSecretOrEmpty(), "")

        val projectSecretManager = ProjectSecretManager(loginInfoManager)
        val projectSecret = "project_secret"
        val publicKey = PublicKeyString("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCAmxhSp1nSNOkRianJtMEP6uEznURRKeLmnr5q/KJnMosVeSHCtFlsDeNrjaR9r90sUgn1oA++ixcu3h6sG4nq4BEgDHi0aHQnZrFNq+frd002ji5sb9dUM2n6M7z8PPjMNiy7xl//qDIbSuwMz9u5G1VjovE4Ej0E9x1HLmXHRQIDAQAB")

        val projectSecretEncrypted = projectSecretManager.encryptAndStoreAndReturnProjectSecret(projectSecret, publicKey)
        Assert.assertTrue(projectSecretEncrypted.isNotEmpty())
    }
}
