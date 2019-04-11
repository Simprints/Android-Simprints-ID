package com.simprints.id.orchestrator.modals.builders

import com.simprints.id.domain.modal.Modal

class AppResponseBuilderFactoryImpl : AppResponseBuilderFactory {

    override fun buildAppResponseBuilder(modal: Modal): AppResponseBuilderForModal =
        when (modal) {
            Modal.FACE -> AppResponseBuilderForFace()
            Modal.FINGER -> AppResponseBuilderForFinger()
            Modal.FINGER_FACE -> AppResponseBuilderForFingerFace()
            Modal.FACE_FINGER -> AppResponseBuilderForFaceFinger()
        }
}

interface AppResponseBuilderFactory {
    fun buildAppResponseBuilder(modal: Modal): AppResponseBuilderForModal
}
