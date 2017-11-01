package com.simprints.id;

import android.support.test.InstrumentationRegistry;
import android.util.Log;

import org.junit.After;
import org.junit.Before;

public class FirstUseTest {

    @Before
    public void setUp() {
        Log.d("EndToEndTests", "FirstUseTest.setUp(): cleaning app data");
        Utils.clearApplicationData(InstrumentationRegistry.getContext());
    }

    @After
    public void tearDown() {
        Log.d("EndToEndTests", "FirstUseTest.tearDown(): nothing");
    }

}
