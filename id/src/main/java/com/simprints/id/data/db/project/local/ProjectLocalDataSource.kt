package com.simprints.id.data.db.project.local

import com.simprints.id.data.db.project.domain.Project
import com.simprints.id.exceptions.safe.data.db.NoSuchStoredProjectException

interface ProjectLocalDataSource {

    fun save(project: Project)

    /** @throws NoSuchStoredProjectException */
    fun load(projectId: String): Project
}
