package com.simprints.id.commontesttools.sessionEvents

import com.simprints.id.data.analytics.eventData.models.domain.session.Device
import com.simprints.id.data.analytics.eventData.models.domain.session.SessionEvents
import com.simprints.id.tools.TimeHelper
import java.util.*

fun createFakeSession(timeHelper: TimeHelper? = null,
                      projectId: String = "some_project",
                      id: String = UUID.randomUUID().toString(),
                      startTime: Long = timeHelper?.now() ?: 0,
                      fakeRelativeEndTime: Long = 0): SessionEvents =
    SessionEvents(
        id = id,
        projectId = projectId,
        appVersionName = "some_version",
        libVersionName = "some_version",
        language = "en",
        device = Device(),
        startTime = startTime).apply {
        relativeEndTime = fakeRelativeEndTime
    }

fun createFakeOpenSession(timeHelper: TimeHelper,
                          projectId: String = "some_project",
                          id: String = UUID.randomUUID().toString()) =
    createFakeSession(timeHelper, projectId, id, timeHelper.nowMinus(1000))

fun createFakeOpenSessionButExpired(timeHelper: TimeHelper,
                                    projectId: String = "some_project",
                                    id: String = UUID.randomUUID().toString()) =
    createFakeSession(timeHelper, projectId, id, timeHelper.nowMinus(SessionEvents.GRACE_PERIOD + 1000))

fun createFakeClosedSession(timeHelper: TimeHelper,
                            projectId: String = "some_project",
                            id: String = UUID.randomUUID().toString()) =
    createFakeSession(timeHelper, projectId, id, timeHelper.nowMinus(1000)).apply {
        relativeEndTime = nowRelativeToStartTime(timeHelper)
    }
