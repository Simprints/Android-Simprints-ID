package com.simprints.id.activities.collectFingerprints

import android.content.Context
import com.simprints.id.services.sync.SyncService
import javax.inject.Inject


class CollectFingerprintsPresenter(private val context: Context,
                                   private val view: CollectFingerprintsContract.View)
    : CollectFingerprintsContract.Presenter {

    private lateinit var syncHelper: CollectFingerprintsSyncHelper

    override fun start() {
        initSyncClient(context, view)
    }

    private fun initSyncClient(context: Context, view: CollectFingerprintsContract.View) {
        SyncService.getClient(context)
        syncHelper = CollectFingerprintsSyncHelper(context, view)
    }

    override fun handleSyncPressed() {
        syncHelper.sync()
    }

    override fun handleOnPause() {
        syncHelper.syncManager.stopListeners()
    }
}
