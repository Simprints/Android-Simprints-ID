package com.simprints.id.tools.serializers

import com.simprints.id.domain.modality.Modality

class ModalitiesListSerializer : Serializer<List<Modality>> {

    override fun serialize(value: List<Modality>): String = value.joinToString(separator = separator)

    override fun deserialize(string: String): List<Modality> =
        string.replace(Regex("[ \n\r\t]"), "")
            .split(separator)
            .filter { it.isNotEmpty() }
            .mapNotNull {
                when (it) {
                    FACE_MODALITY -> Modality.FACE
                    FINGER_MODALITY -> Modality.FINGER
                    else -> null
                }
            }

    companion object {
        private const val separator = ","
        private const val FACE_MODALITY = "FACE"
        private const val FINGER_MODALITY = "FINGER"
    }
}
