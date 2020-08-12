package com.simprints.id.activities.settings.fingerselection

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.tools.extensions.showToast
import kotlinx.android.synthetic.main.activity_finger_selection.*
import javax.inject.Inject

class FingerSelectionActivity : BaseSplitActivity() {

    @Inject
    lateinit var viewModelFactory: FingerSelectionViewModelFactory
    private lateinit var viewModel: FingerSelectionViewModel

    private lateinit var fingerSelectionAdapter: FingerSelectionItemAdapter

    private val itemTouchHelper = ItemTouchHelper(
        object : ItemTouchHelper.SimpleCallback(UP or DOWN or START or END, 0) {

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?,
                                           actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState == ACTION_STATE_DRAG) viewHolder?.itemView?.alpha = 0.5f
            }

            override fun clearView(recyclerView: RecyclerView,
                                   viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                viewHolder.itemView.alpha = 1.0f
            }

            override fun onMove(recyclerView: RecyclerView,
                                viewHolder: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder): Boolean {
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition
                viewModel.moveItem(from, to)
                recyclerView.adapter?.notifyItemMoved(from, to)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as Application).component.inject(this)
        setContentView(R.layout.activity_finger_selection)

        configureToolbar()

        viewModel = ViewModelProvider(this, viewModelFactory).get(FingerSelectionViewModel::class.java)

        initTextInLayout()
        initRecyclerView()
        initAddFingerButton()
        initResetButton()

        listenForItemChanges()

        viewModel.start()
    }

    private fun configureToolbar() {
        setSupportActionBar(settingsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.finger_selection_activity_title)
    }

    private fun initTextInLayout() {
        addFingerButton.text = getString(R.string.finger_selection_add_finger)
        resetButton.text = getString(R.string.finger_selection_reset)
        fingerLabelTextView.text = getString(R.string.finger_selection_finger_label)
        quantityLabelTextView.text = getString(R.string.finger_selection_quantity_label)
    }

    private fun initRecyclerView() {
        fingerSelectionAdapter = FingerSelectionItemAdapter(viewModel, itemTouchHelper)
        fingerSelectionRecyclerView.layoutManager = LinearLayoutManager(this)
        fingerSelectionRecyclerView.adapter = fingerSelectionAdapter
        itemTouchHelper.attachToRecyclerView(fingerSelectionRecyclerView)
    }

    private fun initAddFingerButton() {
        addFingerButton.setOnClickListener { viewModel.addNewFinger() }
    }

    private fun initResetButton() {
        resetButton.setOnClickListener { viewModel.resetFingerItems() }
    }

    private fun listenForItemChanges() {
        viewModel.items.observe(this, Observer {
            fingerSelectionAdapter.notifyDataSetChanged()
            if (it.size >= MAXIMUM_NUMBER_OF_ITEMS) {
                addFingerButton.isEnabled = false
                addFingerButton.background.colorFilter = PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.LIGHTEN)
            } else {
                addFingerButton.isEnabled = true
                addFingerButton.background.colorFilter = null
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (viewModel.haveSettingsChanged()) {
            if (viewModel.canSavePreference()) {
                createAndShowConfirmationDialog()
            } else {
                showToast(R.string.finger_selection_invalid)
            }
        } else {
            super.onBackPressed()
        }
    }

    private fun createAndShowConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.finger_selection_confirm_dialog_text))
            .setPositiveButton(getString(R.string.finger_selection_confirm_dialog_yes)) { _, _ ->
                viewModel.savePreference()
                super.onBackPressed()
            }
            .setNegativeButton(getString(R.string.finger_selection_confirm_dialog_no)) { _, _ -> super.onBackPressed() }
            .setCancelable(false)
            .create()
            .show()
    }

    companion object {
        private const val MAXIMUM_NUMBER_OF_ITEMS = 10
    }
}
