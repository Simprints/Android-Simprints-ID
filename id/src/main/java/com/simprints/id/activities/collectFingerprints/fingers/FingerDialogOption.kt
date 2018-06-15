package com.simprints.id.activities.collectFingerprints.fingers

import com.simprints.libsimprints.FingerIdentifier

data class FingerDialogOption(var name: String, val fingerId: FingerIdentifier?, val required: Boolean, var active: Boolean)
