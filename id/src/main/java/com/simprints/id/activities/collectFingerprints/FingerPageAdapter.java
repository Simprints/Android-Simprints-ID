package com.simprints.id.activities.collectFingerprints;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.simprints.id.domain.Finger;

import java.util.List;

public class FingerPageAdapter extends FragmentStatePagerAdapter {

    private List<Finger> activeFingers;
    private SparseArray<FingerFragment> fragmentSparseArray;

    public FingerPageAdapter(FragmentManager fm, List<Finger> activeFingers) {
        super(fm);
        this.activeFingers = activeFingers;
        this.fragmentSparseArray = new SparseArray<>();
    }

    @Override
    public Fragment getItem(int pos) {
        FingerFragment fragment = FingerFragment.newInstance(activeFingers.get(pos));
        fragmentSparseArray.append(pos, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        fragmentSparseArray.remove(position);
    }

    public FingerFragment getFragment(int pos) {
        return fragmentSparseArray.get(pos);
    }

    @Override
    public int getCount() {
        return activeFingers.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
