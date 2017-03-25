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

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.theparamountgroup.android.inventory.R;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import us.theparamountgroup.android.inventory.data.ShellContract;

/**
 * Allows user to create a new shell or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = EditorActivity.class.getSimpleName();
    private static final int PICK_IMAGE_REQUEST = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    // private static final String CAMERA_DIR = "/dcim/";
    private static final int MY_PERMISSIONS_REQUEST = 2;
    /**
     * Identifier for the shell data loader
     */
    private static final int EXISTING_SHELL_LOADER = 0;
    /**
     * constants for image requests
     */
// a static variable to get a reference of our application context
    public static Context contextOfApplication;
    String mCurrentPhotoPath;
    /**
     * Content URI for the existing shell (null if it's a new shell)
     */
    private Uri mCurrentShellUri;
    /**
     * EditText field to enter the shell's name
     */
    private EditText mNameEditText;
    /**
     * EditText field to enter the shell's color
     */
    private EditText mColorEditText;
    /**
     * EditText field to enter if the shell has a hole
     */
    private Spinner mHoleSpinner;
    /**
     * EditText field to enter the type shell
     */
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
    /**
     * Boolean flag that keeps track of whether the shell has been edited (true) or not (false)
     */
    private boolean mShellHasChanged = false;
    private ImageView mImageView;
    private Button mButtonTakePicture;
    private Uri mUri;
    private Bitmap mBitmap;
    private boolean isGalleryPicture = false;
    private TextView mQuantityTextView;
    private EditText mPriceEditText;
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

    public static Context getContextOfApplication() {
        return contextOfApplication;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       // contextOfApplication = getApplicationContext();
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

        mQuantityTextView = (TextView) findViewById(R.id.edit_product_quantity);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mColorEditText.setOnTouchListener(mTouchListener);
        mHoleSpinner.setOnTouchListener(mTouchListener);
        mTypeSpinner.setOnTouchListener(mTouchListener);
        mQuantityTextView.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);

