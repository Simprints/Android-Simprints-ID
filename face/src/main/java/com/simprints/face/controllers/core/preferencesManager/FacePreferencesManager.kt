package com.simprints.face.controllers.core.preferencesManager

interface FacePreferencesManager {

    /**
     * What is the minimum quality for a picture to be deemed as a good image
     */
    var qualityThreshold: Float


    /**
     * This option is here for projects we run in co-sync mode. The value is set on vulcan/remote config
     * to determine this. Co-sync projects will mostly have this off
     */
    var shouldSaveFaceImages: Boolean
}
