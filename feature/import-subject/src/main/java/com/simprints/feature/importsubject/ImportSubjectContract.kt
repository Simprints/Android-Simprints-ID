package com.simprints.feature.importsubject

import com.simprints.feature.importsubject.screen.ImportSubjectFragmentArgs

object ImportSubjectContract {

    val DESTINATION = R.id.importSubjectFragment

    fun getArgs(projectId: String, subjectId: String, image64: String) =
        ImportSubjectFragmentArgs(projectId, subjectId, image64).toBundle()
}
