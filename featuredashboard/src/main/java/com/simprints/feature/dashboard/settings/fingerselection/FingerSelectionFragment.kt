package com.simprints.feature.dashboard.settings.fingerselection

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentFingerSelectionBinding
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class FingerSelectionFragment : Fragment(R.layout.fragment_finger_selection) {

    companion object {
        private const val MAXIMUM_NUMBER_OF_ITEMS = 10
    }

    private val viewModel: FingerSelectionViewModel by viewModels()
    private val binding by viewBinding(FragmentFingerSelectionBinding::bind)

    private lateinit var fingerSelectionAdapter: FingerSelectionItemAdapter

    private val itemTouchHelper = ItemTouchHelper(
        object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END,
            0
        ) {

            override fun onSelectedChanged(
                viewHolder: RecyclerView.ViewHolder?,
                actionState: Int
            ) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) viewHolder?.itemView?.alpha =
                    0.5f
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)
                viewHolder.itemView.alpha = 1.0f
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
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

    private val saveConfirmationDialog by lazy {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(IDR.string.finger_selection_confirm_dialog_text))
            .setPositiveButton(getString(IDR.string.finger_selection_confirm_dialog_yes)) { _, _ ->
                viewModel.savePreference()
                findNavController().popBackStack()
            }
            .setNegativeButton(getString(IDR.string.finger_selection_confirm_dialog_no)) { _, _ -> findNavController().popBackStack() }
            .setCancelable(false)
            .create()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        super.onCreate(savedInstanceState)
        initRecyclerView()
        listenForItemChanges()

        binding.settingsToolbar.setNavigationOnClickListener {
            onBackPress()
        }
        binding.addFingerButton.setOnClickListener { viewModel.addNewFinger() }
        binding.resetButton.setOnClickListener { viewModel.resetFingerItems() }
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBackPress()
                }
            }
        )
        viewModel.start()
    }

    private fun initRecyclerView() {
        fingerSelectionAdapter = FingerSelectionItemAdapter(
            itemTouchHelper,
            { viewModel.fingerSelections.value ?: emptyList() },
            viewModel::changeFingerSelection,
            viewModel::changeQuantitySelection,
            viewModel::removeItem
        )
        binding.fingerSelectionRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.fingerSelectionRecyclerView.adapter = fingerSelectionAdapter
        itemTouchHelper.attachToRecyclerView(binding.fingerSelectionRecyclerView)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun listenForItemChanges() {
        viewModel.fingerSelections.observe(viewLifecycleOwner) {
            fingerSelectionAdapter.notifyDataSetChanged()
            if (it.size >= MAXIMUM_NUMBER_OF_ITEMS) {
                binding.addFingerButton.isEnabled = false
                binding.addFingerButton.background.colorFilter =
                    PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.LIGHTEN)
            } else {
                binding.addFingerButton.isEnabled = true
                binding.addFingerButton.background.colorFilter = null
            }
        }
    }

    private fun onBackPress() {
        if (viewModel.hasSelectionChanged()) {
            saveConfirmationDialog.show()
        } else {
            findNavController().popBackStack()
        }
    }
}
