package com.simprints.id.exceptions.safe.callout

import com.simprints.id.domain.alert.Alert
import com.simprints.id.exceptions.safe.SafeException

class InvalidCalloutError(val alert: Alert): SafeException(alert.name)
