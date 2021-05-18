package com.simprints.id.activities.fetchguid

import com.simprints.eventsystem.SubjectFetchResult
import kotlinx.coroutines.CoroutineScope

interface FetchGuidHelper {

    suspend fun loadFromRemoteIfNeeded(coroutineScope: CoroutineScope, projectId: String, subjectId: String): SubjectFetchResult

}
