package com.simprints.id.exceptions.safe.data.db

import com.simprints.id.exceptions.safe.SimprintsException


class NoStoredLastSyncedInfo(message: String = "NoStoredLastSyncedInfo") : SimprintsException(message)
