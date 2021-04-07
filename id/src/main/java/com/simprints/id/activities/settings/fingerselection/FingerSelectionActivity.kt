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
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.END
import androidx.recyclerview.widget.ItemTouchHelper.START
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.databinding.ActivityFingerSelectionBinding
import com.simprints.id.tools.extensions.showToast
import javax.inject.Inject

class FingerSelectionActivity : BaseSplitActivity() {

    @Inject
    lateinit var viewModelFactory: FingerSelectionViewModelFactory
    private lateinit var viewModel: FingerSelectionViewModel
    private val binding by viewBinding(ActivityFingerSelectionBinding::inflate)

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
        setContentView(binding.root)

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
        setSupportActionBar(binding.settingsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.finger_selection_activity_title)
    }

    private fun initTextInLayout() {
        binding.addFingerButton.text = getString(R.string.finger_selection_add_finger)
        binding.resetButton.text = getString(R.string.finger_selection_reset)
        binding.fingerLabelTextView.text = getString(R.string.finger_selection_finger_label)
        binding.quantityLabelTextView.text = getString(R.string.finger_selection_quantity_label)
    }

    private fun initRecyclerView() {
        fingerSelectionAdapter = FingerSelectionItemAdapter(
            itemTouchHelper,
            { viewModel.items.value ?: emptyList() },
            viewModel::changeFingerSelection,
            viewModel::changeQuantitySelection,
            viewModel::removeItem
        )
        binding.fingerSelectionRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.fingerSelectionRecyclerView.adapter = fingerSelectionAdapter
        itemTouchHelper.attachToRecyclerView(binding.fingerSelectionRecyclerView)
    }

    private fun initAddFingerButton() {
        binding.addFingerButton.setOnClickListener { viewModel.addNewFinger() }
    }

    private fun initResetButton() {
        binding.resetButton.setOnClickListener { viewModel.resetFingerItems() }
    }

    private fun listenForItemChanges() {
        viewModel.items.observe(this, Observer {
            fingerSelectionAdapter.notifyDataSetChanged()
            if (it.size >= MAXIMUM_NUMBER_OF_ITEMS) {
                binding.addFingerButton.isEnabled = false
                binding.addFingerButton.background.colorFilter = PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.LIGHTEN)
            } else {
                binding.addFingerButton.isEnabled = true
                binding.addFingerButton.background.colorFilter = null
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
