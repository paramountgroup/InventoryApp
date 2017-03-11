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
package us.theparamountgroup.android.inventory.data;

import android.net.Uri;
import android.content.ContentResolver;
import android.provider.BaseColumns;

/**
 * API Contract for the Pets app.
 */
public final class ShellContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private ShellContract() {}

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "us.theparamountgroup.android.inventory";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.shells/shells/ is a valid path for
     * looking at pet data. content://com.example.android.shell/beach/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "beach".
     */
    public static final String PATH_SHELLS = "shells";

    /**
     * Inner class that defines constant values for the pets database table.
     * Each entry in the table represents a single pet.
     */
    public static final class PetEntry implements BaseColumns {

        /** The content URI to access the pet data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_SHELLS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of pets.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SHELLS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single pet.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SHELLS;

        /** Name of database table for pets */
        public final static String TABLE_NAME = "shells";


        /**
         * Unique ID number for the pet (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the pet.
         *
         * Type: TEXT
         */
        public final static String COLUMN_SHELL_NAME ="name";

        /**
         * Color of the shell.
         *
         * Type: TEXT
         */
        public final static String COLUMN_SHELL_COLOR = "color";

        /**
         * Photo of the shell.
         *
         * Type: TEXT
         */
        public final static String COLUMN_SHELL_PHOTO = "photo";

        /**
         * Does the shell have a hole.
         *
         * The only possible values are {@link #HOLE_UNKNOWN}, {@link #HOLE},
         * or {@link #NO_HOLE}.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_SHELL_HOLE = "hole";

        /**
         * Possible values for the hole in the shell.
         */
        public static final int HOLE_UNKNOWN = 0;
        public static final int HOLE = 1;
        public static final int NO_HOLE = 2;

        /**
         * Weight of the pet.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_SHELL_TYPE = "type";

        public static final int TYPE_SCALLOP = 0;
        public static final int TYPE_JINGLE = 1;
        public static final int TYPE_SLIPPER = 2;
        public static final int TYPE_SHARD = 3;





        /**
         * Returns whether or not the given shell hole is {@link #HOLE_UNKNOWN}, {@link #HOLE},
         * or {@link #NO_HOLE}.
         */
        public static boolean isValidHole(int hole) {
            if (hole == HOLE_UNKNOWN || hole == HOLE || hole == NO_HOLE) {
                return true;
            }
            return false;
        }

        /**
         * Returns whether or not the given gender is {@link #HOLE_UNKNOWN}, {@link #HOLE},
         * or {@link #NO_HOLE}.
         */
        public static boolean isValidType(int shellType) {
            if (shellType == TYPE_SCALLOP || shellType == TYPE_JINGLE || shellType == TYPE_SLIPPER || shellType == TYPE_SHARD) {
                return true;
            }
            return false;
        }
    }

}

