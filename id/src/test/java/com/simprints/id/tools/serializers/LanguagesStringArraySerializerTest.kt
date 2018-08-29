package com.simprints.id.tools.serializers

import com.google.common.truth.Truth
import junit.framework.Assert
import org.junit.Test

class LanguagesStringArraySerializerTest {

    @Test
    fun serializingLanguageArray_isAsExpected() {
        val languageArray = arrayOf("en", "fr", "de", "fa-rAF", "ny")
        val expectedString = "en,fr,de,fa-rAF,ny"
        val serializedLanguages = LanguagesStringArraySerializer().serialize(languageArray)
        Assert.assertEquals(expectedString, serializedLanguages)
    }

    @Test
    fun deserializeLanguageString_isAsExpected() {
        val languageString = "en,fr,de,fa-rAF,ny"
        val expectedArray = arrayOf("en", "fr", "de", "fa-rAF", "ny")
        val serializedLanguages = LanguagesStringArraySerializer().deserialize(languageString)
        Truth.assertThat(serializedLanguages).isEqualTo(expectedArray)
    }

    @Test
    fun deserializeLanguageString_worksEvenWithWhiteSpaceAndExtraCommas() {
        val languageString = "en ,fr ,, de,\nfa\n-rAF,ny,\r,\r,"
        val expectedArray = arrayOf("en", "fr", "de", "fa-rAF", "ny")
        val serializedLanguages = LanguagesStringArraySerializer().deserialize(languageString)
        Truth.assertThat(serializedLanguages).isEqualTo(expectedArray)
    }
}
