package titanforge.dkey;

import android.provider.BaseColumns;

/**
 * Created by collyn on 11/29/16.
 */

public class FeedReaderContract {

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private FeedReaderContract() {

    }

    /* Inner class that defines the table contents */
    public static class TableKey implements BaseColumns {
        public static final String TABLE_NAME = "Key";
        public static final String COLUMN_NAME_KEYNAME = "keyName";
        public static final String COLUMN_NAME_ADDRESS = "address";
        public static final String COLUMN_NAME_KEY = "key";
        public static final String COLUMN_NAME_DESC = "description";
        public static final String COLUMN_NAME_STATUS = "keyStatus";
    }

    public static class TableRenting implements BaseColumns {
        public static final String TABLE_NAME = "Renting";
        public static final String COLUMN_NAME_KEYNAME = "keyName";
        public static final String COLUMN_NAME_ADDRESS = "address";
        public static final String COLUMN_NAME_STARTDATE = "startDate";
        public static final String COLUMN_NAME_ENDDATE = "endDate";
        public static final String COLUMN_NAME_RENTCODE = "rentCode";
        public static final String COLUMN_NAME_CHECKIN = "checkIn";
        public static final String COLUMN_NAME_REDEEMDATE = "redeemDate";
    }


}
