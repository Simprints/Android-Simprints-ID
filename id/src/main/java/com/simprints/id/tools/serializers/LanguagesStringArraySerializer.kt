package com.simprints.id.tools.serializers

// TODO : This should probably be a List<String> instead of Array<String>
class LanguagesStringArraySerializer : Serializer<Array<String>> {

    override fun serialize(value: Array<String>): String = StringBuilder().apply {
        value.forEachIndexed { index, language ->
            append(language)
            if (index != value.size - 1) {
                append(delimiter)
            }
        }
    }.toString()

    override fun deserialize(string: String): Array<String> =
        string.replace(" ", "").replace("\n", "").replace("\r", "")
            .split(delimiter)
            .filter { it.isNotEmpty() }
            .toTypedArray()

    companion object {
        const val delimiter = ","
    }
}
