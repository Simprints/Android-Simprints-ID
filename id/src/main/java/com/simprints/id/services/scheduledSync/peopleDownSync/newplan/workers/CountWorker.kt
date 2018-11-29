package com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers

import androidx.work.Worker

/**
 * Fabio - Worker to fetch all counters for SyncParams(p, u, arrayOf(m))
 * I: SyncParams
 * O: SyncParams
 * Two possible approaches:
 * a) Use RxJava to execute multiple CountTasks(p, u, m)
 * OR
 * b) zip SubCountWorkers to fetch counter for each (p, u, m)
 */
class CountWorker : Worker()
