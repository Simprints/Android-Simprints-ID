package com.simprints.id.exceptions.safe.callout

import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.exceptions.safe.SafeException

class InvalidCalloutError(val alertType: ALERT_TYPE): SafeException(alertType.name)
