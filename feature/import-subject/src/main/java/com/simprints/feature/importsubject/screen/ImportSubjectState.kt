package com.simprints.feature.importsubject.screen

internal sealed class ImportSubjectState {

    data object Imported : ImportSubjectState()
    data object Error : ImportSubjectState()

}
