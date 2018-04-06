package com.simprints.id.activities.dashboard.models

open class DashboardCard(open val type:DashboardCardType,
                         open val position: Int,
                         open val imageRes: Int,
                         open val title: String,
                         open val description: String)
