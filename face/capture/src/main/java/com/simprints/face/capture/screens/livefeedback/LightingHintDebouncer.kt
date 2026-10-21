package com.simprints.face.capture.screens.livefeedback

/**
 * Time debounce for the lighting hint, so it does not flicker:
 * the shown state changes only after the new condition has held for [debounceMs].
 * Not thread-safe: confine it to the analyser executor.
 */
internal class LightingHintDebouncer(
    private val debounceMs: Long,
) {
    var isShown: Boolean = false
        private set

    private var pendingSinceMs: Long? = null

    fun update(
        condition: Boolean,
        nowMs: Long,
    ): Boolean {
        if (condition == isShown) {
            pendingSinceMs = null
            return isShown
        }
        val sinceMs = pendingSinceMs ?: nowMs.also { pendingSinceMs = it }
        if (nowMs - sinceMs >= debounceMs) {
            isShown = condition
            pendingSinceMs = null
        }
        return isShown
    }

    fun reset() {
        isShown = false
        pendingSinceMs = null
    }
}
