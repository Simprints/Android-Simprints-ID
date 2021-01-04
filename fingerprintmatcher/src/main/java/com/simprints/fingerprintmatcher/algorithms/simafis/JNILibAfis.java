package com.simprints.fingerprintmatcher.algorithms.simafis;

import com.simprints.fingerprintmatcher.algorithms.simafis.models.SimAfisPerson;

import java.nio.ByteBuffer;
import java.util.List;

public class JNILibAfis {
    static
    {
        System.loadLibrary( "simmatcherwrapper" );
    }

    /*
     * We use a class initializer to allow the native code to cache some
     * field offsets. This native function looks up and caches interesting
     * class/field/method IDs. Throws on failure.
     */
    private static native boolean nativeInit();

    static
    {
        nativeInit();
    }

    public static native int getNbCores();

    public static native float verify(ByteBuffer probe, ByteBuffer candidate);

    public static native float[] identify(SimAfisPerson probe, List<SimAfisPerson> candidates, int nbThreads);

}
