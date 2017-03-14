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

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.theparamountgroup.android.inventory.R;

import us.theparamountgroup.android.inventory.data.ShellContract;

/**
 * Allows user to create a new shell or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = EditorActivity.class.getSimpleName();

    /** Identifier for the shell data loader */
    private static final int EXISTING_SHELL_LOADER = 0;

    /** Content URI for the existing shell (null if it's a new shell) */
    private Uri mCurrentShellUri;

    /** EditText field to enter the shell's name */
    private EditText mNameEditText;

    /** EditText field to enter the shell's color */
    private EditText mColorEditText;



    /** EditText field to enter if the shell has a hole  */
    private Spinner mHoleSpinner;

    /** EditText field to enter the type shell */
    private Spinner mTypeSpinner;

    /**
     * If the shell has hole. The possible valid values are in the ShellContract.java file:
     * {@link ShellContract.ShellEntry#HOLE_UNKNOWN}, {@link ShellContract.ShellEntry#HOLE}, or
     * {@link ShellContract.ShellEntry#NO_HOLE}.
     */
    private int mHole = ShellContract.ShellEntry.HOLE_UNKNOWN;


    /**
     * Type of Shell. The possible valid values are in the ShellContract.java file:
     * {@link ShellContract.ShellEntry#TYPE_SCALLOP}, {@link ShellContract.ShellEntry#TYPE_JINGLE},
     * {@link ShellContract.ShellEntry#TYPE_SLIPPER},{@link ShellContract.ShellEntry#TYPE_SHARD}.
     */
    private int mType = ShellContract.ShellEntry.TYPE_SCALLOP;

    /** Boolean flag that keeps track of whether the shell has been edited (true) or not (false) */
    private boolean mShellHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mShellHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mShellHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new shell or editing an existing one.
        Intent intent = getIntent();
        mCurrentShellUri = intent.getData();

        // If the intent DOES NOT contain a shell content URI, then we know that we are
        // creating a new shell.
        if (mCurrentShellUri == null) {
            // This is a new shell, so change the app bar to say "Add a Shell"
            setTitle(getString(R.string.editor_activity_title_new_pet));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a shell that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing shell, so change app bar to say "Edit Shell"
            setTitle(getString(R.string.editor_activity_title_edit_pet));

            // Initialize a loader to read the shell data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_SHELL_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_shell_name);
        mColorEditText = (EditText) findViewById(R.id.edit_shell_color);

        mHoleSpinner = (Spinner) findViewById(R.id.spinner_hole);
        mTypeSpinner = (Spinner) findViewById(R.id.spinner_type);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mColorEditText.setOnTouchListener(mTouchListener);

        mHoleSpinner.setOnTouchListener(mTouchListener);
        mTypeSpinner.setOnTouchListener(mTouchListener);

        setupHoleSpinner();
        setupTypeSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select if the shell has a hole.
     */
    private void setupHoleSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_hole_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mHoleSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mHoleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.no_hole))) {
                        mHole = ShellContract.ShellEntry.HOLE;
                    } else if (selection.equals(getString(R.string.hole))) {
                        mHole = ShellContract.ShellEntry.NO_HOLE;
                    } else {
                        mHole = ShellContract.ShellEntry.HOLE_UNKNOWN;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mHole = ShellContract.ShellEntry.HOLE_UNKNOWN;
            }
        });
    }

    /**
     * Setup the dropdown spinner that allows the user to select the type of shell.
     */
    private void setupTypeSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter typeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_type_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        typeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mTypeSpinner.setAdapter(typeSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.type_scallop))) {
                        mType = ShellContract.ShellEntry.TYPE_SCALLOP;
                    } else if (selection.equals(getString(R.string.type_jingle))) {
                        mType = ShellContract.ShellEntry.TYPE_JINGLE;
                    } else if (selection.equals(getString(R.string.type_slipper))) {
                        mType = ShellContract.ShellEntry.TYPE_SLIPPER;
                    } else {

                        mType = ShellContract.ShellEntry.TYPE_SHARD;
                        Log.i(LOG_TAG," In type spinner and assigned mType to shard: " + mType);
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mType = ShellContract.ShellEntry.TYPE_SCALLOP;
            }
        });
    }
    /**
     * Get user input from editor and save pet into database.
     */
    private void saveShell() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String colorString = mColorEditText.getText().toString().trim();


        // Check if this is supposed to be a new shell
        // and check if all the fields in the editor are blank
        if (mCurrentShellUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(colorString) && mHole == ShellContract.ShellEntry.HOLE_UNKNOWN) {
            // Since no fields were modified, we can return early without creating a new shell.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and pet attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ShellContract.ShellEntry.COLUMN_SHELL_NAME, nameString);
        values.put(ShellContract.ShellEntry.COLUMN_SHELL_COLOR, colorString);
        values.put(ShellContract.ShellEntry.COLUMN_SHELL_HOLE, mHole);
        values.put(ShellContract.ShellEntry.COLUMN_SHELL_TYPE, mType);

        // Determine if this is a new or existing shell by checking if mCurrentShellUri is null or not
        if (mCurrentShellUri == null) {
            // This is a NEW shell, so insert a new shell into the provider,
            // returning the content URI for the new shell.
            Uri newUri = getContentResolver().insert(ShellContract.ShellEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING shell, so update the shell with content URI: mCurrentShellUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentShellUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentShellUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new shell, hide the "Delete" menu item.
        if (mCurrentShellUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save shell to database
                saveShell();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the shell hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mShellHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the shell hasn't changed, continue with handling back button press
        if (!mShellHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all shell attributes, define a projection that contains
        // all columns from the pet table
        String[] projection = {
                ShellContract.ShellEntry._ID,
                ShellContract.ShellEntry.COLUMN_SHELL_NAME,
                ShellContract.ShellEntry.COLUMN_SHELL_COLOR,
                ShellContract.ShellEntry.COLUMN_SHELL_HOLE,
                ShellContract.ShellEntry.COLUMN_SHELL_TYPE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentShellUri,         // Query the content URI for the current shell
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of Shell attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ShellContract.ShellEntry.COLUMN_SHELL_NAME);
            int breedColumnIndex = cursor.getColumnIndex(ShellContract.ShellEntry.COLUMN_SHELL_COLOR);
            int genderColumnIndex = cursor.getColumnIndex(ShellContract.ShellEntry.COLUMN_SHELL_HOLE);
            int typeColumnIndex = cursor.getColumnIndex(ShellContract.ShellEntry.COLUMN_SHELL_TYPE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String color = cursor.getString(breedColumnIndex);
            int hole = cursor.getInt(genderColumnIndex);
            int type = cursor.getInt(typeColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mColorEditText.setText(color);


            // Hole is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Unknown, 1 is Hole, 2 is No Hole).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (hole) {
                case ShellContract.ShellEntry.HOLE:
                    mHoleSpinner.setSelection(1);
                    break;
                case ShellContract.ShellEntry.NO_HOLE:
                    mHoleSpinner.setSelection(2);
                    break;
                default:
                    mHoleSpinner.setSelection(0);
                    break;
            }

            // type is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Scallop, 1 is Jingle, 2 is Slipper, 3 is Shard).
            // Then call setSelection() so that option is displayed on screen as the current selection.

            switch (type) {
                case ShellContract.ShellEntry.TYPE_SHARD:
                    mTypeSpinner.setSelection(3);
                    break;
                case ShellContract.ShellEntry.TYPE_JINGLE:
                    mTypeSpinner.setSelection(1);
                    break;
                case ShellContract.ShellEntry.TYPE_SLIPPER:
                    mTypeSpinner.setSelection(2);
                    break;
                default:
                    mTypeSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mColorEditText.setText("");

        mHoleSpinner.setSelection(0); // Select "Unknown" hole
        mTypeSpinner.setSelection(0); // Select "Scallop" to type
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this pet.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the shell.
                deleteShell();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the shell.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the shell in the database.
     */
    private void deleteShell() {
        // Only perform the delete if this is an existing shell.
        if (mCurrentShellUri != null) {
            // Call the ContentResolver to delete the shell at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentShellUri
            // content URI already identifies the shell that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentShellUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_shell_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}