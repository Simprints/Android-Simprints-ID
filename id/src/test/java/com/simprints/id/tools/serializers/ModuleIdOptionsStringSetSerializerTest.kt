package com.simprints.id.tools.serializers

import com.google.common.truth.Truth
import org.junit.Test

class ModuleIdOptionsStringSetSerializerTest {

    @Test
    fun serializingModuleIdOptionsSet_isAsExpected() {
        val moduleIdOptionsSet = setOf("module1", "module2", "module3", "module4", "module5")
        val expectedString = "module1|module2|module3|module4|module5"
        val serializedModuleIdOptions = ModuleIdOptionsStringSetSerializer().serialize(moduleIdOptionsSet)
        Truth.assertThat(serializedModuleIdOptions).isEqualTo(expectedString)
    }

    @Test
    fun serializingModuleIdOptionsSet_fromEmptySetWorks() {
        val moduleIdOptionsSet = setOf<String>()
        val expectedString = ""
        val serializedModuleIdOptions = ModuleIdOptionsStringSetSerializer().serialize(moduleIdOptionsSet)
        Truth.assertThat(serializedModuleIdOptions).isEqualTo(expectedString)
    }

    @Test
    fun serializingModuleIdOptionsSet_withDuplicateIds_filtersDuplicates() {
        val moduleIdOptionsSet = setOf("module1", "module2", "module3", "module4", "module4", "module5", "module5")
        val expectedString = "module1|module2|module3|module4|module5"
        val serializedModuleIdOptions = ModuleIdOptionsStringSetSerializer().serialize(moduleIdOptionsSet)
        Truth.assertThat(serializedModuleIdOptions).isEqualTo(expectedString)
    }

    @Test
    fun deserializeModuleIdOptionsString_isAsExpected() {
        val languageString = "module1|module2|module3|module4|module5"
        val expectedSet = setOf("module1", "module2", "module3", "module4", "module5")
        val deserializedModuleIdOptions = ModuleIdOptionsStringSetSerializer().deserialize(languageString)
        Truth.assertThat(deserializedModuleIdOptions).isEqualTo(expectedSet)
    }

    @Test
    fun deserializeModuleIdOptions_worksEvenWithWhiteSpaceAndExtraPipes() {
        val languageString = "module1  ||m\nodule2\n\r|||module 3|module4|module5\r\r\n "
        val expectedSet = setOf("module1  ", "m\nodule2\n\r", "module 3", "module4", "module5\r\r\n ")
        val deserializedModuleIdOptions = ModuleIdOptionsStringSetSerializer().deserialize(languageString)
        Truth.assertThat(deserializedModuleIdOptions).isEqualTo(expectedSet)
    }

    @Test
    fun deserializeModuleIdOptions_toEmptySetWorks() {
        val languageString = ""
        val expectedSet = setOf<String>()
        val deserializedModuleIdOptions = ModuleIdOptionsStringSetSerializer().deserialize(languageString)
        Truth.assertThat(deserializedModuleIdOptions).isEqualTo(expectedSet)
    }

    @Test
    fun deserializeModuleIdOptions_withDuplicateIds_filtersDuplicates() {
        val languageString = "module1|module1|module1|module2|module3|module3|module4|module5|module5"
        val expectedSet = setOf("module1", "module2", "module3", "module4", "module5")
        val deserializedModuleIdOptions = ModuleIdOptionsStringSetSerializer().deserialize(languageString)
        Truth.assertThat(deserializedModuleIdOptions).isEqualTo(expectedSet)
    }
}
