package us.theparamountgroup.android.inventory;

import android.content.Context;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.theparamountgroup.android.inventory.R;

/**
 * {@link CategoryAdapter} is a {@link FragmentPagerAdapter} that can provide the layout for
 * each list item based on a data source which is a list of {@link Location} objects.
 */
public class CategoryAdapter extends FragmentPagerAdapter {

    /** Context of the app */
    private Context mContext;

    /**
     * Create a new {@link CategoryAdapter} object.
     *
     * @param fm is the fragment manager that will keep each fragment's state in the adapter
     *           across swipes.
     */
    public CategoryAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }


    /**
     * Return the {@link Fragment} that should be displayed for the given page number.
     */
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new ShellFragmentScallops();
        } else if (position == 1) {
            return new ShellFragmentJingle();
        } else if (position == 2) {
            return new ShellFragmentSlipper();
        } else {;
            return new ShellFragmentShard();
        }
    }

    /**
     * Return the total number of pages.
     */
    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return mContext.getString(R.string.scallops);
        } else if (position == 1) {
            return mContext.getString(R.string.jingle);
        } else if (position == 2) {
            return mContext.getString(R.string.slipper);
        } else {
            return mContext.getString(R.string.shard);
        }
    }


}
