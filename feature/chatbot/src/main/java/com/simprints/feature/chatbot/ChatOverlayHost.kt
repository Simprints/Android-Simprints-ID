package com.simprints.feature.chatbot

/**
 * Interface implemented by activities that host the chat overlay FAB + panel.
 * Allows the ChatbotFragment to request minimization of the overlay.
 */
interface ChatOverlayHost {
    fun minimizeChatOverlay()
}
