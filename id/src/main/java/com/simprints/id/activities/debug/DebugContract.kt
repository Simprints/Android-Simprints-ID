package com.simprints.id.activities.debug

import androidx.lifecycle.MutableLiveData
import com.simprints.id.activities.debug.DebugActivity


interface DebugContract {

    interface Presenter {

        val stateLiveData: MutableLiveData<DebugActivity.State>
        fun refresh()
    }
}
