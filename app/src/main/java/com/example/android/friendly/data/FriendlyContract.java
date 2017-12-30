package com.example.android.friendly.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by ricHVision on 11/20/2017.
 */

public class FriendlyContract {

    public final static String AUTHORITY = "com.example.android.friendly";
    public final static Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public final static String FRIEND_PATH = "friends";

    private FriendlyContract(){}

public static final class FriendlyEntry implements BaseColumns {

    public final static Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(FRIEND_PATH).build();

    public final static String TABLE_NAME = "friends";

    public final static String _ID = BaseColumns._ID;

    public final static String COLUMN_FRIEND_NAME ="name";

    public final static String COLUMN_FRIEND_EMAIL ="email";

    public final static String COLUMN_FRIEND_PHONE ="phone";

    public final static String COLUMN_FRIEND_FB ="facebookId";

    public final static String COLUMN_FRIEND_REL ="relationship";

    public static final int REL_UNKNOWN = 0;
    public static final int REL_FAMILY = 1;
    public static final int REL_FRIEND = 2;


    }

}
