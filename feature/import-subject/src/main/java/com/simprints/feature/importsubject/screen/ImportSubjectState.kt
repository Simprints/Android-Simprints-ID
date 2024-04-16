package com.simprints.feature.importsubject.screen

import android.graphics.Bitmap

internal sealed class ImportSubjectState {

    data class Imported(val bitmap: Bitmap) : ImportSubjectState()
    data class Error(val reason: String) : ImportSubjectState()
    data object Complete : ImportSubjectState()
}
