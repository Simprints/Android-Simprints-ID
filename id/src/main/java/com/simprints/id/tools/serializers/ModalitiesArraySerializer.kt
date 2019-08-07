package com.simprints.id.tools.serializers

import com.simprints.id.domain.modality.Modality

// TODO : This should probably be a List<String> instead of Array<String>
class ModalitiesArraySerializer : Serializer<List<Modality>> {

    override fun serialize(value: List<Modality>): String = StringBuilder().apply {
        value.forEachIndexed { index, language ->
            append(language)
            if (index != value.size - 1) {
                append(delimiter)
            }
        }
    }.toString()

    override fun deserialize(string: String): List<Modality> =
        string.replace(" ", "").replace("\n", "").replace("\r", "")
            .split(delimiter)
            .filter { it.isNotEmpty() }
            .map {
                when (it) {
                    "FACE" -> Modality.FACE
                    "FINGER" -> Modality.FINGER
                    else -> null
                }
            }.filterNotNull()

    companion object {
        const val delimiter = ","
    }
}
