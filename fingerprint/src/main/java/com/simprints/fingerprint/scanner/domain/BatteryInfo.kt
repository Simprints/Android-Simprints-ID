package com.simprints.fingerprint.scanner.domain

data class BatteryInfo(
    val charge: Int,        // Percentage of full charge
    val voltage: Int,       // In mV
    val current: Int,       // In mA, negative if discharging, positive if charging
    val temperature: Int    // In dK (tenths of Kelvin above 0K)
) {

    fun isLowBattery() = charge <= LOW_BATTERY_CHARGE

    companion object {
        const val LOW_BATTERY_CHARGE = 20

        val UNKNOWN = BatteryInfo(Int.MIN_VALUE, Int.MIN_VALUE, Int.MIN_VALUE, Int.MIN_VALUE)
    }
}
