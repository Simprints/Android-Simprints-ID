package com.simprints.clientapi.clientrequests.extractors.odk

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.libsimprints.Constants
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OdkEnrolExtractorTest {

    @Test
    fun shouldNotIncludeAcceptableExtrasToUnknownExtras() {
        val intent = Intent().putExtra(Constants.SIMPRINTS_PROJECT_ID, "projectId")
            .putExtra(Constants.SIMPRINTS_USER_ID, "userId")
            .putExtra(Constants.SIMPRINTS_MODULE_ID, "moduleId")
            .putExtra(Constants.SIMPRINTS_METADATA, "metadata")
            .putExtra("key-a", "value-a")
            .putExtra("key-b", "value-b")

        val acceptableExtras = listOf("key-a", "key-b")
        val extractor = OdkEnrolExtractor(intent, acceptableExtras)

        val unknownExtras = extractor.getUnknownExtras()

        assertThat(unknownExtras).isEmpty()
    }

}
