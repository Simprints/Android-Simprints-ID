package com.simprints.feature.chatbot

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.simprints.feature.chatbot.context.ChatContextProvider
import com.simprints.infra.aichat.model.WorkflowStepInfo
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.experimental
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Manages the floating chat FAB and chat panel overlay within an Activity.
 * Call [attach] from the activity's onCreate to add the FAB to the root layout.
 * The FAB is only shown when the chatbot feature flag is enabled.
 *
 * Also serves as the public API for feeding runtime context into the chatbot
 * (screen name, workflow type, workflow steps) via [updateScreen], [updateWorkflow],
 * [updateSteps], and [clearWorkflow].
 */
class ChatOverlayManager @Inject constructor(
    private val configRepository: ConfigRepository,
    private val contextProvider: ChatContextProvider,
) {
    private var activity: FragmentActivity? = null
    private var fab: ImageButton? = null
    private var chatCard: MaterialCardView? = null
    private var isExpanded = false

    fun attach(activity: FragmentActivity, rootContainer: FrameLayout) {
        this.activity = activity

        val fab = ImageButton(activity).apply {
            layoutParams = FrameLayout.LayoutParams(
                dpToPx(activity, 56),
                dpToPx(activity, 56),
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                setMargins(0, 0, dpToPx(activity, 16), dpToPx(activity, 16))
            }
            setBackgroundResource(R.drawable.bg_chat_fab)
            setImageResource(android.R.drawable.ic_dialog_info)
            imageTintList = ColorStateList.valueOf(Color.WHITE)
            contentDescription = activity.getString(R.string.chatbot_fab_description)
            elevation = dpToPx(activity, 6).toFloat()
            setOnClickListener { toggleChat() }
            visibility = View.GONE
        }
        this.fab = fab
        rootContainer.addView(fab)

        val card = MaterialCardView(activity).apply {
            id = View.generateViewId()
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            ).apply {
                val margin = dpToPx(activity, 12)
                setMargins(margin, dpToPx(activity, 80), margin, dpToPx(activity, 12))
            }
            radius = dpToPx(activity, 16).toFloat()
            strokeWidth = dpToPx(activity, 1)
            strokeColor = Color.parseColor("#66000000")
            cardElevation = dpToPx(activity, 12).toFloat()
            setCardBackgroundColor(Color.WHITE)
            visibility = View.GONE
        }
        this.chatCard = card
        rootContainer.addView(card)

        activity.lifecycleScope.launch {
            val enabled = runCatching {
                configRepository.getProjectConfiguration().experimental().chatbotEnabled
            }.getOrDefault(false)

            if (enabled) {
                fab.visibility = View.VISIBLE
            }
        }
    }

    fun toggleChat() {
        if (isExpanded) {
            minimize()
        } else {
            expand()
        }
    }

    fun minimize() {
        chatCard?.visibility = View.GONE
        fab?.visibility = View.VISIBLE
        isExpanded = false
    }

    private fun expand() {
        val act = activity ?: return
        fab?.visibility = View.GONE
        chatCard?.visibility = View.VISIBLE

        val containerId = chatCard?.id ?: return
        val existing = act.supportFragmentManager.findFragmentById(containerId)
        if (existing == null) {
            act.supportFragmentManager.commit {
                replace(containerId, ChatbotFragment())
            }
        }
        isExpanded = true
    }

    fun updateScreen(screenName: String) {
        contextProvider.updateScreen(screenName)
    }

    fun updateSteps(steps: List<WorkflowStepInfo>) {
        contextProvider.updateSteps(steps)
    }

    fun updateWorkflow(workflowType: String) {
        contextProvider.updateWorkflow(workflowType)
    }

    fun clearWorkflow() {
        contextProvider.clearWorkflow()
    }

    private fun dpToPx(activity: FragmentActivity, dp: Int): Int {
        val density = activity.resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }
}
