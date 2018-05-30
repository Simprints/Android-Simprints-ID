package com.simprints.id.activities.dashboard.views

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import timber.log.Timber

//known RecyclerView's bug https://stackoverflow.com/questions/35653439/recycler-view-inconsistency-detected-invalid-view-holder-adapter-positionviewh/44590192
class WrapContentLinearLayoutManager(context: Context?) : LinearLayoutManager(context) {

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            //TODO: send crashlytics event.
            Timber.e("IndexOutOfBoundsException in RecyclerView happens")
        }
    }
}
