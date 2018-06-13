package com.simprints.id.activities.collectFingerprints

import android.support.annotation.DrawableRes
import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView


interface CollectFingerprintsContract {

    interface View : BaseView<Presenter> {

        // Sync
        fun setSyncItem(enabled: Boolean, title: String, @DrawableRes icon: Int)
    }

    interface Presenter: BasePresenter {

        // Lifecycle
        fun handleOnPause()

        // Sync
        fun handleSyncPressed()
    }
}
