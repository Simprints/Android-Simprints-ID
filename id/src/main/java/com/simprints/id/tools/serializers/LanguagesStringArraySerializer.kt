package com.simprints.id.tools.serializers

class LanguagesStringArraySerializer : Serializer<Array<String>> {

    override fun serialize(value: Array<String>): String = StringBuilder().apply {
        value.forEachIndexed { index, language ->
            append(language)
            if (index != value.size - 1) {
                append(",")
            }
        }
    }.toString()

    override fun deserialize(string: String): Array<String> =
        string.replace(" ", "").replace("\n", "").replace("\r", "")
            .split(",")
            .filter { it.isNotEmpty() }
            .toTypedArray()
}
