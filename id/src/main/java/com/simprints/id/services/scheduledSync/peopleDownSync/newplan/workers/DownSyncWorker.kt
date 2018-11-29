package com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers

import androidx.work.Worker

/**
 * Fabio - Worker to execute syncs for SyncParams(p, u, arrayOf(m))
 * I: SyncParams
 * O: SyncParams
 * Two possible approaches:
 * a) Use RxJava to execute multiple DownSyncTasks(p, u, m)
 * OR
 * b) zip SubDownSyncWorkers to do the sync for each (p, u, m)
 */
class DownSyncWorker : Worker()
