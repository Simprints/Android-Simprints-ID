package com.simprints.infra.config.store.models

sealed class SettingsPasswordConfig {
    object NotSet : SettingsPasswordConfig()

    object Unlocked : SettingsPasswordConfig()

    data class Locked(
        val password: String,
    ) : SettingsPasswordConfig()

    val locked: Boolean
        get() = this is Locked

    fun getNullablePassword(): String? = (this as? Locked)?.password

    companion object {
        fun toDomain(settingsPassword: String?): SettingsPasswordConfig = when {
            settingsPassword.isNullOrEmpty() -> NotSet
            else -> Locked(settingsPassword)
        }
    }
}
