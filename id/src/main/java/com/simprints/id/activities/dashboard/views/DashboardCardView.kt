package com.simprints.id.activities.dashboard.views

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.*
import androidx.recyclerview.widget.RecyclerView
import com.simprints.id.R
import com.simprints.id.activities.dashboard.viewModels.DashboardCardViewModel

open class DashboardCardView(rootView: View) : RecyclerView.ViewHolder(rootView), LifecycleOwner {

    private val image: ImageView? = rootView.findViewById(R.id.dashboardCardImage)
    private val title: TextView? = rootView.findViewById(R.id.dashboardCardTitle)
    protected val description: TextView? = rootView.findViewById(R.id.dashboardCardDescription)

    private val lifecycleRegistry = LifecycleRegistry(this)

    init {
        lifecycleRegistry.markState(Lifecycle.State.INITIALIZED)
    }

    fun onAppear() {
        lifecycleRegistry.markState(Lifecycle.State.CREATED)
    }

    fun onDisappear() {
        lifecycleRegistry.markState(Lifecycle.State.DESTROYED)
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }


    open fun bind(viewModel: ViewModel) {
        val cardViewModel = viewModel as? DashboardCardViewModel
        cardViewModel?.let {
            it.stateLiveData.observe(this, Observer<DashboardCardViewModel.State> {
                image?.setImageResource(it.imageRes)
                title?.text = it.title
                description?.text = it.description
            })
        }
    }
}
