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

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.theparamountgroup.android.inventory.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import us.theparamountgroup.android.inventory.data.ShellContract;

/**
 * {@link ShellCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of pet data as its data source. This adapter knows
 * how to create list items for each row of pet data in the {@link Cursor}.
 */
public class ShellCursorAdapter extends CursorAdapter {
    public static final String LOG_TAG = ShellCursorAdapter.class.getSimpleName();
    /**
     * Constructs a new {@link ShellCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ShellCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */

    private Uri mUri;
    private Bitmap mBitmap;

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the shell data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current shell can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        final int THUMBNAIL_SIZE = 64;
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView colorTextView = (TextView) view.findViewById(R.id.color);
        ImageView photoImageView = (ImageView) view.findViewById(R.id.thumbnail);
        // Find the columns of pet attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(ShellContract.ShellEntry.COLUMN_SHELL_NAME);
        int colorColumnIndex = cursor.getColumnIndex(ShellContract.ShellEntry.COLUMN_SHELL_COLOR);
        int photoColumnIndex = cursor.getColumnIndex(ShellContract.ShellEntry.COLUMN_SHELL_PHOTO);

        // Read the pet attributes from the Cursor for the current pet
        String shellName = cursor.getString(nameColumnIndex);
        Log.i(LOG_TAG, " Lets find the stored string for the shellName: " + shellName);
        String shellColor = cursor.getString(colorColumnIndex);
        Log.i(LOG_TAG, " Lets find the stored string for the shellColor: " + shellColor);
        String photo = cursor.getString(photoColumnIndex);
        Log.i(LOG_TAG, " Lets find the stored URL for the photo: " + photo);
        try (InputStream is = new URL(photo).openStream()) {
            Bitmap thumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeStream(is), THUMBNAIL_SIZE, THUMBNAIL_SIZE);
            photoImageView.setImageBitmap(thumbImage);

        } catch (MalformedURLException e) {
            Log.e("ThumbnailUtils", "Problem extracting thumbnail", e);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // If the shell color is empty string or null, then use some default text
        // that says "Unknown color", so the TextView isn't blank.
        if (TextUtils.isEmpty(shellColor)) {
            shellColor = context.getString(R.string.unknown_color);
        }

        // Update the TextViews with the attributes for the current shell
        nameTextView.setText(shellName);
        colorTextView.setText(shellColor);

/*
        if (!photo.isEmpty()) {

            mUri = Uri.parse(photo);
            Bitmap  mBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mUri);
            photoImageView.setImageBitmap(mBitmap);
        }
*/
    }


}
