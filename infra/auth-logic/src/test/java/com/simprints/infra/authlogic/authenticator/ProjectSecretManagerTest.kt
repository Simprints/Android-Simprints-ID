package com.simprints.infra.authlogic.authenticator

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProjectSecretManagerTest {

    private val projectSecretManager = ProjectSecretManager()


    @Test
    fun `should encrypt the project secret`() {
        val projectSecret = "project_secret"
        val publicKey =
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCAmxhSp1nSNOkRianJtMEP6uEznURRKeLmnr5q/KJnMosVeSHCtFlsDeNrjaR9r90sUgn1oA++ixcu3h6sG4nq4BEgDHi0aHQnZrFNq+frd002ji5sb9dUM2n6M7z8PPjMNiy7xl//qDIbSuwMz9u5G1VjovE4Ej0E9x1HLmXHRQIDAQAB"
        val projectSecretEncrypted =
            projectSecretManager.encryptProjectSecret(projectSecret, publicKey)
        assertThat(projectSecretEncrypted).isNotEmpty()
    }
}
