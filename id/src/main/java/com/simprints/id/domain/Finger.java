package com.simprints.id.domain;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.simprints.id.R;
import com.simprints.libcommon.Fingerprint;
import com.simprints.libsimprints.FingerIdentifier;


public class Finger implements Parcelable, Comparable<Finger>{

    public final static int NB_OF_FINGERS = 10;
    public static final Parcelable.Creator<Finger> CREATOR
            = new Parcelable.Creator<Finger>() {
        public Finger createFromParcel(Parcel in) {
            return new Finger(in);
        }

        public Finger[] newArray(int size) {
            return new Finger[size];
        }
    };
    private FingerIdentifier id;
    private boolean isActive;
    private Status status;
    private Fingerprint template;
    private boolean lastFinger;
    private int priority;
    private int order;

    /**
     * Constructor for the Finger class
     * @param id            The ISO number of the finger (0-9)
     * @param isActive      Whether the finger is the current finger
     * @param priority      The order in which fingers get auto-added (based on research) (0-9)
     * @param order         The order in which fingers appear in the list and the workflow (0-9)
     */
    public Finger(@NonNull FingerIdentifier id, boolean isActive, int priority, int order) {
        this.id = id;
        this.isActive = isActive;
        this.status = Status.NOT_COLLECTED;
        this.template = null;
        this.lastFinger = false;
        this.priority = priority;
        this.order = order;
    }


    private Finger(Parcel in) {
        id = FingerIdentifier.values()[in.readInt()];
        isActive = in.readInt() == 1;
        status = Status.values()[in.readInt()];
        if (in.readInt() == 1) {
            template = in.readParcelable(Fingerprint.class.getClassLoader());
        } else {
            template = null;
        }
    }

    public FingerIdentifier getId() {
        return id;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Fingerprint getTemplate() {
        return template;
    }

    public void setTemplate(Fingerprint template) {
        this.template = template;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id.ordinal());
        out.writeInt(isActive ? 1 : 0);
        out.writeInt(status.ordinal());
        out.writeInt(template != null ? 1 : 0);
        if (template != null) {
            out.writeParcelable(template, 0);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Finger finger = (Finger) o;

        return id.equals(finger.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int compareTo(@NonNull Finger other) {
        return this.order - other.order;
    }

    public boolean isLastFinger() {
        return lastFinger;
    }

    public void setLastFinger(boolean lastFinger) {
        this.lastFinger = lastFinger;
    }

    public int getPriority() {
        return priority;
    }

    public int getOrder() {
        return order;
    }

    public enum Status {
        NOT_COLLECTED(R.drawable.ic_blank_selected, R.drawable.ic_blank_deselected,
                R.string.scan_label, Color.GRAY, R.string.empty, Color.WHITE,
                R.string.please_scan),
        COLLECTING(R.drawable.ic_blank_selected, R.drawable.ic_blank_deselected,
                R.string.cancel_button, Color.BLUE, R.string.empty, Color.WHITE,
                R.string.scanning),
        GOOD_SCAN(R.drawable.ic_ok_selected, R.drawable.ic_ok_deselected,
                R.string.good_scan_message, Color.argb(255, 0, 204, 0),
                R.string.good_scan_message, Color.GREEN, R.string.good_scan_direction),
        RESCAN_GOOD_SCAN(R.drawable.ic_ok_selected, R.drawable.ic_ok_deselected,
                R.string.rescan_label_question, Color.argb(255, 0, 204, 0),
                R.string.good_scan_message, Color.GREEN, R.string.good_scan_direction),
        BAD_SCAN(R.drawable.ic_alert_selected, R.drawable.ic_alert_deselected,
                R.string.rescan_label, Color.argb(255, 204, 0, 0),
                R.string.poor_scan_message, Color.RED, R.string.poor_scan_direction);

        private int dotSelectedDrawableId;
        private int dotDeselectedDrawableId;
        private int buttonTextId;
        private int buttonTextColor;
        private int buttonBgColor;
        private int textResult;
        private int textResultColor;
        private int textDirection;
        private int textDirectionColor;

        Status(int selectedDrawableId, int deselectedDrawableId, int textId,
               int bgColor, int result, int resultColor, int direction) {
            this.dotSelectedDrawableId = selectedDrawableId;
            this.dotDeselectedDrawableId = deselectedDrawableId;
            this.buttonTextId = textId;
            this.buttonTextColor = Color.WHITE;
            this.buttonBgColor = bgColor;
            this.textResult = result;
            this.textResultColor = resultColor;
            this.textDirection = direction;
            this.textDirectionColor = Color.GRAY;
        }

        public int getDrawableId(boolean selected) {
            return selected ? dotSelectedDrawableId : dotDeselectedDrawableId;
        }

        public int getButtonTextId() {
            return buttonTextId;
        }

        public int getButtonTextColor() {
            return buttonTextColor;
        }

        public int getButtonBgColor() {
            return buttonBgColor;
        }

        public int getTextResult() {
            return textResult;
        }

        public int getTextResultColor() {
            return textResultColor;
        }

        public int getTextDirection() {
            return textDirection;
        }

        public int getTextDirectionColor() {
            return textDirectionColor;
        }
    }
}
