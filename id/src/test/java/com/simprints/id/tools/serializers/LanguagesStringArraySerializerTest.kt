package com.simprints.id.tools.serializers

import com.google.common.truth.Truth
import junit.framework.TestCase.assertEquals
import org.junit.Test

class LanguagesStringArraySerializerTest {

    @Test
    fun serializingLanguageArray_isAsExpected() {
        val languageArray = arrayOf("en", "fr", "de", "fa-rAF", "ny")
        val expectedString = "en,fr,de,fa-rAF,ny"
        val serializedLanguages = LanguagesStringArraySerializer().serialize(languageArray)
        assertEquals(expectedString, serializedLanguages)
    }

    @Test
    fun serializingLanguageArray_fromEmptyArrayWorks() {
        val languageArray = arrayOf<String>()
        val expectedString = ""
        val serializedLanguages = LanguagesStringArraySerializer().serialize(languageArray)
        assertEquals(expectedString, serializedLanguages)
    }

    @Test
    fun deserializeLanguageString_isAsExpected() {
        val languageString = "en,fr,de,fa-rAF,ny"
        val expectedArray = arrayOf("en", "fr", "de", "fa-rAF", "ny")
        val deserializedLanguages = LanguagesStringArraySerializer().deserialize(languageString)
        Truth.assertThat(deserializedLanguages).isEqualTo(expectedArray)
    }

    @Test
    fun deserializeLanguageString_worksEvenWithWhiteSpaceAndExtraCommas() {
        val languageString = "en ,fr ,, de,\nfa\n-rAF,ny,\r,\r,"
        val expectedArray = arrayOf("en", "fr", "de", "fa-rAF", "ny")
        val deserializedLanguages = LanguagesStringArraySerializer().deserialize(languageString)
        Truth.assertThat(deserializedLanguages).isEqualTo(expectedArray)
    }

    @Test
    fun deserializeLanguageString_toEmptyArrayWorks() {
        val languageString = ""
        val expectedArray = arrayOf<String>()
        val deserializedLanguages = LanguagesStringArraySerializer().deserialize(languageString)
        Truth.assertThat(deserializedLanguages).isEqualTo(expectedArray)
    }
}
