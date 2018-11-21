package com.simprints.id.tools.serializers

class ModuleIdOptionsStringSetSerializer : Serializer<Set<String>> {

    override fun serialize(value: Set<String>): String = StringBuilder().apply {
        value.forEachIndexed { index, language ->
            append(language)
            if (index != value.size - 1) {
                append(delimiter)
            }
        }
    }.toString()

    override fun deserialize(string: String): Set<String> =
        string
            .split(delimiter)
            .filter { it.isNotEmpty() }
            .toSet()

    companion object {
        const val delimiter = "|"
    }
}
