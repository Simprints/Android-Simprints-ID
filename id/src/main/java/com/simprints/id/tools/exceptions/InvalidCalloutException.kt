package com.simprints.id.tools.exceptions

import com.simprints.id.model.ALERT_TYPE

class InvalidCalloutException(val alertType: ALERT_TYPE): Exception(alertType.name)