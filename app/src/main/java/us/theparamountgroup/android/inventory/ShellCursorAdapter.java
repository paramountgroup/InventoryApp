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

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.theparamountgroup.android.inventory.R;

import us.theparamountgroup.android.inventory.data.ShellContract;

import static us.theparamountgroup.android.inventory.data.DbBitmapUtility.getImage;

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
     * moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */

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
    public void bindView(View view, final Context context, Cursor cursor) {

        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView colorTextView = (TextView) view.findViewById(R.id.color);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        ImageView photoImageView = (ImageView) view.findViewById(R.id.thumbnail);
        Button saleButton = (Button) view.findViewById(R.id.sale_button);
        final String idColumn = cursor.getString(cursor.getColumnIndexOrThrow(
                ShellContract.ShellEntry._ID));
        final String nameColumn = cursor.getString(cursor.getColumnIndexOrThrow(
                ShellContract.ShellEntry.COLUMN_SHELL_NAME));
        final Uri currentProductUri = ContentUris.withAppendedId(ShellContract.ShellEntry.CONTENT_URI, Long.parseLong(idColumn));
        // Find the columns of shell attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(ShellContract.ShellEntry.COLUMN_SHELL_NAME);
        int colorColumnIndex = cursor.getColumnIndex(ShellContract.ShellEntry.COLUMN_SHELL_COLOR);
        int photoColumnIndex = cursor.getColumnIndex(ShellContract.ShellEntry.COLUMN_SHELL_PHOTO);
        int quantityColumnIndex = cursor.getColumnIndex(ShellContract.ShellEntry.COLUMN_SHELL_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(ShellContract.ShellEntry.COLUMN_SHELL_PRICE);
        int thumbnailColumnIndex = cursor.getColumnIndex(ShellContract.ShellEntry.COLUMN_SHELL_THUMBNAIL);

        // Read the shell attributes from the Cursor for the current shell

        final String shellName = cursor.getString(nameColumnIndex);
        String shellColor = cursor.getString(colorColumnIndex);
        String shellQuantity = cursor.getString(quantityColumnIndex);
        String shellPrice = cursor.getString(priceColumnIndex);
        String photo = cursor.getString(photoColumnIndex);

        // check if there is photo saved for item and assign the saved thumbnail to photoImageView
        if (!photo.isEmpty()) {
            try {
                byte[] thumbnail = cursor.getBlob(thumbnailColumnIndex);
                Bitmap thumbImage = getImage(thumbnail);
                photoImageView.setBackground(null);
                int cornerRadius = 10;
                RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(context.getResources(), thumbImage);
                dr.setCornerRadius(cornerRadius);
                photoImageView.setImageDrawable(dr);

            } catch (Exception e) {
                Log.e("ThumbnailUtils", "Problem extracting thumbnail", e);
                e.printStackTrace();
            }
        }

        // If the shell color is empty string or null, then use some default text
        // that says "Unknown color", so the TextView isn't blank.
        if (TextUtils.isEmpty(shellColor)) {
            shellColor = context.getString(R.string.unknown_color);
        };


        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int quantity;
                if (quantityTextView.getText().toString().isEmpty()) {
                    quantity = 0;
                } else {
                    quantity = Integer.parseInt(quantityTextView.getText().toString());
                }
                if (quantity > 0) {
                    quantity = quantity - 1;
                    quantityTextView.setText(String.valueOf(quantity));

                    ContentValues values = new ContentValues();
                   // values.put(ShellContract.ShellEntry.COLUMN_SHELL_NAME, shellName);
                    values.put(ShellContract.ShellEntry.COLUMN_SHELL_QUANTITY, quantity);
                    //values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceColumn);
                    //values.put(ProductEntry.COLUMN_PRODUCT_PHOTO, photoColumn);

                    int rowsAffected = context.getContentResolver().update(currentProductUri, values, null, null);
                    if (rowsAffected == 0) {
                       // Toast.makeText(v.getContext(), v.getContext().getString("error updating"), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(v.getContext(), "Sale Product " + nameColumn, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(v.getContext(), "Order Product", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // Update the TextViews with the attributes for the current shell
        nameTextView.setText(shellName);
        colorTextView.setText(shellColor);
        quantityTextView.setText(shellQuantity);
        priceTextView.setText("$" + shellPrice);
    }


}
