package com.example.android.friendly;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.LoaderManager;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.example.android.friendly.data.FriendlyContract.FriendlyEntry;
import com.squareup.picasso.Picasso;

/**
 * Created by ricHVision on 11/20/2017.
 */

public class FriendlyEditor extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private Spinner mRelSpinner;

    private ImageView mCapture;
    private TextView openCamera, openGallery;
    private static final String TAG = FriendlyEditor.class.getSimpleName();
    private static int RESULT_LOAD_IMAGE = 1;
    private static final int EXISTING_PET_LOADER = 0;
    private int mRelationship = FriendlyEntry.REL_UNKNOWN;
    private Uri mCurrentFriendUri;

    private EditText mNameEditText;
    private EditText mEmailEditText;
    private EditText mPhoneEditText;
    private EditText mfbEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mCurrentFriendUri = getIntent().getData();
        if (mCurrentFriendUri == null) {
            // This is a new pet, so change the app bar to say "Add a Pet"
            setTitle(getString(R.string.editor_activity_title_new_friend));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing pet, so change app bar to say "Edit Pet"
            setTitle(getString(R.string.editor_activity_title_edit_friend));

            // Initialize a loader to read the pet data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PET_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = findViewById(R.id.edit_friend_name);
        mEmailEditText = findViewById(R.id.edit_friend_email);
        mPhoneEditText = findViewById(R.id.edit_friend_phone);
        mfbEditText = findViewById(R.id.edit_friend_fb);


        mRelSpinner = findViewById(R.id.spinner_rel);
        mCapture = findViewById(R.id.capture);
        mCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showDeleteConfirmationDialog();
            }
        });

        setupSpinner();
    }

    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_rel_options, android.R.layout.simple_spinner_item);
        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        // Apply the adapter to the spinner
        mRelSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mRelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.rel_family))) {
                        mRelationship = FriendlyEntry.REL_FAMILY;
                    } else if (selection.equals(getString(R.string.rel_friend))) {
                        mRelationship = FriendlyEntry.REL_FRIEND;
                    } else {
                        mRelationship = FriendlyEntry.REL_UNKNOWN;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mRelationship = FriendlyEntry.REL_UNKNOWN;
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // If this is a new pet, hide the "Delete" menu item.
        if (mCurrentFriendUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                saveFriend();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                deleteFriend();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        Dialog customDialog = new Dialog(this);
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.setContentView(R.layout.dialog_capture);
        customDialog.setCancelable(true);
        customDialog.show();
        Window window = customDialog.getWindow();
        window.setLayout(android.support.v7.app.ActionBar.LayoutParams.MATCH_PARENT,
                android.support.v7.app.ActionBar.LayoutParams.WRAP_CONTENT);
        openCamera = customDialog.findViewById(R.id.open_camera);
        openGallery = customDialog.findViewById(R.id.open_gallery);
        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, RESULT_LOAD_IMAGE);
            }
        });
        openGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_LOAD_IMAGE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == RESULT_LOAD_IMAGE) {
                //Get ImageURi and load with help of picasso
                //Uri selectedImageURI = data.getData();
                Picasso.with(FriendlyEditor.this).load(data.getData()).noPlaceholder()
                        .centerCrop()
                        .fit()
                        .into(mCapture);

            }
        }
    }

    private void saveFriend(){

            String name = ((EditText) findViewById(R.id.edit_friend_name)).getText().toString().trim();
            String email = ((EditText) findViewById(R.id.edit_friend_email)).getText().toString().trim();
            String phone = (((EditText) findViewById(R.id.edit_friend_phone)).getText().toString());
            String fbId = ((EditText) findViewById(R.id.edit_friend_fb)).getText().toString().trim();

        if (mCurrentFriendUri == null &&
                TextUtils.isEmpty(name) && TextUtils.isEmpty(email) &&
                TextUtils.isEmpty(phone) && TextUtils.isEmpty(fbId) && mRelationship == FriendlyEntry.REL_UNKNOWN) {
            // Since no fields were modified, we can return early without creating a new pet.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

            ContentValues contentValues = new ContentValues();
            contentValues.put(FriendlyEntry.COLUMN_FRIEND_NAME, name);
            contentValues.put(FriendlyEntry.COLUMN_FRIEND_EMAIL, email);
            contentValues.put(FriendlyEntry.COLUMN_FRIEND_PHONE, phone);
            contentValues.put(FriendlyEntry.COLUMN_FRIEND_FB, fbId);
            contentValues.put(FriendlyEntry.COLUMN_FRIEND_REL, mRelationship);

        if (mCurrentFriendUri == null) {

            Uri uri = getContentResolver().insert(FriendlyEntry.CONTENT_URI, contentValues);
            if (uri != null) {
                Toast.makeText(this, "Friend Saved", Toast.LENGTH_LONG).show();
            }
        } else {

            // Otherwise this is an EXISTING pet, so update the pet with content URI: mCurrentPetUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentPetUri will already identify the correct row in the database that
            // we want to modify.

            int rowsAffected = getContentResolver().update(mCurrentFriendUri, contentValues, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_friend_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_friend_successful),
                        Toast.LENGTH_SHORT).show();
            }

        }
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
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
                    return getContentResolver().query(mCurrentFriendUri,
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
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(FriendlyEntry.COLUMN_FRIEND_NAME);
            int emailColumnIndex = cursor.getColumnIndex(FriendlyEntry.COLUMN_FRIEND_EMAIL);
            int phoneColumnIndex = cursor.getColumnIndex(FriendlyEntry.COLUMN_FRIEND_PHONE);
            int fbColumnIndex = cursor.getColumnIndex(FriendlyEntry.COLUMN_FRIEND_FB);
            int relColumnIndex = cursor.getColumnIndex(FriendlyEntry.COLUMN_FRIEND_REL);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String email = cursor.getString(emailColumnIndex);
            long phone = cursor.getLong(phoneColumnIndex);
            String fb = cursor.getString(fbColumnIndex);
            int rel = cursor.getInt(relColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mEmailEditText.setText(email);
            mPhoneEditText.setText(Long.toString(phone));
            mfbEditText.setText(fb);

            // Gender is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Unknown, 1 is Male, 2 is Female).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (rel) {
                case FriendlyEntry.REL_FAMILY:
                    mRelSpinner.setSelection(1);
                    break;
                case FriendlyEntry.REL_FRIEND:
                    mRelSpinner.setSelection(2);
                    break;
                default:
                    mRelSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void deleteFriend() {
        // Only perform the delete if this is an existing pet.
        if (mCurrentFriendUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentFriendUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_friend_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_friend_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}
