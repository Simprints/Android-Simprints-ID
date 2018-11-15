package com.simprints.id.tools.serializers

class ModuleIdOptionsStringListSerializer : Serializer<List<String>> {

    override fun serialize(value: List<String>): String = StringBuilder().apply {
        value.forEachIndexed { index, language ->
            append(language)
            if (index != value.size - 1) {
                append(delimiter)
            }
        }
    }.toString()

    override fun deserialize(string: String): List<String> =
        string
            .split(delimiter)
            .filter { it.isNotEmpty() }

    companion object {
        const val delimiter = "|"
    }
}
