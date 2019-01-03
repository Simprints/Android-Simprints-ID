package com.simprints.clientapi.clientrequests.requests.legacy

import com.simprints.clientapi.clientrequests.requests.ClientRequest


interface LegacyClientRequest : ClientRequest {

    val legacyApiKey: String

}
