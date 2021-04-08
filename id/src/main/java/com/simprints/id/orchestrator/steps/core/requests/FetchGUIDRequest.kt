package com.simprints.id.orchestrator.steps.core.requests

import kotlinx.parcelize.Parcelize

@Parcelize
class FetchGUIDRequest(val projectId: String, val verifyGuid: String): CoreRequest
