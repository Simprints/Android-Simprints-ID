package com.simprints.id.orchestrator.modality.builders

import com.simprints.id.domain.modality.Modality

class AppResponseFactoryImpl : AppResponseFactory {

    override fun buildAppResponseBuilder(modality: Modality): AppResponseBuilderForModal =
        when (modality) {
            Modality.FACE -> AppResponseBuilderForFace()
            Modality.FINGER -> AppResponseBuilderForFinger()
            Modality.FINGER_FACE -> AppResponseBuilderForFingerFace()
            Modality.FACE_FINGER -> AppResponseBuilderForFaceFinger()
        }
}

interface AppResponseFactory {
    fun buildAppResponseBuilder(modality: Modality): AppResponseBuilderForModal
}
