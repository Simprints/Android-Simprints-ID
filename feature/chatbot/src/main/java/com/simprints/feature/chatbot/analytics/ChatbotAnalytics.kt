package com.simprints.feature.chatbot.analytics

import com.simprints.infra.logging.LoggingConstants.CrashReportTag.CHATBOT
import com.simprints.infra.logging.Simber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lightweight analytics tracker for chatbot usage.
 * Uses Simber logging (Crashlytics) for the PoC.
 * Can be upgraded to full Event system integration when the feature matures.
 */
@Singleton
internal class ChatbotAnalytics @Inject constructor() {

    fun trackChatOpened(isOnline: Boolean) {
        Simber.i("Chatbot opened (online=$isOnline)", tag = CHATBOT)
    }

    fun trackMessageSent() {
        Simber.i("User message sent", tag = CHATBOT)
    }

    fun trackResponseReceived(responseTimeMs: Long, isCloud: Boolean) {
        Simber.i("Response received (time=${responseTimeMs}ms, cloud=$isCloud)", tag = CHATBOT)
    }

    fun trackOfflineFallback() {
        Simber.i("Offline FAQ fallback used", tag = CHATBOT)
    }

    fun trackSessionClosed(messageCount: Int, durationMs: Long) {
        Simber.i("Chat session closed (messages=$messageCount, duration=${durationMs}ms)", tag = CHATBOT)
    }

    fun trackError(error: String) {
        Simber.i("Chatbot error: $error", tag = CHATBOT)
    }
}
