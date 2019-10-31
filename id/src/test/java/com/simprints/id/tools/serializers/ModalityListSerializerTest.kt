package com.simprints.id.tools.serializers

import com.google.common.truth.Truth.assertThat
import com.simprints.id.domain.modality.Modality.FACE
import com.simprints.id.domain.modality.Modality.FINGER
import org.junit.Test

class ModalityListSerializerTest {

    @Test
    fun serializingModalityList_isAsExpected() {
        val modalityList = listOf(FINGER, FACE)
        val expectedString = "FINGER,FACE"

        val serializedModalities = ModalitiesListSerializer().serialize(modalityList)

        assertThat(expectedString).isEqualTo(serializedModalities)
    }

    @Test
    fun deserializeModality_isAsExpected() {
        val modalitiesString = "FINGER,FACE"
        val expectedList = listOf(FINGER, FACE)

        val deserializedModalities = ModalitiesListSerializer().deserialize(modalitiesString)
        assertThat(expectedList).isEqualTo(deserializedModalities)
    }
}
