package com.simprints.id.exceptions.safe.sync

import com.simprints.id.exceptions.safe.SafeException

class EmptyPeopleOperationsParamsException(message: String = "People params are not provided") :
    SafeException(message)
