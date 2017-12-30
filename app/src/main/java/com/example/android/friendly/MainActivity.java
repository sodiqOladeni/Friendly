package com.example.android.friendly;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.friendly.data.FriendlyContract.FriendlyEntry;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int FRIEND_LOADER_ID = 0;
    private static final String TAG = MainActivity.class.getSimpleName();
    private FriendlyCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FriendlyEditor.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the pet data
        ListView friendlyListView = findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        friendlyListView.setEmptyView(emptyView);


        // Setup an Adapter to create a list item for each row of pet data in the Cursor.
        // There is no pet data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new FriendlyCursorAdapter(this, null);
        friendlyListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        friendlyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(MainActivity.this, FriendlyEditor.class);

                // Form the content URI that represents the specific pet that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link PetEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.pets/pets/2"
                // if the pet with ID 2 was clicked on.
                Uri currentFriendUri = ContentUris.withAppendedId(FriendlyEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentFriendUri);

                // Launch the {@link EditorActivity} to display the data for the current pet.
                startActivity(intent);
            }
        });
        getSupportLoaderManager().initLoader(FRIEND_LOADER_ID, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(FRIEND_LOADER_ID, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                //insert fake data for testing
                fakeFriend();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                //Deleting all datas in the database
                deleteAllFriends();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fakeFriend() {
        // Create a ContentValues object where column names are the keys,
        // and Toto's pet attributes are the values.
        ContentValues contentValues = new ContentValues();
        contentValues.put(FriendlyEntry.COLUMN_FRIEND_NAME, "Zabra Fracklin");
        contentValues.put(FriendlyEntry.COLUMN_FRIEND_EMAIL, "zabrafrcklin@noEmail.com");
        contentValues.put(FriendlyEntry.COLUMN_FRIEND_PHONE, Long.parseLong("+2348051234512"));
        contentValues.put(FriendlyEntry.COLUMN_FRIEND_FB, "@sodiqOladeni");
        contentValues.put(FriendlyEntry.COLUMN_FRIEND_REL, 2);

        // Insert a new row for Toto into the provider using the ContentResolver.
        // Use the {@link PetEntry#CONTENT_URI} to indicate that we want to insert
        // into the pets database table.
        // Receive the new content URI that will allow us to access Toto's data in the future.
        Uri newUri = getContentResolver().insert(FriendlyEntry.CONTENT_URI, contentValues);

        getSupportLoaderManager().restartLoader(FRIEND_LOADER_ID, null, MainActivity.this);
    }

    private void deleteAllFriends() {
        int rowsDeleted = getContentResolver().delete(FriendlyEntry.CONTENT_URI, null, null);
        getSupportLoaderManager().restartLoader(FRIEND_LOADER_ID, null, MainActivity.this);
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        return new AsyncTaskLoader<Cursor>(this) {

                // Initialize a Cursor, this will hold all the task data
                Cursor mFriendData = null;

                // onStartLoading() is called when a loader first starts loading data
                @Override
                protected void onStartLoading() {
                    if (mFriendData != null) {
                        // Delivers any previously loaded data immediately
                        deliverResult(mFriendData);
                    } else {
                        // Force a new load
                        forceLoad();
                    }
            }

            @Override
            public Cursor loadInBackground() {
                try {
                    return getContentResolver().query(FriendlyEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            null);

                }catch (Exception e){
                    Log.e (TAG, "Failed to asynchronously load data");
                    e.printStackTrace();
                }
                return null;
            }
            // deliverResult sends the result of the load, a Cursor, to the registered listener
            public void deliverResult(Cursor data) {
                mFriendData = data;
                super.deliverResult(data);
            }
        };
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
