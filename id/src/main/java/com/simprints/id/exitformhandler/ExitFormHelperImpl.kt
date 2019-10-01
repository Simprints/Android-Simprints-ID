package com.simprints.id.exitformhandler

import android.content.Intent
import com.simprints.id.activities.coreexitform.CoreExitFormActivity
import com.simprints.id.activities.coreexitform.result.CoreExitFormActivityResult
import com.simprints.id.activities.faceexitform.FaceExitFormActivity
import com.simprints.id.activities.faceexitform.result.FaceExitFormActivityResult
import com.simprints.id.activities.fingerprintexitform.FingerprintExitFormActivity
import com.simprints.id.activities.fingerprintexitform.result.FingerprintExitFormActivityResult
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.core.response.CoreExitFormResponse
import com.simprints.id.domain.moduleapi.core.response.CoreFaceExitFormResponse
import com.simprints.id.domain.moduleapi.core.response.CoreFingerprintExitFormResponse
import com.simprints.id.domain.moduleapi.core.response.CoreResponse
import com.simprints.id.exitformhandler.ExitFormResult.Companion.EXIT_FORM_BUNDLE_KEY
import com.simprints.id.exitformhandler.ExitFormResult.ExitFormType.*

class ExitFormHelperImpl : ExitFormHelper {

    override fun getExitFormActivityClassFromModalities(modalities: List<Modality>): String? =
        if (isSingleModality(modalities)) {
            getModalitySpecificExitFormClass(modalities)
        } else {
            getCoreExitFormClass()
        }

    private fun isSingleModality(modalities: List<Modality>) = modalities.size == 1

    private fun getModalitySpecificExitFormClass(modalities: List<Modality>) =
        when (modalities.first()) {
            Modality.FACE -> FaceExitFormActivity::class.java.canonicalName
            Modality.FINGER -> FingerprintExitFormActivity::class.java.canonicalName
        }

    private fun getCoreExitFormClass() = CoreExitFormActivity::class.java.canonicalName

    override fun buildExitFormResponseForCore(data: Intent?): CoreResponse? =
        data?.getParcelableExtra<ExitFormResult>(EXIT_FORM_BUNDLE_KEY)?.let {
            when (it.type) {
                CORE_EXIT_FORM -> {
                    buildCoreResponseFromActivityResultIfSubmitted(data.getParcelableExtra(EXIT_FORM_BUNDLE_KEY))
                }
                CORE_FINGERPRINT_EXIT_FROM -> {
                    buildFingerprintResponseFromActivityResultIfSubmitted(data.getParcelableExtra(EXIT_FORM_BUNDLE_KEY))
                }
                CORE_FACE_EXIT_FORM -> {
                    buildFaceResponseFromActivityResultIfSubmitted(data.getParcelableExtra(EXIT_FORM_BUNDLE_KEY))
                }
            }
        }

    private fun buildCoreResponseFromActivityResultIfSubmitted(result: CoreExitFormActivityResult) =
        when (result.action) {
            CoreExitFormActivityResult.Action.SUBMIT ->  {
                CoreExitFormResponse(result.answer.reason, result.answer.optionalText)
            }
            CoreExitFormActivityResult.Action.GO_BACK -> null
        }

    private fun buildFingerprintResponseFromActivityResultIfSubmitted(result: FingerprintExitFormActivityResult) =
        when (result.action) {
            FingerprintExitFormActivityResult.Action.SUBMIT -> {
                CoreFingerprintExitFormResponse(result.answer.reason, result.answer.optionalText)
            }
            FingerprintExitFormActivityResult.Action.SCAN_FINGERPRINTS -> null
        }

    private fun buildFaceResponseFromActivityResultIfSubmitted(result: FaceExitFormActivityResult) =
        when (result.action) {
            FaceExitFormActivityResult.Action.SUBMIT -> {
                CoreFaceExitFormResponse(result.answer.reason, result.answer.optionalText)
            }
            FaceExitFormActivityResult.Action.GO_BACK -> null
        }
}
