package com.simprints.face.controllers.core.preferencesManager

import com.simprints.id.data.prefs.PreferencesManager

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
}
