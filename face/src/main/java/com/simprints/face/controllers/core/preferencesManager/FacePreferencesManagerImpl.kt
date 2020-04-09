package com.simprints.face.controllers.core.preferencesManager

import com.simprints.id.data.prefs.PreferencesManager

// TODO: get correct value from PreferencesManager
class FacePreferencesManagerImpl(private val prefs: PreferencesManager) : FacePreferencesManager {
    override var maxRetries: Int = 2

    override var qualityThreshold: Float = -1f
}
