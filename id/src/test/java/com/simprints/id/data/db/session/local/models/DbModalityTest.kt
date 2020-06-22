package com.simprints.id.data.db.session.local.models

import com.google.common.truth.Truth.assertThat
import com.simprints.id.domain.modality.Modality
import org.junit.Test

class DbModalityTest {

    @Test
    fun shouldBuildDbModalityWithDomainInstance() {
        val domain = Modality.FINGER
        val db = DbModality(domain)

        assertThat(db.name).isEqualTo(DbModality.MODALITY_FINGER)
    }

    @Test
    fun shouldConvertDbModalityToDomain() {
        val db = DbModality(Modality.FACE)
        val expected = Modality.FACE

        val actual = db.toDomain()

        assertThat(actual).isEqualTo(expected)
    }

}
