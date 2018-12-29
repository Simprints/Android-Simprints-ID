package com.simprints.clientapi.clientrequests.requests


abstract class ClientRequest(open val projectId: String,
                             open val moduleId: String,
                             open val userId: String,
                             open val metadata: String?) {

    abstract val apiVersion: ApiVersion

}
