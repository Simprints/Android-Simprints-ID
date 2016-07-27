package com.simprints.id.model;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.simprints.id.R;
import com.simprints.libsimprints.FingerIdentifier;


public class Finger implements Parcelable, Comparable<Finger> {

    public final static int NB_OF_FINGERS = 10;

    public enum Status {
        NOT_COLLECTED(R.drawable.ic_blank_selected, R.drawable.ic_blank_deselected,
                R.string.scan_label, Color.WHITE, Color.GRAY),
        COLLECTING(R.drawable.ic_blank_selected, R.drawable.ic_blank_deselected,
                R.string.cancel_button, Color.WHITE, Color.BLUE),
        GOOD_SCAN(R.drawable.ic_ok_selected, R.drawable.ic_ok_deselected,
                R.string.rescan_label_question, Color.WHITE, Color.argb(255, 0, 204, 0)),
        BAD_SCAN(R.drawable.ic_alert_selected, R.drawable.ic_alert_deselected,
                R.string.rescan_label, Color.WHITE, Color.argb(255, 204, 0, 0));

        private int selectedDrawableId;
        private int deselectedDrawableId;
        private int textId;
        private int textColor;
        private int bgColor;

        Status(int selectedDrawableId, int deselectedDrawableId, int textId,
               int textColor, int bgColor)
        {
            this.selectedDrawableId = selectedDrawableId;
            this.deselectedDrawableId = deselectedDrawableId;
            this.textId = textId;
            this.textColor = textColor;
            this.bgColor = bgColor;
        }

        public int getDrawableId(boolean selected) {
            return selected ? selectedDrawableId : deselectedDrawableId;
        }

        public int getTextId() {
            return textId;
        }

        public int getTextColor() {
            return textColor;
        }

        public int getBgColor() {
            return bgColor;
        }
    }

    private FingerIdentifier id;
    private boolean isActive;
    private Status status;
    private byte[] template;

    public Finger(FingerIdentifier id, boolean isActive) {
        this.id = id;
        this.isActive = isActive;
        this.status = Status.NOT_COLLECTED;
        this.template = null;
    }


    public FingerIdentifier getId() {
        return id;
    }

    public boolean isActive() {
        return isActive;
    }

    public Status getStatus() {
        return status;
    }

    public byte[] getTemplate() {
        return template;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setTemplate(byte[] template) {
        this.template = template;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id.ordinal());
        out.writeInt(isActive ? 1 : 0);
        out.writeInt(status.ordinal());
        if (status == Status.GOOD_SCAN || status == Status.BAD_SCAN) {
            out.writeByteArray(template);
        }
    }

    public static final Parcelable.Creator<Finger> CREATOR
            = new Parcelable.Creator<Finger>() {
        public Finger createFromParcel(Parcel in) {
            return new Finger(in);
        }

        public Finger[] newArray(int size) {
            return new Finger[size];
        }
    };

    private Finger(Parcel in) {
        this.id = FingerIdentifier.values()[in.readInt()];
        this.isActive = in.readInt() == 1;
        this.status = Status.values()[in.readInt()];
        if (status == Status.GOOD_SCAN || status == Status.BAD_SCAN) {
            in.readByteArray(this.template);
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
        return this.id.ordinal() - other.id.ordinal();
    }
}
