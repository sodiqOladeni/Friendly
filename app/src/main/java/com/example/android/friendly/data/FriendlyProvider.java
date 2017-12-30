package com.example.android.friendly.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.example.android.friendly.data.FriendlyContract.FriendlyEntry;


/**
 * Created by ricHVision on 11/20/2017.
 */

public class FriendlyProvider extends ContentProvider {

    public final static int FRIENDS = 1000;
    public final static int FRIENDS_WITH_ID = 1001;
    public final static UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(FriendlyContract.AUTHORITY, FriendlyContract.FRIEND_PATH, FRIENDS);
        uriMatcher.addURI(FriendlyContract.AUTHORITY, FriendlyContract.FRIEND_PATH + "/#", FRIENDS_WITH_ID);

        return uriMatcher;
    }

    private FriendlyDbHelper mFriendlyDbHelper;
    @Override
    public boolean onCreate() {
        Context context = getContext();
        mFriendlyDbHelper = new FriendlyDbHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        final SQLiteDatabase db = mFriendlyDbHelper.getReadableDatabase();

        int match = sUriMatcher.match(uri);

        Cursor retCursor;
        switch (match){
            case FRIENDS:
                retCursor = db.query(FriendlyEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case FRIENDS_WITH_ID:
                String id = uri.getPathSegments().get(1);

                String mSelection = "_id=?";
                String[] mSelectionArgs = new String[]{id};

                retCursor = db.query(FriendlyEntry.TABLE_NAME,
                        projection,
                        mSelection,
                        mSelectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
                default:
                    throw new UnsupportedOperationException("Unknown Uri");
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final SQLiteDatabase db = mFriendlyDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);

        Uri returnUri;
        switch (match){
            case FRIENDS:
                long id = db.insert(FriendlyEntry.TABLE_NAME, null, contentValues);

                if (id > 0){

                    returnUri = ContentUris.withAppendedId(FriendlyEntry.CONTENT_URI, id);
                }else {
                    throw new android.database.SQLException("Failed to insert row");
                }
                break;

                default:
                    throw new UnsupportedOperationException("Unknown Uri");
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // COMPLETED (1) Get access to the database and write URI matching code to recognize a single item
        final SQLiteDatabase db = mFriendlyDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        // Keep track of the number of deleted tasks
        int friendsDeleted; // starts as 0

        // COMPLETED (2) Write the code to delete a single row of data
        // [Hint] Use selections to delete an item by its row ID
        switch (match) {
            case FRIENDS:
                // Delete all rows that match the selection and selection args
                friendsDeleted = db.delete(FriendlyEntry.TABLE_NAME, selection, selectionArgs);
                break;
            // Handle the single item case, recognized by the ID included in the URI path
            case FRIENDS_WITH_ID:
                // Get the task ID from the URI path
                String id = uri.getPathSegments().get(1);
                // Use selections/selectionArgs to filter for this ID
                friendsDeleted = db.delete(FriendlyEntry.TABLE_NAME, "_id=?", new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // COMPLETED (3) Notify the resolver of a change and return the number of items deleted
        if (friendsDeleted != 0) {
            // A task was deleted, set notification
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of tasks deleted
        return friendsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection,
                      @Nullable String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case FRIENDS:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case FRIENDS_WITH_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = FriendlyEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update pets in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more pets).
     * Return the number of rows that were successfully updated.
     */
    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link PetEntry#COLUMN_PET_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(FriendlyEntry.COLUMN_FRIEND_NAME)) {
            String name = values.getAsString(FriendlyEntry.COLUMN_FRIEND_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Friend requires a name");
            }
        }

        // If the {@link PetEntry#COLUMN_PET_GENDER} key is present,
        // check that the gender value is valid.
        if (values.containsKey(FriendlyEntry.COLUMN_FRIEND_EMAIL)) {
            String email = values.getAsString(FriendlyEntry.COLUMN_FRIEND_EMAIL);
            if (email == null) {
                throw new IllegalArgumentException("Friend requires valid email");
            }
        }

        // If the {@link PetEntry#COLUMN_PET_GENDER} key is present,
        // check that the gender value is valid.
        if (values.containsKey(FriendlyEntry.COLUMN_FRIEND_PHONE)) {
            Integer phone = values.getAsInteger(FriendlyEntry.COLUMN_FRIEND_PHONE);
            if (phone == null) {
                throw new IllegalArgumentException("Friend requires valid Phone Number");
            }
        }

        // If the {@link PetEntry#COLUMN_PET_GENDER} key is present,
        // check that the gender value is valid.
        if (values.containsKey(FriendlyEntry.COLUMN_FRIEND_FB)) {
            String fb = values.getAsString(FriendlyEntry.COLUMN_FRIEND_FB);
            if (fb == null) {
                throw new IllegalArgumentException("Friend requires valid Facebook ID");
            }
        }


        // No need to check the breed, any value is valid (including null).

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mFriendlyDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(FriendlyEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }
}
