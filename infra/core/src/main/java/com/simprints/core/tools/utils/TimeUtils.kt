package com.simprints.core.tools.utils

object TimeUtils {
    fun getFormattedEstimatedOutage(estimatedOutage: Long): String {
        val elapsedTime = secondsToHoursAndMinutes(estimatedOutage).split(':').map { it.toLong() }
        return when {
            elapsedTime.size == 3 -> {
                String.format(
                    "%02d hours, %02d minutes, %02d seconds",
                    elapsedTime.first(),
                    elapsedTime[1],
                    elapsedTime.last(),
                )
            }
            elapsedTime.size == 2 && elapsedTime.first() == 0L -> {
                // This means we have a 00 as the first element in the array which we do not want so we only have seconds
                String.format(
                    "%02d seconds",
                    elapsedTime.last(),
                )
            }
            else -> {
                String.format(
                    "%02d minutes, %02d seconds",
                    elapsedTime.first(),
                    elapsedTime.last(),
                )
            }
        }
    }

    private fun secondsToHoursAndMinutes(estimatedOutage: Long): String {
        val second = estimatedOutage % 60
        var minute = estimatedOutage / 60
        if (minute >= 60) {
            val hour = minute / 60
            minute %= 60
            return "$hour:$minute:$second"
        }
        return "$minute:$second"
    }
}
