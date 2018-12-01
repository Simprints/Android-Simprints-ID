package com.simprints.id.services.scheduledSync.peopleDownSync

enum class PeopleDownSyncOption {
    OFF,
    BACKGROUND,
    ACTIVE
}

fun PeopleDownSyncOption.isDownSyncOff() = this == PeopleDownSyncOption.OFF
fun PeopleDownSyncOption.isDownSyncActiveOnLaunch() = this == PeopleDownSyncOption.ACTIVE
fun PeopleDownSyncOption.isDownSyncActiveOnUserAction() = this == PeopleDownSyncOption.BACKGROUND
