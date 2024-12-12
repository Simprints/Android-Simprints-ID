package com.simprints.feature.clientapi.mappers.request.extractors.odk

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.libsimprints.Constants
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OdkVerifyExtractorTest {
    @Test
    fun `should not include acceptableExtras in unknownExtras`() {
        val extras = mapOf(
            Constants.SIMPRINTS_PROJECT_ID to "projectId",
            Constants.SIMPRINTS_USER_ID to "userId",
            Constants.SIMPRINTS_MODULE_ID to "moduleId",
            Constants.SIMPRINTS_METADATA to "metadata",
            "key-a" to "value-a",
            "key-b" to "value-b",
        )

        val acceptableExtras = listOf("key-a", "key-b")
        val extractor = OdkEnrolRequestExtractor(extras, acceptableExtras)

        val unknownExtras = extractor.getUnknownExtras()

        assertThat(unknownExtras).isEmpty()
    }
}
