package com.simprints.face.controllers.core.preferencesManager

interface FacePreferencesManager {
    /**
     * Maximum number of retries possible after first try
     * E.g. 2 retries will make for 3 chances (0, 1, 2)
     */
    var maxRetries: Int

    /**
     * What is the minimum quality for a picture to be deemed as a good image
     */
    var qualityThreshold: Float

    /**
     * What is the minimum threshold for a picture to be deemed as a match
     */
    var faceMatchThreshold: Float
}
