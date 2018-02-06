package com.simprints.id.secure

import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.secure.models.PublicKeyString
import com.simprints.id.tools.base.RxJavaTest
import junit.framework.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class ProjectSecretManagerText : RxJavaTest() {

   @Test
    fun validPublicKeyAndProjectSecret_shouldEncryptAndStoreIt() {
       val app = RuntimeEnvironment.application as Application

       assertEquals(app.secureDataManager.getEncryptedProjectSecretOrEmpty(), "")

       val projectSecretManager = ProjectSecretManager(app.secureDataManager)
       val projectSecret = "project_secret"
       val publicKey = PublicKeyString("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCAmxhSp1nSNOkRianJtMEP6uEznURRKeLmnr5q/KJnMosVeSHCtFlsDeNrjaR9r90sUgn1oA++ixcu3h6sG4nq4BEgDHi0aHQnZrFNq+frd002ji5sb9dUM2n6M7z8PPjMNiy7xl//qDIbSuwMz9u5G1VjovE4Ej0E9x1HLmXHRQIDAQAB")

       val testObserver = projectSecretManager.encryptAndStoreAndReturnProjectSecret(projectSecret, publicKey).test()
       testObserver.awaitTerminalEvent()
       testObserver
           .assertNoErrors()
           .assertComplete()

       val encryptedProjectSecret = "YCJUfHsDEMqggt-Lni8ugKlgWEn1MfsyssN5o4Wr85YXfGNrAXdTnsBAjFZCC0j3zPoNjulORKnT\n" +
           "UO-jfO6cXp0It0yz8Vcar1AEceSGBpTNOxk00gxqdPhRcgL21HTs_6glBnGxgeKOcIxgf5SOY82z\n" +
           "nr0CHhr8F48hqBLDmOU=\n"

       assertEquals(encryptedProjectSecret, app.secureDataManager.encryptedProjectSecret)
   }
}
