package com.simprints.id.data.db.project.local

import com.simprints.id.data.db.project.domain.Project
import com.simprints.id.exceptions.safe.data.db.NoSuchStoredProjectException

interface ProjectLocalDataSource {

    suspend fun save(project: Project)

    /** @throws NoSuchStoredProjectException */
    suspend fun load(projectId: String): Project
}
