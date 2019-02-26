package com.simprints.libcommon;

import com.simprints.libsimprints.FingerIdentifier;

import java.util.Arrays;

@SuppressWarnings("unused")
public class ScanConfig {

    private final FingerConfig[] fingerConfigs;
    private final int[] priorities;
    private final int[] orders;

    /**
     * Constructor.
     * Constructs a config where all the fingers are optional and have priority 0
     */
    public ScanConfig()
    {
        this.fingerConfigs = new FingerConfig[FingerIdentifier.values().length];
        Arrays.fill(this.fingerConfigs, FingerConfig.OPTIONAL);
        this.priorities = new int[FingerIdentifier.values().length];
        Arrays.fill(this.priorities, 0);
        this.orders = new int[FingerIdentifier.values().length];
        Arrays.fill(this.orders, 0);
    }

    public void set(FingerIdentifier id, FingerConfig config, int priority, int order)
    {
        fingerConfigs[id.ordinal()] = config;
        this.priorities[id.ordinal()] = priority;
        this.orders[id.ordinal()] = order;
    }

    public FingerConfig get(FingerIdentifier id)
    {
        return fingerConfigs[id.ordinal()];
    }

    public int getPriority(FingerIdentifier id)
    {
        return priorities[id.ordinal()];
    }

    public int getOrder(FingerIdentifier id) {
        return orders[id.ordinal()];
    }
}
