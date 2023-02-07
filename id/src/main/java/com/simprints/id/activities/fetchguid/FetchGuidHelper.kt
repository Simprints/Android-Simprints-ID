package com.simprints.id.activities.fetchguid

import com.simprints.id.data.db.SubjectFetchResult

interface FetchGuidHelper {

    suspend fun loadFromRemoteIfNeeded(projectId: String, subjectId: String): SubjectFetchResult

}
