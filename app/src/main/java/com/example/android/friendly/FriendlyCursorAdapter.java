package com.example.android.friendly;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.friendly.data.FriendlyContract.FriendlyEntry;

/**
 * Created by ricHVision on 11/20/2017.
 */

public class FriendlyCursorAdapter extends CursorAdapter {

    public FriendlyCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = view.findViewById(R.id.username);
        TextView emailTextView = view.findViewById(R.id.email);

        // Find the columns of pet attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(FriendlyEntry.COLUMN_FRIEND_NAME);
        int emailColumnIndex = cursor.getColumnIndex(FriendlyEntry.COLUMN_FRIEND_EMAIL);

        // Read the pet attributes from the Cursor for the current pet
        String friendName = cursor.getString(nameColumnIndex);
        String friendEmail = cursor.getString(emailColumnIndex);

        // If the pet breed is empty string or null, then use some default text
        // that says "Unknown breed", so the TextView isn't blank.
        if (TextUtils.isEmpty(friendEmail)) {
            friendEmail = context.getString(R.string.unknown_email);
        }

        // Update the TextViews with the attributes for the current pet
        nameTextView.setText(friendName);
        emailTextView.setText(friendEmail);
    }
}
