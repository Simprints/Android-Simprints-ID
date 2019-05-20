package com.simprints.clientapi.exceptions

sealed class ClientApiSafeException(message: String = "") : RuntimeException(message)
sealed class InvalidRequestException(message: String = "") : ClientApiSafeException(message)

class InvalidClientRequestException(message: String = "") : InvalidRequestException(message)
class InvalidMetadataException(message: String = "") : InvalidRequestException(message)
class InvalidModuleIdException(message: String = "") : InvalidRequestException(message)
class InvalidProjectIdException(message: String = "") : InvalidRequestException(message)
class InvalidSelectedIdException(message: String = "") : InvalidRequestException(message)
class InvalidSessionIdException(message: String = "") : InvalidRequestException(message)
class InvalidUserIdException(message: String = "") : InvalidRequestException(message)
class InvalidVerifyIdException(message: String = "") : InvalidRequestException(message)
