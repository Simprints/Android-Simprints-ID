package com.simprints.clientapi.requests

data class LegcayEnrollmentRequest(val projectId: String,
                                   val moduleId: String,
                                   val userId: String,
                                   val callingPackage: String,
                                   val metadata: String)
