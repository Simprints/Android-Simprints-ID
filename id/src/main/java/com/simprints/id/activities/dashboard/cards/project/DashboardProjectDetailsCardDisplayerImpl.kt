package com.simprints.id.activities.dashboard.cards.project

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import com.simprints.id.R
import com.simprints.id.tools.AndroidResourcesHelper
import org.jetbrains.anko.layoutInflater

class DashboardProjectDetailsCardDisplayerImpl(
    private val context: Context,
    private val androidResourcesHelper: AndroidResourcesHelper
) : DashboardProjectDetailsCardDisplayer {

    private lateinit var root: LinearLayout
    private lateinit var card: View

    override fun initRoot(rootLayout: LinearLayout) {
        root = rootLayout
        card = inflateCard(root)
    }

    override fun displayProjectDetails() {

    }

    private fun inflateCard(root: LinearLayout) = context.layoutInflater.inflate(
        R.layout.activity_login, root, false
    )

}
