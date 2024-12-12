package com.simprints.infra.recent.user.activity.local

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.recent.user.activity.ProtoRecentUserActivity
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import org.junit.Test

class RecentUserActivityTest {
    companion object {
        private val recentUserActivity = RecentUserActivity(
            "version",
            "scanner",
            "user".asTokenizableEncrypted(),
            10,
            20,
            30,
            50L,
        )

        private val protoRecentUserActivity = ProtoRecentUserActivity
            .newBuilder()
            .setLastScannerVersion("version")
            .setLastScannerUsed("scanner")
            .setLastUserUsed("user")
            .setEnrolmentsToday(10)
            .setIdentificationsToday(20)
            .setVerificationsToday(30)
            .setLastActivityTime(50L)
            .setIsUserIdTokenized(true)
            .build()
    }

    @Test
    fun `toProto should adapt correctly`() {
        assertThat(recentUserActivity.toProto()).isEqualTo(protoRecentUserActivity)
    }

    @Test
    fun `toDomain should adapt correctly`() {
        assertThat(protoRecentUserActivity.toDomain()).isEqualTo(recentUserActivity)
    }
}
