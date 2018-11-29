package com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers

import androidx.work.Worker

/**
 * Fabio - Sync Worker: Worker to chain CountWorker and DownSyncWorker
 * passing SyncParams as Input of the CountWorker.
 */
class SyncWorker : Worker()
