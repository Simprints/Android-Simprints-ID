package com.simprints.id.activities.debug

import androidx.lifecycle.MutableLiveData


interface DebugContract {

    interface Presenter {

        val stateLiveData: MutableLiveData<DebugActivity.State>
        fun refresh()
    }
}
