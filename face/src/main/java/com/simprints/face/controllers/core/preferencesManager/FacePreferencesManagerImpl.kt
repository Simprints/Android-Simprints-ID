package com.simprints.face.controllers.core.preferencesManager

import com.simprints.id.data.prefs.IdPreferencesManager

class FacePreferencesManagerImpl(private val prefs: IdPreferencesManager) : FacePreferencesManager {

    override var qualityThreshold: Float = prefs.faceQualityThreshold
        set(value) {
            field = value
            prefs.faceQualityThreshold = field
        }

    override var shouldSaveFaceImages: Boolean = prefs.shouldSaveFaceImages
        set(value) {
            field = value
            prefs.shouldSaveFaceImages = field
        }
}
