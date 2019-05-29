package com.simprints.fingerprintscanner.enums;

import java.util.Arrays;

public enum LED_STATE {
    OFF,
    RED,
    GREEN,
    ORANGE,
    ON;

    public final static LED_STATE[] OFF_LEDS = new LED_STATE[LED.COUNT];
    public final static LED_STATE[] GREEN_LEDS = new LED_STATE[LED.COUNT];
    public final static LED_STATE[] RED_LEDS = new LED_STATE[LED.COUNT];
    public final static LED_STATE[] ONE_ORANGE_LED = new LED_STATE[LED.COUNT];
    static {
        Arrays.fill(OFF_LEDS, LED_STATE.OFF);
        Arrays.fill(GREEN_LEDS, LED_STATE.GREEN);
        Arrays.fill(RED_LEDS, LED_STATE.RED);
        Arrays.fill(ONE_ORANGE_LED, LED_STATE.OFF);
        ONE_ORANGE_LED[LED.COUNT / 2] = LED_STATE.ORANGE;
    }
}
