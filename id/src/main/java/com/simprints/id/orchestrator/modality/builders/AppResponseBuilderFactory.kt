package com.simprints.id.orchestrator.modals.builders

import com.simprints.id.domain.modal.Modality

class AppResponseBuilderFactoryImpl : AppResponseBuilderFactory {

    override fun buildAppResponseBuilder(modality: Modality): AppResponseBuilderForModal =
        when (modality) {
            Modality.FACE -> AppResponseBuilderForFace()
            Modality.FINGER -> AppResponseBuilderForFinger()
            Modality.FINGER_FACE -> AppResponseBuilderForFingerFace()
            Modality.FACE_FINGER -> AppResponseBuilderForFaceFinger()
        }
}

interface AppResponseBuilderFactory {
    fun buildAppResponseBuilder(modality: Modality): AppResponseBuilderForModal
}
