package com.example.aichatassisstant.presentation.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.aichatassisstant.databinding.ItemMessageBotBinding
import com.example.aichatassisstant.databinding.ItemMessageLoadingBinding
import com.example.aichatassisstant.databinding.ItemMessageUserBinding
import com.example.aichatassisstant.domain.model.ChatMessage
import com.example.aichatassisstant.domain.model.MessageRole

class ChatAdapter : ListAdapter<ChatListItem, RecyclerView.ViewHolder>(ChatDiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ChatListItem.UserMessage -> VIEW_TYPE_USER
            is ChatListItem.BotMessage -> VIEW_TYPE_BOT
            is ChatListItem.Loading -> VIEW_TYPE_LOADING
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_USER -> UserMessageViewHolder(
                ItemMessageUserBinding.inflate(inflater, parent, false)
            )
            VIEW_TYPE_BOT -> BotMessageViewHolder(
                ItemMessageBotBinding.inflate(inflater, parent, false)
            )
            VIEW_TYPE_LOADING -> LoadingViewHolder(
                ItemMessageLoadingBinding.inflate(inflater, parent, false)
            )
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ChatListItem.UserMessage -> (holder as UserMessageViewHolder).bind(item.message)
            is ChatListItem.BotMessage -> (holder as BotMessageViewHolder).bind(item.message)
            is ChatListItem.Loading -> Unit
        }
    }

    fun submitMessages(
        messages: List<ChatMessage>,
        isLoading: Boolean,
        commitCallback: Runnable? = null
    ) {
        val items = buildList {
            messages.forEach { message ->
                when (message.role) {
                    MessageRole.USER -> add(ChatListItem.UserMessage(message))
                    MessageRole.BOT -> add(ChatListItem.BotMessage(message))
                }
            }
            if (isLoading) {
                add(ChatListItem.Loading)
            }
        }
        submitList(items, commitCallback)
    }

    private class UserMessageViewHolder(
        private val binding: ItemMessageUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            binding.textMessage.text = message.content
        }
    }

    private class BotMessageViewHolder(
        private val binding: ItemMessageBotBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            binding.textMessage.text = message.content
        }
    }

    private class LoadingViewHolder(
        binding: ItemMessageLoadingBinding
    ) : RecyclerView.ViewHolder(binding.root)

    private class ChatDiffCallback : DiffUtil.ItemCallback<ChatListItem>() {
        override fun areItemsTheSame(oldItem: ChatListItem, newItem: ChatListItem): Boolean {
            return oldItem.stableId == newItem.stableId
        }

        override fun areContentsTheSame(oldItem: ChatListItem, newItem: ChatListItem): Boolean {
            return oldItem == newItem
        }
    }

    companion object {
        private const val VIEW_TYPE_USER = 0
        private const val VIEW_TYPE_BOT = 1
        private const val VIEW_TYPE_LOADING = 2
    }
}

sealed class ChatListItem(val stableId: Long) {
    data class UserMessage(val message: ChatMessage) : ChatListItem(message.id)
    data class BotMessage(val message: ChatMessage) : ChatListItem(message.id)
    data object Loading : ChatListItem(LOADING_ID)

    companion object {
        private const val LOADING_ID = -1L
    }
}
