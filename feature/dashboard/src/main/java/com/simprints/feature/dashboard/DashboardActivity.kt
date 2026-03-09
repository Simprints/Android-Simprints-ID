package com.simprints.feature.dashboard

import android.os.Bundle
import android.widget.FrameLayout
import com.simprints.core.tools.activity.BaseActivity
import com.simprints.feature.chatbot.ChatOverlayHost
import com.simprints.feature.chatbot.ChatOverlayManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DashboardActivity : BaseActivity(R.layout.activity_dashboard_main), ChatOverlayHost {

    @Inject
    lateinit var chatOverlayManager: ChatOverlayManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootContainer = findViewById<FrameLayout>(R.id.dashboardRoot)
        chatOverlayManager.attach(this, rootContainer)
    }

    override fun minimizeChatOverlay() {
        chatOverlayManager.minimize()
    }
}
