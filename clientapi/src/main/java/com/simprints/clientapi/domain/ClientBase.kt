package com.simprints.clientapi.domain

import com.simprints.clientapi.domain.requests.ExtraRequestInfo

interface ClientBase {

    val projectId: String
    val extra: ExtraRequestInfo?
}
