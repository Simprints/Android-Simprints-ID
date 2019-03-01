package com.simprints.clientapi.models.domain

enum class ApiVersion {

    /**
     * V1 API is the first iteration of LibSimprints which used an API key + contained patient
     * update.
     */
    V1,

    /**
     * V2 API is the second iteration of LibSimprints, frequently referred to as the Auth 2.0
     * release. This version contains a project ID instead of an API key.
     */
    V2

}
