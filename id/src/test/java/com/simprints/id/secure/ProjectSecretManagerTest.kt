package com.simprints.id.secure

import com.google.firebase.FirebaseApp
import com.simprints.id.Application
import com.simprints.id.DaggerTest
import com.simprints.id.TestAppModule
import com.simprints.id.data.prefs.loginInfo.LoginInfoManager
import com.simprints.id.secure.models.PublicKeyString
import com.simprints.id.testUtils.base.RxJavaTest
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.tools.delegates.lazyVar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import javax.inject.Inject

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class)
class ProjectSecretManagerTest : RxJavaTest, DaggerTest() {

    @Inject lateinit var loginInfoManager: LoginInfoManager

    override var module by lazyVar {
        TestAppModule(app,
            remoteDbManagerSpy = true)
    }

    @Before
    override fun setUp() {
        FirebaseApp.initializeApp(RuntimeEnvironment.application)
        app = (RuntimeEnvironment.application as TestApplication)
        super.setUp()
        testAppComponent.inject(this)
    }

    @Test
    fun validPublicKeyAndProjectSecret_shouldEncryptAndStoreIt() {
       val app = RuntimeEnvironment.application as Application

       assertEquals(loginInfoManager.getEncryptedProjectSecretOrEmpty(), "")

       val projectSecretManager = ProjectSecretManager(loginInfoManager)
       val projectSecret = "project_secret"
       val publicKey = PublicKeyString("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCAmxhSp1nSNOkRianJtMEP6uEznURRKeLmnr5q/KJnMosVeSHCtFlsDeNrjaR9r90sUgn1oA++ixcu3h6sG4nq4BEgDHi0aHQnZrFNq+frd002ji5sb9dUM2n6M7z8PPjMNiy7xl//qDIbSuwMz9u5G1VjovE4Ej0E9x1HLmXHRQIDAQAB")

       val projectSecretEncrypted = projectSecretManager.encryptAndStoreAndReturnProjectSecret(projectSecret, publicKey)
       assertTrue(projectSecretEncrypted.isNotEmpty())
   }
}
