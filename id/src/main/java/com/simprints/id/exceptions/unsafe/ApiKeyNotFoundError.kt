package com.simprints.id.exceptions.unsafe


class ApiKeyNotFoundError(message: String = "ApiKeyNotFoundError") : Error(message)
class ApiKeyNonValid(message: String = "ApiKeyNonValid") : Error(message)
class ProjectKeyNonValid(message: String = "ProjectKeyNonValid") : Error(message)

