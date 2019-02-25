package com.simprints.id.domain.requests

interface IdBaseRequest {
    val projectId: String
}

interface IdRequestAction {
    val userId: String
    val moduleId: String
    val metadata: String
}

interface IdRequest: IdBaseRequest, IdRequestAction

