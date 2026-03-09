package com.simprints.infra.aichat.engine

import com.simprints.infra.aichat.database.FaqDao
import com.simprints.infra.aichat.model.ChatContext
import com.simprints.infra.aichat.model.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline fallback [ChatEngine] backed by a curated FAQ database.
 * Uses simple keyword matching against pre-loaded FAQ entries stored in Room.
 * Always available regardless of network connectivity.
 */
@Singleton
internal class OfflineFaqEngine @Inject constructor(
    private val faqDao: FaqDao,
) : ChatEngine {

    override fun chat(
        messages: List<ChatMessage>,
        context: ChatContext,
    ): Flow<String> = flow {
        val lastMessage = messages.lastOrNull()?.content ?: return@flow

        val keywords = extractKeywords(lastMessage)
        val results = keywords.flatMap { keyword ->
            faqDao.search(keyword)
        }.distinctBy { it.id }

        if (results.isEmpty()) {
            emit(OFFLINE_NO_MATCH_RESPONSE)
        } else {
            val response = buildString {
                appendLine(OFFLINE_HEADER)
                appendLine()
                results.take(MAX_RESULTS).forEach { faq ->
                    appendLine("**${faq.question}**")
                    appendLine(faq.answer)
                    appendLine()
                }
                appendLine(OFFLINE_FOOTER)
            }
            emit(response)
        }
    }

    override suspend fun isAvailable(): Boolean = true

    private fun extractKeywords(query: String): List<String> = query
        .lowercase()
        .split(WORD_BOUNDARY_REGEX)
        .filter { it.length >= MIN_KEYWORD_LENGTH && it !in STOP_WORDS }
        .take(MAX_KEYWORDS)

    companion object {
        private const val MAX_RESULTS = 3
        private const val MIN_KEYWORD_LENGTH = 3
        private const val MAX_KEYWORDS = 5

        private val WORD_BOUNDARY_REGEX = Regex("[\\s,.?!;:]+")

        private val STOP_WORDS = setOf(
            "the", "and", "for", "that", "this", "with", "from",
            "are", "was", "were", "been", "have", "has", "had",
            "not", "but", "what", "how", "why", "when", "where",
            "can", "will", "does", "did", "don", "isn",
        )

        private const val OFFLINE_HEADER =
            "📴 I'm currently in **offline mode** and can't generate new answers. " +
                "Here are some relevant help topics I found:"

        private const val OFFLINE_NO_MATCH_RESPONSE =
            "📴 I'm currently in **offline mode** and couldn't find a matching help topic. " +
                "Your question has been saved and will be answered when you're back online.\n\n" +
                "In the meantime, try checking the app's help section or contacting your project supervisor."

        private const val OFFLINE_FOOTER =
            "_Connect to the internet for more detailed, personalized assistance._"
    }
}
