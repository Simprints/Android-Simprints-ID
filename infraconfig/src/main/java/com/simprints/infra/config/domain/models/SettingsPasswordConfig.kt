package com.simprints.infra.config.domain.models

sealed class SettingsPasswordConfig {

    object NotSet : SettingsPasswordConfig()
    object Unlocked : SettingsPasswordConfig()

    data class Locked(
        val code: String,
    ) : SettingsPasswordConfig()

    val locked: Boolean
        get() = this is Locked

    companion object {

        fun toDomain(settingsPassword: String?): SettingsPasswordConfig = when {
            settingsPassword.isNullOrEmpty() -> NotSet
            else -> Locked(settingsPassword)
        }
    }
}
