package com.simprints.id.data.db.remote.enums

@Deprecated("Remove it with RTDB")
enum class VERIFY_GUID_EXISTS_RESULT private constructor(private val title: String) {
    GUID_FOUND("GUID_found"),
    GUID_NOT_FOUND_ONLINE("GUID_not_found_online"),
    GUID_NOT_FOUND_OFFLINE("GUID_not_found_offline"),
    GUID_NOT_FOUND_UNKNOWN("GUID_not_found_unknown_connectivity)");


    override fun toString(): String {
        return title
    }
}
