package com.simprints.id.activities.about

import androidx.lifecycle.MutableLiveData
import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView


interface DebugContract {

    interface Presenter {

        val stateLiveData: MutableLiveData<DebugActivity.State>
        fun refresh()
    }
}
