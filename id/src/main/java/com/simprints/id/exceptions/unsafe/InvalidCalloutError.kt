package com.simprints.id.exceptions.unsafe

import com.simprints.id.domain.ALERT_TYPE

class InvalidCalloutError(val alertType: ALERT_TYPE): Error(alertType.name)
