package us.theparamountgroup.android.inventory;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.theparamountgroup.android.inventory.R;

import us.theparamountgroup.android.inventory.data.ShellContract.ShellEntry;


public class ShellFragmentScallops extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String LOG_TAG = ShellFragmentScallops.class.getSimpleName();
    /**
     * Identifier for the shell data loader
     */
    private static final int SHELL_LOADER = 0;

    /**
     * Adapter for the ListView
     */
    ShellCursorAdapter mCursorAdapter;

    public ShellFragmentScallops() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_shells, container, false);
        // Inflate the layout for this fragment

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab2);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the shell data
        ListView petListView = (ListView) rootView.findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = rootView.findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of pet data in the Cursor.
        // There is no shell data yet (until the loader finishes) so pass in null for the Cursor.

        mCursorAdapter = new ShellCursorAdapter(getContext(), null);
        petListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(getActivity(), EditorActivity.class);

                // Form the content URI that represents the specific shell that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link ShellEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.shells/shells/2"
                // if the shell with ID 2 was clicked on.
                Uri currentPetUri = ContentUris.withAppendedId(ShellEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentPetUri);

                // Launch the {@link EditorActivity} to display the data for the current shell.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(SHELL_LOADER, null, this);

        return rootView;
    }

    public android.support.v4.content.Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                ShellEntry._ID,
                ShellEntry.COLUMN_SHELL_NAME,
                ShellEntry.COLUMN_SHELL_COLOR,
                ShellEntry.COLUMN_SHELL_HOLE,
                ShellEntry.COLUMN_SHELL_QUANTITY,
                ShellEntry.COLUMN_SHELL_PRICE,
                ShellEntry.COLUMN_SHELL_PHOTO,
                ShellEntry.COLUMN_SHELL_THUMBNAIL};

        String[] scallopArgument = {
                "0"
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new android.support.v4.content.CursorLoader(getContext(),   // Parent activity context
                ShellEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                ShellEntry.COLUMN_SHELL_TYPE + "=?",               // selection clause shell type
                scallopArgument,                     //selection argument "0" Scallop
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {

        // Update {@link ShellCursorAdapter} with this new cursor containing updated shell data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
