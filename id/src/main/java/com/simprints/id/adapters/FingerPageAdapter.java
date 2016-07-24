package com.simprints.id.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.simprints.id.activities.MainActivity;
import com.simprints.id.fragments.FingerFragment;
import com.simprints.id.model.Finger;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FingerPageAdapter extends FragmentStatePagerAdapter {

    private List<Finger> activeFingers;
    private Map<Integer, FingerFragment> fragmentsMap;

    public FingerPageAdapter(FragmentManager fm, List<Finger> activeFingers) {
        super(fm);
        this.activeFingers = activeFingers;
        this.fragmentsMap = new HashMap<>();
    }

    @Override
    public Fragment getItem(int pos) {
        MainActivity.log(String.format(Locale.UK, "FingerPageAdapter supplied fragment %d", pos));
        FingerFragment fragment = FingerFragment.newInstance(activeFingers.get(pos));
        fragmentsMap.put(pos, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        fragmentsMap.remove(position);
    }

    public FingerFragment getFragment(int pos) {
        return fragmentsMap.get(pos);
    }

    @Override
    public int getCount() {
        return activeFingers.size();
    }


    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
//        FingerFragment fragment = (FingerFragment) object;
//        Finger finger = fragment.getFinger();
//        int position = activeFingers.indexOf(finger);
//        if (position >= 0) {
//            MainActivity.log(String.format("%s is in position %d", finger.getId().name(), position));
//            return position;
//        } else {
//            MainActivity.log(String.format("%s is in POSITION_NONE", finger.getId().name()));
//            return POSITION_NONE;
//        }
    }
}
