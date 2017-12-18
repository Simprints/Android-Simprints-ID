package com.simprints.libdata.databaseResolvers;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.simprints.libcommon.Fingerprint;
import com.simprints.libdata.tools.Constants;
import com.simprints.libdata.tools.Utils;
import com.simprints.libsimprints.FingerIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CommCareResolver extends DatabaseResolver {

    @Override
    @NonNull
    public String getReadPermission() {
        return Constants.CC_PERMISSION;
    }


    @Override
    @NonNull
    protected List<String> getGuids(@NonNull String userId, @NonNull ContentResolver resolver) {
        Utils.log(String.format("CommCareResolver(%s, resolver)", userId));
        List<String> guids = new ArrayList<>();
        Cursor c = resolver.query(Constants.CC_CASES_URI, new String[]{Constants.CC_CASE_ID_COL, Constants.CC_OWNER_ID_COL}, null, null, null);

        if (c != null) {
            int colIndex;
//            String owner;
            String guid;
            while (c.moveToNext()) {
                try {
                    // get user id
//                    colIndex = c.getColumnIndex(Constants.CC_OWNER_ID_COL);
//                    if (colIndex == -1)
//                        continue;
//                    owner = c.getString(colIndex);
//                    Utils.log(String.format("owner -> %s", owner != null ? owner : "null"));
//                    if (owner == null || !owner.equals(userId))
//                        continue;

                    // Get patient id
                    colIndex = c.getColumnIndex(Constants.CC_CASE_ID_COL);
                    if (colIndex == -1)
                        continue;
                    guid = c.getString(colIndex);
                    if (guid == null)
                        continue;


                    guids.add(guid);
                } catch (Exception ignored) {
                }
            }
            c.close();
        }
        return guids;
    }


    @Override
    @NonNull
    protected List<Fingerprint> getFingerprints(@NonNull String guid, @NonNull ContentResolver resolver) {
        List<Fingerprint> prints = new ArrayList<>();
        Cursor c = resolver.query(
                Uri.parse(Utils.format(Constants.CC_DATA_URI, guid)),
                new String[]{Constants.CC_VALUE_COL, Constants.CC_DATUM_ID_COL},
                String.format(Locale.UK, "%s = ?", Constants.CC_DATUM_ID_COL),
                Constants.CC_TEMPLATE_SELECTION_ARGS,
                null);

        if (c != null) {
            while (c.moveToNext()) {
                try {
                    int datumIdColIndex = c.getColumnIndex(Constants.CC_DATUM_ID_COL);
                    int valueColIndex = c.getColumnIndex(Constants.CC_VALUE_COL);
                    if (datumIdColIndex != -1 && valueColIndex != -1) {
                        String datumId = c.getString(datumIdColIndex);
                        String value = c.getString(valueColIndex);
                        // Note that if datumId is null, or not a finger identifier, fId will be null
                        FingerIdentifier fId = FingerIdentifier.parseFingerIdentifier(datumId);
                        if (fId != null && value != null)
                            prints.add(new Fingerprint(fId, value));
                    }
                } catch (Exception ignored) {
                }
            }
            c.close();
        }
        return prints;
    }

}
