package com.simprints.feature.importsubject

import com.simprints.feature.importsubject.screen.ImportSubjectFragmentArgs

object ImportSubjectContract {

    val DESTINATION = R.id.importSubjectFragment

    fun getArgs(projectId: String, subjectId: String, uri: String) =
        ImportSubjectFragmentArgs(projectId, subjectId, uri).toBundle()
}
