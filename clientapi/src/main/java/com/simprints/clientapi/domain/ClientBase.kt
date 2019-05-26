package com.simprints.clientapi.domain

import com.simprints.clientapi.domain.requests.ExtraRequestInfo


interface ClientBase {

    val projectId: String

    val unknownExtras: Map<String, Any?>

    val extra: ExtraRequestInfo?

}
