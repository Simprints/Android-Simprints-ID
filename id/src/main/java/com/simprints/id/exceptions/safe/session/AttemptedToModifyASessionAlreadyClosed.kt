package com.simprints.id.exceptions.safe.session

import com.simprints.id.exceptions.safe.SimprintsException

class AttemptedToModifyASessionAlreadyClosed(message: String = "AttemptedToModifyASessionAlreadyClosed") : SimprintsException(message)
