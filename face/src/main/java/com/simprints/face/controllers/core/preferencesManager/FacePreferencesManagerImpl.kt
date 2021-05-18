package com.simprints.face.controllers.core.preferencesManager

import com.simprints.core.sharedpreferences.PreferencesManager

class FacePreferencesManagerImpl(private val prefs: PreferencesManager) : FacePreferencesManager {
    override var maxRetries: Int = prefs.faceMaxRetries
        set(value) {
            field = value
            prefs.faceMaxRetries = field
        }

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
