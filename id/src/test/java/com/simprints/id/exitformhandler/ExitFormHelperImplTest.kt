package com.simprints.id.exitformhandler

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.simprints.id.activities.coreexitform.result.CoreExitFormActivityResult
import com.simprints.id.activities.faceexitform.result.FaceExitFormActivityResult
import com.simprints.id.activities.fingerprintexitform.result.FingerprintExitFormActivityResult
import com.simprints.id.data.exitform.CoreExitFormReason
import com.simprints.id.data.exitform.FaceExitFormReason
import com.simprints.id.data.exitform.FingerprintExitFormReason
import com.simprints.id.orchestrator.steps.CoreStepProcessorImplTest
import com.simprints.id.orchestrator.steps.core.response.CoreExitFormResponse
import com.simprints.id.orchestrator.steps.core.response.CoreFaceExitFormResponse
import com.simprints.id.orchestrator.steps.core.response.CoreFingerprintExitFormResponse
import com.simprints.id.testtools.TestApplication
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class ExitFormHelperImplTest {

    private lateinit var exitFormHelper: ExitFormHelper

    @Before
    fun setUp() {
        exitFormHelper = ExitFormHelperImpl()
    }

    @Test
    fun `test buildExitFormResponseForCore should return null if data is null`() {
        // Given
        val exitFormData = null
        // When
        val result = exitFormHelper.buildExitFormResponseForCore(exitFormData)
        // Then
        Truth.assertThat(result).isNull()
    }

    @Test
    fun `test buildExitFormResponseForCore should return null if action is go_back for type CORE_EXIT_FORM`() {
        // Given
        val exitFormData = buildCoreExitFormData(CoreExitFormActivityResult.Action.GO_BACK)
        // When
        val result = exitFormHelper.buildExitFormResponseForCore(exitFormData)
        // Then
        Truth.assertThat(result).isNull()
    }

    @Test
    fun `test buildExitFormResponseForCore should return CoreExitFormResponse if action is SUBMIT for type CORE_EXIT_FORM`() {
        // Given
        val exitFormData = buildCoreExitFormData(CoreExitFormActivityResult.Action.SUBMIT)
        // When
        val result = exitFormHelper.buildExitFormResponseForCore(exitFormData)
        Truth.assertThat(result).isNotNull()
        Truth.assertThat((result as CoreExitFormResponse).reason)
            .isEqualTo(CoreExitFormReason.OTHER)
    }

    @Test
    fun `test buildExitFormResponseForCore should return null if action is SCAN_FINGERPRINTS for type CORE_FINGERPRINT_EXIT_FROM`() {
        // Given
        val exitFormData =
            buildFingerprintExitFormData(FingerprintExitFormActivityResult.Action.SCAN_FINGERPRINTS)
        // When
        val result = exitFormHelper.buildExitFormResponseForCore(exitFormData)
        Truth.assertThat(result).isNull()
    }
    @Test
    fun `test buildExitFormResponseForCore should return null if action is null`() {
        // Given
        val exitFormData =
            buildFingerprintExitFormData(null)
        // When
        val result = exitFormHelper.buildExitFormResponseForCore(exitFormData)
        Truth.assertThat(result).isNull()
    }

    @Test
    fun `test buildExitFormResponseForCore should return CoreExitFormResponse if action is SUBMIT for type CORE_FINGERPRINT_EXIT_FROM`() {
        // Given
        val exitFormData =
            buildFingerprintExitFormData(FingerprintExitFormActivityResult.Action.SUBMIT)
        // When
        val result = exitFormHelper.buildExitFormResponseForCore(exitFormData)
        Truth.assertThat(result).isNotNull()
        Truth.assertThat((result as CoreFingerprintExitFormResponse).reason)
            .isEqualTo(FingerprintExitFormReason.OTHER)
    }


    @Test
    fun `test buildExitFormResponseForCore should return null if action is go_back for type CORE_FACE_EXIT_FORM`() {
        // Given
        val exitFormData = buildFaceExitFormData(FaceExitFormActivityResult.Action.GO_BACK)
        // When
        val result = exitFormHelper.buildExitFormResponseForCore(exitFormData)
        Truth.assertThat(result).isNull()
    }

    @Test
    fun `test buildExitFormResponseForCore should return CoreExitFormResponse if action is SUBMIT for type CORE_FACE_EXIT_FORM`() {
        // Given
        val exitFormData = buildFaceExitFormData(FaceExitFormActivityResult.Action.SUBMIT)
        // When
        val result = exitFormHelper.buildExitFormResponseForCore(exitFormData)
        Truth.assertThat(result).isNotNull()
        Truth.assertThat((result as CoreFaceExitFormResponse).reason)
            .isEqualTo(FaceExitFormReason.OTHER)
    }

    private fun buildCoreExitFormData(action: CoreExitFormActivityResult.Action): Intent {
        return Intent().apply {

            putExtra(
                CoreStepProcessorImplTest.CORE_STEP_BUNDLE, CoreExitFormResponse(
                    CoreExitFormReason.OTHER, "optional_text"
                )
            )
            putExtra(
                ExitFormResult.EXIT_FORM_BUNDLE_KEY, CoreExitFormActivityResult(
                    action,
                    CoreExitFormActivityResult.Answer(reason = CoreExitFormReason.OTHER)
                )
            )
        }
    }

    private fun buildFingerprintExitFormData(action: FingerprintExitFormActivityResult.Action?)
        : Intent {
        return Intent().apply {
            putExtra(
                CoreStepProcessorImplTest.CORE_STEP_BUNDLE, CoreExitFormResponse(
                    CoreExitFormReason.OTHER, "optional_text"
                )
            )
            action?.let {

                putExtra(
                    ExitFormResult.EXIT_FORM_BUNDLE_KEY, FingerprintExitFormActivityResult(
                        action,
                        FingerprintExitFormActivityResult.Answer()
                    )
                )
            }
        }

    }

    private fun buildFaceExitFormData(action: FaceExitFormActivityResult.Action): Intent {

        return Intent().apply {

            putExtra(
                CoreStepProcessorImplTest.CORE_STEP_BUNDLE, CoreExitFormResponse(
                    CoreExitFormReason.OTHER, "optional_text"
                )
            )
            putExtra(
                ExitFormResult.EXIT_FORM_BUNDLE_KEY, FaceExitFormActivityResult(
                    action,
                    FaceExitFormActivityResult.Answer()
                )
            )
        }
    }
}
