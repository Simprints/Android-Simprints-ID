package com.simprints.id;

import android.support.test.InstrumentationRegistry;
import android.util.Log;

import com.simprints.id.TestingUtilities.Client;
import com.simprints.id.testbucket01.Bucket01Suite;
import com.simprints.id.testbucket02.Bucket02Suite;
import com.simprints.remoteadminclient.ApiException;
import com.simprints.remoteadminclient.api.DefaultApi;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({Bucket01Suite.class, Bucket02Suite.class})
public class AllBucketsSuite {

    @BeforeClass
    public static void setUp() throws ApiException {
        Log.d("EndToEndTests", "Bucket01Suite.suiteSetUp(): ensuring wifi is enabled");
        Client.toggleWifiSync(true, InstrumentationRegistry.getContext());

        DefaultApi apiInstance = Utils.getConfiguredApiInstance();
        Log.d("EndToEndTests", "AllBucketsSuite.suiteSetUp(): clear cloud functions");
        apiInstance.deleteCloudFunctions();
        Log.d("EndToEndTests", "AllBucketsSuite.suiteSetUp(): clear realtime database");
        apiInstance.deleteAny("/");
        Log.d("EndToEndTests", "AllBucketsSuite.suiteSetUp(): copy cloud functions of the production project");
        apiInstance.putCloudFunctions(Utils.PROD_ID);
        Log.d("EndToEndTests", "AllBucketsSuite.suiteSetUp(): copy  security rules of the production project");
        apiInstance.putRules(Utils.PROD_ID);
    }

    @AfterClass
    public static void tearDown() throws ApiException {
        DefaultApi apiInstance = Utils.getConfiguredApiInstance();
        Log.d("EndToEndTests", "AllBucketsSuite.suiteSetUp(): clear cloud functions");
        apiInstance.deleteCloudFunctions();
        Log.d("EndToEndTests", "AllBucketsSuite.suiteSetUp(): clear realtime database");
        apiInstance.deleteAny("/");
        Log.d("EndToEndTests", "AllBucketsSuite.suiteSetUp(): clear security rules");
        apiInstance.deleteRules();
    }

}
