package com.simprints.clientapi.domain


interface ClientBase {

    val projectId: String

    val unknownExtras: Map<String, Any?>

}
