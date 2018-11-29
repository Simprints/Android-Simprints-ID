package com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers.understudy

import androidx.work.Worker

/**
 * Tris - Worker to execute sync for (p, u, m) using DownCountTask.
 * Invocated by DownSyncWorker
 */
class SubDownSyncWorker : Worker()