/*      The view tree observer can be used to get notifications when global events, like layout, happen.
*       The returned ViewTreeObserver observer is not guaranteed to remain valid for the lifetime of this View.
*/
        mImageView = (ImageView) findViewById(R.id.image);
        ViewTreeObserver viewTreeObserver = mImageView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.i(LOG_TAG, " in onGlobalLayout trying to get image to appear");
                mImageView.setImageResource(R.drawable.ic_soul_shells_logo);
                mImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
               // mImageView.setImageBitmap(getBitmapFromUri(mUri));
            }
        });
        mButtonTakePicture = (Button) findViewById(R.id.take_photo);
        mButtonTakePicture.setEnabled(false);

        requestPermissions();
        setupHoleSpinner();
        setupTypeSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select if the shell has a hole.
     */
    private void setupHoleSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter holeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_hole_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        holeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mHoleSpinner.setAdapter(holeSpinnerAdapter);

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
                        Log.i(LOG_TAG, " In type spinner and assigned mType to shard: " + mType);
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
        Log.i(LOG_TAG, " in saveShell: ");
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String colorString = mColorEditText.getText().toString().trim();
        String quantityString = mQuantityTextView.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        // remove the number sign if necessary
        if (!Character.isDigit(priceString.charAt(0))){
            priceString = priceString.substring(1);
        }
        String photoString;


        // Check if this is supposed to be a new shell
        // and check if all the fields in the editor are blank
        if (mCurrentShellUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(colorString) && mHole == ShellContract.ShellEntry.HOLE_UNKNOWN) {
            // Since no fields were modified, we can return early without creating a new shell.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }
        if (mUri != null) {
            Log.i(LOG_TAG, "in Save shell Lets see what mUri has for us: " + mUri);
            photoString = mUri.toString();
            Log.i(LOG_TAG, " Lets see what photoString has for us: " + photoString);
        } else {
            photoString = "";
        }

        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }

        double price = 0.0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Double.parseDouble(priceString);
        }

        Log.i(LOG_TAG, " Lets see what photoString has for us after if: " + photoString);
        // Create a ContentValues object where column names are the keys,
        // and pet attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ShellContract.ShellEntry.COLUMN_SHELL_NAME, nameString);
        values.put(ShellContract.ShellEntry.COLUMN_SHELL_COLOR, colorString);
        values.put(ShellContract.ShellEntry.COLUMN_SHELL_HOLE, mHole);
        values.put(ShellContract.ShellEntry.COLUMN_SHELL_TYPE, mType);
        values.put(ShellContract.ShellEntry.COLUMN_SHELL_PHOTO, photoString);
        values.put(ShellContract.ShellEntry.COLUMN_SHELL_QUANTITY, quantity);
        values.put(ShellContract.ShellEntry.COLUMN_SHELL_PRICE, price);


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
                ShellContract.ShellEntry.COLUMN_SHELL_TYPE,
                ShellContract.ShellEntry.COLUMN_SHELL_QUANTITY,
                ShellContract.ShellEntry.COLUMN_SHELL_PRICE,
                ShellContract.ShellEntry.COLUMN_SHELL_PHOTO};

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
            int photoColumnIndex = cursor.getColumnIndex(ShellContract.ShellEntry.COLUMN_SHELL_PHOTO);
            int quantityColumnIndex = cursor.getColumnIndex(ShellContract.ShellEntry.COLUMN_SHELL_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(ShellContract.ShellEntry.COLUMN_SHELL_PRICE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String color = cursor.getString(breedColumnIndex);
            int hole = cursor.getInt(genderColumnIndex);
            int type = cursor.getInt(typeColumnIndex);
            String photo = cursor.getString(photoColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mColorEditText.setText(color);
            mQuantityTextView.setText(Integer.toString(quantity));
            mPriceEditText.setText("$" + Double.toString(price));

            if (!photo.isEmpty()) {
                mUri = Uri.parse(photo);
                mBitmap = getBitmapFromUri(mUri);
                mImageView.setImageBitmap(mBitmap);
            }




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

        mQuantityTextView.clearComposingText();
        mPriceEditText.clearComposingText();
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


    /*********************** New Stuff *******************/


    public void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST);
            }
        } else {
            mButtonTakePicture.setEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    mButtonTakePicture.setEnabled(true);
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    public void takePicture(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Log.i(LOG_TAG, "in takePicture");
        try {
            File file = createImageFile();
            Log.i(LOG_TAG, "in takePicture just back from createImagefile");

            mUri = FileProvider.getUriForFile(getApplication().getApplicationContext(),
                    "us.theparamountgroup.android.inventory.fileprovider", file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT,
                    mUri);
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openImageSelector(View view) {
        Intent intent;
        Log.e(LOG_TAG, "While is set and the ifs are worked through.");

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        Log.e(LOG_TAG, "Check write to external permissions");

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private File createImageFile() throws IOException {
        Log.i(LOG_TAG, "in createImageFile");
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        Log.i(LOG_TAG, "in createImageFile mCurrentPhotoPath: " + mCurrentPhotoPath);
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        Log.i(LOG_TAG, "Received an \"Activity Result\"");

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                mUri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + mUri.toString());
                mBitmap = getBitmapFromUri(mUri);
                mImageView.setImageBitmap(mBitmap);
                mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                isGalleryPicture = true;
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Log.i(LOG_TAG, "Uri: " + mUri.toString());

            mBitmap = getBitmapFromUri(mUri);
            mImageView.setImageBitmap(mBitmap);
            mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            isGalleryPicture = false;
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        if (uri == null) {
            return null;
        }
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(fileDescriptor, null, opts);
            int photoW = opts.outWidth;
            int photoH = opts.outHeight;

            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            opts.inJustDecodeBounds = false;
            opts.inSampleSize = scaleFactor;
            opts.inPurgeable = true;
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, opts);
            return image;

        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error closing ParcelFile Descriptor");
            }
        }
    }

    /* orderProduct will send users to the website www.soulshells.com where they
     * can order their own shell necklace */
    public void orderProduct(View view) {

        String url = "https://www.soulshells.com";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        // first verify that an app exists to receive the intent
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "Sorry no access to the website www.soulshells.com", Toast.LENGTH_SHORT).show();
            return;
        }

    }

    public void addQuantity(View view) {
        int quantity;
        String quantityString = mQuantityTextView.getText().toString();
        if (quantityString.isEmpty()) {
            quantity = 0;
        } else {
            quantity = Integer.parseInt(quantityString);
        }

        quantity = quantity + 1;
        mQuantityTextView.setText(String.valueOf(quantity));
    }

    public void subtractQuantity(View view) {
        int quantity;
        String quantityString = mQuantityTextView.getText().toString();
        if (quantityString.isEmpty()) {
            quantity = 0;
        } else {
            quantity = Integer.parseInt(quantityString);
        }

        if (quantity > 0) {
            quantity = quantity - 1;
        }

        mQuantityTextView.setText(String.valueOf(quantity));
    }

}