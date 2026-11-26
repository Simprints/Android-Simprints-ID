package com.simprints.feature.dashboard.main.dailyactivity

import com.google.common.truth.Truth.assertThat
import com.simprints.feature.dashboard.main.dailyactivity.DailyActivityUiState.DailyActivityUi
import org.junit.Test

class DailyActivityUiTest {
    @Test
    fun `each has flag is true only when its count is above zero`() {
        assertThat(ui(0, 0, 0).hasEnrolments).isFalse()
        assertThat(ui(1, 0, 0).hasEnrolments).isTrue()
        assertThat(ui(0, 0, 0).hasIdentifications).isFalse()
        assertThat(ui(0, 1, 0).hasIdentifications).isTrue()
        assertThat(ui(0, 0, 0).hasVerifications).isFalse()
        assertThat(ui(0, 0, 1).hasVerifications).isTrue()
    }

    @Test
    fun `has no activity only when every count is zero`() {
        assertThat(ui(0, 0, 0).hasNoActivity).isTrue()
        assertThat(ui(1, 0, 0).hasNoActivity).isFalse()
        assertThat(ui(0, 1, 0).hasNoActivity).isFalse()
        assertThat(ui(0, 0, 1).hasNoActivity).isFalse()
    }

    @Test
    fun `enrolments divider is visible only with enrolments and at least one other count`() {
        assertThat(ui(1, 1, 0).isEnrolmentsDividerVisible).isTrue()
        assertThat(ui(1, 0, 1).isEnrolmentsDividerVisible).isTrue()
        assertThat(ui(1, 1, 1).isEnrolmentsDividerVisible).isTrue()
        assertThat(ui(1, 0, 0).isEnrolmentsDividerVisible).isFalse()
        assertThat(ui(0, 1, 1).isEnrolmentsDividerVisible).isFalse()
        assertThat(ui(0, 0, 0).isEnrolmentsDividerVisible).isFalse()
    }

    @Test
    fun `identifications divider is visible only with identifications and verifications`() {
        assertThat(ui(0, 1, 1).isIdentificationsDividerVisible).isTrue()
        assertThat(ui(1, 1, 1).isIdentificationsDividerVisible).isTrue()
        assertThat(ui(1, 1, 0).isIdentificationsDividerVisible).isFalse()
        assertThat(ui(1, 0, 1).isIdentificationsDividerVisible).isFalse()
        assertThat(ui(0, 1, 0).isIdentificationsDividerVisible).isFalse()
        assertThat(ui(0, 0, 0).isIdentificationsDividerVisible).isFalse()
    }

    private fun ui(
        enrolments: Int,
        identifications: Int,
        verifications: Int,
    ) = DailyActivityUi(enrolments, identifications, verifications, currentDate = "2022-11-15")
}
