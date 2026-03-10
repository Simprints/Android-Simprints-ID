package com.simprints.feature.chatbot

import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.simprints.feature.chatbot.databinding.ItemChatMessageAssistantBinding
import com.simprints.feature.chatbot.databinding.ItemChatMessageUserBinding
import com.simprints.infra.aichat.model.ChatMessage
import com.simprints.infra.aichat.model.ChatRole
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.linkify.LinkifyPlugin

internal class ChatMessageAdapter :
    ListAdapter<ChatMessage, RecyclerView.ViewHolder>(ChatMessageDiffCallback()) {

    private var markwon: Markwon? = null

    private fun getMarkwon(parent: ViewGroup): Markwon =
        markwon ?: Markwon.builder(parent.context)
            .usePlugin(TablePlugin.create(parent.context))
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(LinkifyPlugin.create())
            .build()
            .also { markwon = it }

    override fun getItemViewType(position: Int): Int = when (getItem(position).role) {
        ChatRole.USER -> VIEW_TYPE_USER
        else -> VIEW_TYPE_ASSISTANT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_USER -> UserViewHolder(
                ItemChatMessageUserBinding.inflate(inflater, parent, false),
            )
            else -> AssistantViewHolder(
                ItemChatMessageAssistantBinding.inflate(inflater, parent, false),
                getMarkwon(parent),
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is UserViewHolder -> holder.bind(message)
            is AssistantViewHolder -> holder.bind(message)
        }
    }

    class UserViewHolder(
        private val binding: ItemChatMessageUserBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.messageText.text = message.content
        }
    }

    class AssistantViewHolder(
        private val binding: ItemChatMessageAssistantBinding,
        private val markwon: Markwon,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.messageText.movementMethod = LinkMovementMethod.getInstance()
        }

        fun bind(message: ChatMessage) {
            markwon.setMarkdown(binding.messageText, message.content)
        }
    }

    companion object {
        private const val VIEW_TYPE_USER = 0
        private const val VIEW_TYPE_ASSISTANT = 1
    }
}

private class ChatMessageDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
    override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean =
        oldItem.timestampMs == newItem.timestampMs && oldItem.role == newItem.role

    override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean =
        oldItem == newItem
}
