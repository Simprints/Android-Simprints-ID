package com.simprints.face.infra.basebiosdk.detection

import android.graphics.Bitmap
import android.graphics.Rect
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports

/**
 * Chooses which of the faces in an image a template should be extracted for.
 *
 * Receives every face the detector found, as bounding boxes in source-image pixels and in the
 * order the SDK reported them, and returns the index to extract or null to extract none.
 *
 * This exists so the selection policy stays with the caller - the SDK has no way of knowing which
 * person in the frame is the subject - while the expensive template extraction still runs only
 * once, for the face that was chosen.
 */
typealias FaceSelector = (faces: List<Rect>) -> Int?

@ExcludedFromGeneratedTestCoverageReports("No need to test the interface")
interface FaceDetector {
    /**
     * Analyze an ARGB_8888 bitmap and return the detected face data
     *
     * @param bitmap ARGB_8888 formatted
     * @param estimateAgeAndGender whether to also request age/gender estimation. This is extra native
     * processing on top of face detection/template extraction, so it should only be requested for
     * the final selected capture, not on every live-preview frame.
     * @param selectFace decides which face to extract when the image holds more than one. Defaults
     * to the first face the SDK reports, which is all that can be done without a caller policy.
     * Extraction runs for the selected face alone, so the returned [Face] always describes exactly
     * one person.
     * @return Face object, or null if no face was detected or [selectFace] chose none
     */
    fun analyze(
        bitmap: Bitmap,
        estimateAgeAndGender: Boolean = false,
        selectFace: FaceSelector? = null,
    ): Face?

    /**
     * Perform a spoof check on an ARGB_8888 bitmap
     *
     * @param bitmap original captured image (ARGB_8888)
     * @return Either a spoof score (lower is better) or a reason why the check was skipped
     */
    fun spoofCheck(
        bitmap: Bitmap,
        configuredMaxSize: Int,
        selectFace: FaceSelector? = null,
    ): SpoofCheckResult
}
