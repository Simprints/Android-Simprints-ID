package com.simprints.feature.chatbot

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.simprints.feature.chatbot.databinding.FragmentChatbotBinding
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class ChatbotFragment : Fragment(R.layout.fragment_chatbot) {

    private val viewModel: ChatbotViewModel by viewModels()
    private val binding by viewBinding(FragmentChatbotBinding::bind)
    private val adapter = ChatMessageAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.chatMessageList.adapter = adapter

        binding.btnSend.setOnClickListener { sendMessage() }
        binding.chatInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }

        binding.btnMinimize.setOnClickListener {
            (activity as? ChatOverlayHost)?.minimizeChatOverlay()
        }

        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            adapter.submitList(messages) {
                if (messages.isNotEmpty()) {
                    binding.chatMessageList.smoothScrollToPosition(messages.size - 1)
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.typingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSend.isEnabled = !isLoading
        }

        viewModel.isOffline.observe(viewLifecycleOwner) { isOffline ->
            binding.offlineBanner.visibility = if (isOffline) View.VISIBLE else View.GONE
        }
    }

    private fun sendMessage() {
        val text = binding.chatInput.text?.toString()?.trim() ?: return
        if (text.isEmpty()) return
        binding.chatInput.text?.clear()
        viewModel.sendMessage(text)
    }
}
