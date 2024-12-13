package com.simprints.fingerprint.infra.scanner.domain

/**
 * This class represents the information of the current state of the scanner's battery.
 *
 * @param charge Percentage of full charge (0 - 100)
 * @param voltage Measured voltage in mV
 * @param current Measured current in mA (negative if discharging, positive if charging)
 * @param temperature Measured temperature in dK (tenths of Kelvin above 0K)
 */
data class BatteryInfo(
    val charge: Int,
    val voltage: Int,
    val current: Int,
    val temperature: Int,
) {
    fun isLowBattery() = charge <= LOW_BATTERY_CHARGE

    companion object {
        const val LOW_BATTERY_CHARGE = 20

        val UNKNOWN = BatteryInfo(Int.MIN_VALUE, Int.MIN_VALUE, Int.MIN_VALUE, Int.MIN_VALUE)
    }
}
