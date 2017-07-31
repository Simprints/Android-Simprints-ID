package com.simprints.id.tools;


import android.content.Context;
import android.content.res.Resources;

public class ResourceHelper {

    /**
     * The abstract concept of quantity found in most languages. Note that this will not be correct
     * for all, more complicated, languages (e.g. Russian). Include only the necessary strings for
     * the relevant translation of strings.xml, but always include "other" for all languages.
     * For English, it usually sufficient to include only "one" and "other" (and sometimes "zero").
     *
     * See more: https://developer.android.com/guide/topics/resources/string-resource.html#Plurals
     */
    private enum QUANTITY {
        ZERO("zero"),
        ONE("one"),
        TWO("two"),
        FEW("few"),
        MANY("many"),
        OTHER("other");

        private String title;

        QUANTITY(String title) {
            this.title = title;
        }


        @Override
        public String toString() {
            return title;
        }
    }

    /**
     * Converts an integer to the grammatical concept of quantity. This is an incomplete conversion
     * but works for most languages. More advanced processing is required for languages that treat
     * numbers that end in zero, one, and two differently (e.g. 22 in Russian).
     *
     * @param quantity      The integer number that describes the quantity
     * @return              The heuristic description of the number
     */
    private static QUANTITY intToQuantity(int quantity) {
        switch (quantity) {
            case 0:
                return QUANTITY.ZERO;
            case 1:
                return QUANTITY.ONE;
            case 2:
                return QUANTITY.TWO;
            case 3:
            case 4:
                return QUANTITY.FEW;
        }

        if (quantity > 4) return QUANTITY.MANY;

        return QUANTITY.OTHER;
    }

    /**
     * A translation-friendly way to access pluralised string resources. Some assumptions are made
     * about the names of the child string resources. Always make sure there is an "other" version
     * of the plural as this will be used as the default. Besides "other", only include plurals in
     * strings.xml that are required by the language.
     *
     * @param context               The parent context
     * @param stringQuantityKey     The R.string id of the key of the target resource
     * @param quantity              The number to use in deciding which plural to use
     * @param values                The values used to format the string
     * @return                      The formatted final string
     */
    public static String getStringPlural(Context context, int stringQuantityKey, int quantity, Object... values) {
        Resources res = context.getResources();

        try {
            String targetStringResourceName = res.getString(stringQuantityKey) + intToQuantity(quantity).toString();
            int targetStringResourceId = res.getIdentifier(targetStringResourceName, "string", context.getPackageName());
            return res.getString(targetStringResourceId, values);

        } catch (Resources.NotFoundException e) {
            // If we can't find the resource, try instead to find the "other" version
            String targetStringResourceName = res.getString(stringQuantityKey) + QUANTITY.OTHER.toString();
            int targetStringResourceId = res.getIdentifier(targetStringResourceName, "string", context.getPackageName());
            return res.getString(targetStringResourceId, values);
        }
    }
}
