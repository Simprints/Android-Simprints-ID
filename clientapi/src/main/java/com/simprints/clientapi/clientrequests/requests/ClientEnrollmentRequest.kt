package com.simprints.clientapi.clientrequests.requests


data class ClientEnrollmentRequest(val projectId: String?,
                                   val apiKey: String?,
                                   val moduleId: String?,
                                   val userId: String?,
                                   val metadata: String?)
