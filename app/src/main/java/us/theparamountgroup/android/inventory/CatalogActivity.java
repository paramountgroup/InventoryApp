/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package us.theparamountgroup.android.inventory;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.theparamountgroup.android.inventory.R;

import us.theparamountgroup.android.inventory.data.ShellContract;
import us.theparamountgroup.android.inventory.data.ShellContract.ShellEntry;

/**
 * Displays list of shells that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity {

    public static final String LOG_TAG = ShellFragmentScallops.class.getSimpleName();
    /**
     * Identifier for the shell data loader
     */
   // private static final int PET_LOADER = 0;

    private static String NO_PHOTO = "";

    public static CatalogActivity parentContext;

    /**
     * Adapter for the ListView
     */
    ShellCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        parentContext = CatalogActivity.this;
        Log.i(LOG_TAG, " In CatalogActivity - onCreate");
        // Find the view pager that will allow the user to swipe between fragments
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);

        // Create an adapter that knows which fragment should be shown on each page
        CategoryAdapter adapter = new CategoryAdapter(this, getSupportFragmentManager());

        // Set the adapter onto the view pager
        viewPager.setAdapter(adapter);

        // Find the tab layout that shows the tabs
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        // Connect the tab layout with the view pager. This will
        //   1. Update the tab layout when the view pager is swiped
        //   2. Update the view pager when a tab is selected
        //   3. Set the tab layout's tab names with the view pager's adapter's titles
        //      by calling onPageTitle()
        tabLayout.setupWithViewPager(viewPager);

    }

    /**
     * Helper method to insert hardcoded shell data into the database. For debugging purposes only.
     */
    private void insertShell() {
        // Create a ContentValues object where column names are the keys,
        // and Toto's pet attributes are the values.
        ContentValues values = new ContentValues();
        values.put(ShellContract.ShellEntry.COLUMN_SHELL_NAME, "Inserted Shell");
        values.put(ShellContract.ShellEntry.COLUMN_SHELL_COLOR, "White");
        values.put(ShellEntry.COLUMN_SHELL_HOLE, ShellContract.ShellEntry.HOLE);
        values.put(ShellContract.ShellEntry.COLUMN_SHELL_TYPE, ShellContract.ShellEntry.TYPE_JINGLE);
        values.put(ShellContract.ShellEntry.COLUMN_SHELL_PHOTO, NO_PHOTO);
        values.put(ShellContract.ShellEntry.COLUMN_SHELL_PRICE, 7.14);
        values.put(ShellContract.ShellEntry.COLUMN_SHELL_QUANTITY, 14);
        values.put(ShellContract.ShellEntry.COLUMN_SHELL_THUMBNAIL, NO_PHOTO);

        // Insert a new row for "Inserted Shell" into the provider using the ContentResolver.
        // Use the {@link ShellEntry#CONTENT_URI} to indicate that we want to insert
        // into the shells database table.
        // Receive the new content URI that will allow us to access inserted shell data in the future.
        Uri newUri = getContentResolver().insert(ShellContract.ShellEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all shells in the database.
     */
    private void deleteAllShells() {
        int rowsDeleted = getContentResolver().delete(ShellContract.ShellEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from shell database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertShell();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllShells();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
