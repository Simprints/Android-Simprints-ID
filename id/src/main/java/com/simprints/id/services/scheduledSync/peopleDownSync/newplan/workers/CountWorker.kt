
package com.simprints.id.services.scheduledSync.peopleDownSync.workers

/**
 * Fabio - Worker to fetch all counters for SyncParams(p, u, arrayOf(m))
 * I: SyncParams
 * O: SyncParams
 * Two possible approaches:
 * a) Use RxJava to execute multiple CountTasks(p, u, m)
 * OR
 * b) zip SubCountWorkers to fetch counter for each (p, u, m)
 */
//class CountWorker : Worker() {
//
//    companion object {
//        const val COUNT_WORKER_TAG = "COUNT_WORKER_TAG"
//        private const val COUNT_WORKER_CHAIN = "COUNT_WORKER_CHAIN"
//    }
//
//    override fun doWork(): Result {
//        val subCountWorkersChainFactory = SubCountWorkerChainFactory()
//        val input = inputData.getString(SyncWorker.SYNC_WORKER_SYNC_SCOPE_INPUT) ?: throw IllegalArgumentException("input required")
//        val scope = SyncScope.fromJson(Gson(), input)  ?: throw IllegalArgumentException("SyncScope required")
//        val countWorkers = subCountWorkersChainFactory.buildChainOfSubCountWorker(scope)
//
//        WorkManager.getInstance()
//            .beginUniqueWork(COUNT_WORKER_CHAIN, ExistingWorkPolicy.KEEP, countWorkers)
//            .then(subCountWorkersChainFactory.buildInputMergerWorker())
//            .enqueue()
//
//        return Result.SUCCESS.also {
//            Timber.d("WM - CountWorker($scope): $it")
//        }
//    }
//}
