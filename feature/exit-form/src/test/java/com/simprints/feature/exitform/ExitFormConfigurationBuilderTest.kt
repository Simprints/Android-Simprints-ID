package com.simprints.feature.exitform

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.exitform.config.ExitFormOption
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExitFormConfigurationBuilderTest {
    @Test
    fun `default config does not include ScannerNotWorking`() {
        assertThat(exitFormConfiguration { }.visibleOptions).doesNotContain(ExitFormOption.ScannerNotWorking)
    }

    @Test
    fun `scanner config does not include AppNotWorking`() {
        assertThat(scannerOptions()).doesNotContain(ExitFormOption.AppNotWorking)
    }
}
