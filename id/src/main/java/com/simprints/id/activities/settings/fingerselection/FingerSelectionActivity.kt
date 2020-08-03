package com.simprints.id.activities.settings.fingerselection

import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.id.Application
import com.simprints.id.R
import kotlinx.android.synthetic.main.activity_finger_selection.*
import javax.inject.Inject

class FingerSelectionActivity : BaseSplitActivity() {

    @Inject
    lateinit var viewModelFactory: FingerSelectionViewModelFactory
    private lateinit var viewModel: FingerSelectionViewModel

    private lateinit var fingerSelectionAdapter: FingerSelectionItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as Application).component.inject(this)
        setContentView(R.layout.activity_finger_selection)

        configureToolbar()

        viewModel = ViewModelProvider(this, viewModelFactory).get(FingerSelectionViewModel::class.java)

        initRecyclerView()
        initAddFingerButton()
        initResetButton()

        viewModel.items.observe(this, Observer { fingerSelectionAdapter.notifyDataSetChanged() })

        viewModel.start()
    }

    private fun configureToolbar() {
        setSupportActionBar(settingsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.finger_selection_activity_title)
    }

    private fun initRecyclerView() {
        fingerSelectionAdapter = FingerSelectionItemAdapter(this, viewModel)
        fingerSelectionRecyclerView.layoutManager = LinearLayoutManager(this)
        fingerSelectionRecyclerView.adapter = fingerSelectionAdapter
    }

    private fun initAddFingerButton() {
        addFingerButton.setOnClickListener { viewModel.addNewFinger() }
    }

    private fun initResetButton() {
        resetButton.setOnClickListener { viewModel.resetFingerItems() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
