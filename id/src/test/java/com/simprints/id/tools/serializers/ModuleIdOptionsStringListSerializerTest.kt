package com.simprints.id.tools.serializers

import com.google.common.truth.Truth
import junit.framework.Assert
import org.junit.Test

class ModuleIdOptionsStringListSerializerTest {

    @Test
    fun serializingModuleIdOptionsList_isAsExpected() {
        val moduleIdOptionsList = listOf("module1", "module2", "module3", "module4", "module5")
        val expectedString = "module1|module2|module3|module4|module5"
        val serializedModuleIdOptions = ModuleIdOptionsStringListSerializer().serialize(moduleIdOptionsList)
        Assert.assertEquals(expectedString, serializedModuleIdOptions)
    }

    @Test
    fun serializingModuleIdOptionsList_fromEmptyListWorks() {
        val moduleIdOptionsList = listOf<String>()
        val expectedString = ""
        val serializedModuleIdOptions = ModuleIdOptionsStringListSerializer().serialize(moduleIdOptionsList)
        Assert.assertEquals(expectedString, serializedModuleIdOptions)
    }

    @Test
    fun deserializeModuleIdOptionsString_isAsExpected() {
        val languageString = "module1|module2|module3|module4|module5"
        val expectedList = listOf("module1", "module2", "module3", "module4", "module5")
        val deserializedModuleIdOptions = ModuleIdOptionsStringListSerializer().deserialize(languageString)
        Truth.assertThat(deserializedModuleIdOptions).isEqualTo(expectedList)
    }

    @Test
    fun deserializeModuleIdOptions_worksEvenWithWhiteSpaceAndExtraPipes() {
        val languageString = "module1  ||m\nodule2\n\r|||module 3|module4|module5\r\r\n "
        val expectedList = listOf("module1  ", "m\nodule2\n\r", "module 3", "module4", "module5\r\r\n ")
        val deserializedModuleIdOptions = ModuleIdOptionsStringListSerializer().deserialize(languageString)
        Truth.assertThat(deserializedModuleIdOptions).isEqualTo(expectedList)
    }

    @Test
    fun deserializeModuleIdOptions_toEmptyListWorks() {
        val languageString = ""
        val expectedList = listOf<String>()
        val deserializedModuleIdOptions = ModuleIdOptionsStringListSerializer().deserialize(languageString)
        Truth.assertThat(deserializedModuleIdOptions).isEqualTo(expectedList)
    }
}
