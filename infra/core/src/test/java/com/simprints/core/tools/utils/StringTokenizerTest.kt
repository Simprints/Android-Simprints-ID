package com.simprints.core.tools.utils

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.crypto.tink.daead.DeterministicAeadConfig
import io.mockk.MockKAnnotations
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StringTokenizerTest {
    private lateinit var stringTokenizer: StringTokenizer

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        DeterministicAeadConfig.register()
        stringTokenizer = StringTokenizer(EncodingUtilsImpl)
    }

    @Test
    fun `when value is encrypted then decryption returns the same value`() = with(stringTokenizer) {
        val value = " some untrimmed value with speci@l ch@r#cters and numb3rs_  "
        val encrypted = encrypt(value, KEYSET_JSON)
        assertThat(decrypt(encrypted, KEYSET_JSON)).isEqualTo(value)
    }

    /**
     * This test verifies that the tokenization process in SID produces the same result as in BFSID
     */
    @Test
    fun `encryption should produce the same value as BFSID`() = with(stringTokenizer) {
        val value = "module 1"
        val expectedEncrypted = "Ac1LV23Dfoo6m8usBUX/U7hInen/DeNgJEUQLzo="

        assertThat(encrypt(value, BFSID_KEYSET_JSON)).isEqualTo(expectedEncrypted)
    }

    companion object {
        private val KEYSET_JSON =
            """
            {
               "primaryKeyId":3444266861,
               "key":[
                  {
                     "keyData":{
                        "typeUrl":"type.googleapis.com/google.crypto.tink.AesSivKey",
                        "value":"EkAnu5jmYn8LZCdFZ3a93CMmRirb38FJLVTCiqZNPHb4b/z5NqZm/wceHtsfuVjUcKNBcovgKHi/MfUastghymI9",
                        "keyMaterialType":"SYMMETRIC"
                     },
                     "status":"ENABLED",
                     "keyId":3444266861,
                     "outputPrefixType":"TINK"
                  }
               ]
            }
            """.trimIndent()
        private val BFSID_KEYSET_JSON =
            """
            {
               "primaryKeyId":3444266861,
               "key":[
                  {
                     "keyData":{
                        "typeUrl":"type.googleapis.com/google.crypto.tink.AesSivKey",
                        "value":"EkAnu5jmYn8LZCdFZ3a93CMmRirb38FJLVTCiqZNPHb4b/z5NqZm/wceHtsfuVjUcKNBcovgKHi/MfUastghymI9",
                        "keyMaterialType":"SYMMETRIC"
                     },
                     "status":"ENABLED",
                     "keyId":3444266861,
                     "outputPrefixType":"TINK"
                  }
               ]
            }
            """.trimIndent()
    }
}
