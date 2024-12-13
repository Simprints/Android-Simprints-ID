package com.simprints.infra.authstore.exceptions

class RemoteDbNotSignedInException(
    message: String = "RemoteDbNotSignedInException",
) : RuntimeException(message)
