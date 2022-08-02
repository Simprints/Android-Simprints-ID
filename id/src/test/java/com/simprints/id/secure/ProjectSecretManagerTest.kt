package com.simprints.id.secure

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.login.domain.LoginInfoManager
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProjectSecretManagerTest {

    private val loginInfoManager: LoginInfoManager = mockk()
    private val projectSecretManager = ProjectSecretManager(loginInfoManager)


    @Test
    fun `should encrypt the project secret and store it`() {
        val projectSecret = "project_secret"
        val publicKey =
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCAmxhSp1nSNOkRianJtMEP6uEznURRKeLmnr5q/KJnMosVeSHCtFlsDeNrjaR9r90sUgn1oA++ixcu3h6sG4nq4BEgDHi0aHQnZrFNq+frd002ji5sb9dUM2n6M7z8PPjMNiy7xl//qDIbSuwMz9u5G1VjovE4Ej0E9x1HLmXHRQIDAQAB"
        val projectSecretEncrypted =
            projectSecretManager.encryptAndStoreAndReturnProjectSecret(projectSecret, publicKey)
        assertThat(projectSecretEncrypted).isNotEmpty()
        verify {
            loginInfoManager.setProperty("encryptedProjectSecret").value(projectSecretEncrypted)
        }
    }
}

