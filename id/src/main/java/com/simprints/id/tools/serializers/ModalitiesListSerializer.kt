package com.simprints.id.tools.serializers

import com.simprints.id.domain.modality.Modality

class ModalitiesListSerializer : Serializer<List<Modality>> {

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
            .mapNotNull {
                when (it) {
                    FACE_MODALITY -> Modality.FACE
                    FINGER_MODALITY -> Modality.FINGER
                    else -> null
                }
            }

    companion object {
        const val delimiter = ","
        private const val FACE_MODALITY = "FACE"
        private const val FINGER_MODALITY = "FINGER"
    }
}
