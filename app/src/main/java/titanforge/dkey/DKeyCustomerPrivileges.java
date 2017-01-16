package titanforge.dkey;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import titanforge.dkey.FeedReaderContract.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

/**
 * Created by collyn on 12/5/16.
 */

public class DKeyCustomerPrivileges {

    private static final String TAG = "MasterFunction";

    //REQUEST CUSTOMER TYPE
    public final byte REQ_CUSTOMER_REGISTER = 10;
    public final byte REQ_CUSTOMER_LOCK_CONTROl = 11;
    public final byte REQ_CUSTOMER_CHECK_IN = 12;

    //REQUEST COMMON TYPE
    public final byte REQ_COMMON_DATE = 30;
    public final byte REQ_COMMON_TIME = 31;
    public final byte REQ_COMMON_UNLOCK_VALIDATION = 32;
    public final byte REQ_COMMON_WEIGHT_MATRIX = 33;

    //RESPONSE CUSTOMER TYPE
    public final byte RESP_CUSTOMER_VALID = 60;
    public final byte RESP_CUSTOMER_INVALID = 61;
    public final byte RESP_CUSTOMER_DATE_INVALID = 62;
    public final byte RESP_CUSTOMER_CURR_SEED = 63;
    public final byte RESP_CUSTOMER_DATE_VALID = 64;

    //RESPONSE COMMON TYPE
    public final byte RESP_COMMON_TRANSACTION_COMPLETE = 90;
    public final byte RESP_COMMON_WEIGHT_MATRIX_READY = 91;
    public final byte RESP_COMMON_TRANSACTION_PART_COMPLETE = 92;
    public final byte RESP_COMMON_INVALID_USER = 93;
    public final byte RESP_COMMON_WEIGHT_MATRIX_NOT_SET = 94;
    public final byte RESP_COMMON_DOOR_UNLOCK = 95;
    public final byte RESP_COMMON_UNLOCK_VALIDATION_INVALID = 96;
    public final byte RESP_COMMON_CONNECTION_READY = 97;
    private final byte RESP_COMMON_LOCK_DOOR = 98;
    private final byte RESP_COMMON_UNLOCK_DOOR = 99;
    private final byte RESP_COMMON_OPEN_DOOR = 100;

    private int dataCounter = 0;
    private byte[] dataBuffer = new byte[256];
    private byte lockAction = 0;
    private int[][] weightMatrix = new int[16][16];

    private boolean waitingForData = false;

    private Context context;

    private Cursor mainCursor;
    private FeedReaderDbHelper feedReaderDbHelper;
    private SQLiteDatabase db;

    private String currentCheckInAddress;
    private RBLService rblService;
    private BluetoothGattCharacteristic characteristic;

    private onResponseResultListener responseResultListener = null;
    private onConnectionReady connectionReady = null;

    private final char[][] subtitutionTable = new char[][]{
            {'0','a','c','g','i'},
            {'b','1','C','G','I'},
            {'B','A','2','h','j'},
            {'d','e','f','3','J'},
            {'D','E','F','H','4'},
            {'5','k','m','q','s'},
            {'l','6','M','Q','S'},
            {'L','K','7','r','t'},
            {'n','o','p','8','T'},
            {'N','O','P','R','9'}
    };

    private final String codeFormat = "IIIOOOOOOOOOOOOOOOOSSSSEEEEEEEESSSSOOOOOOOOOOOOOOOOIII";
    private final String rentCodeFormat = "AAABBBCCDDEEFFGGHHIIJJKKLLMMNNOOPPQQRR";

    public DKeyCustomerPrivileges(Context context){
        this.context = context;
        feedReaderDbHelper = new FeedReaderDbHelper(context.getApplicationContext());
    }

    public DKeyCustomerPrivileges(Context context, RBLService rblService, BluetoothGattCharacteristic characteristic){
        this.context = context;
        this.responseResultListener = null;
        this.rblService = rblService;
        this.characteristic = characteristic;
        feedReaderDbHelper = new FeedReaderDbHelper(context.getApplicationContext());
    }

    public void setOnResponseResultListener(onResponseResultListener responseResultListener){
        this.responseResultListener = responseResultListener;
    }

    public void setOnConnectionReady(onConnectionReady connectionReady){
        this.connectionReady = connectionReady;
    }

    public static void prepareDataBase(Context context){
        Date currDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
        String formatDate = dateFormat.format(currDate);

        int[] currentDate = new int[]{
                Integer.valueOf(formatDate.substring(0,2)),
                Integer.valueOf(formatDate.substring(2,4)),
                Integer.valueOf(formatDate.substring(4,8))
        };
        int[] returnedDate;

        Log.e("CURR_DAY", String.valueOf(currentDate[0]));
        Log.e("CURR_MONTH", String.valueOf(currentDate[1]));
        Log.e("CURR_YEAR", String.valueOf(currentDate[2]));

        String returnedString;

        //store the ID of which need to be deleted
        Vector<Long> idND = new Vector<>(1);

        FeedReaderDbHelper feedReaderDbHelper = new FeedReaderDbHelper(context.getApplicationContext());
        SQLiteDatabase db = feedReaderDbHelper.getReadableDatabase();

        String projection[] = {
                TableRenting._ID,
                TableRenting.COLUMN_NAME_ENDDATE,
        };

        Cursor cursor = db.query(
                TableRenting.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        int row = 0;

        if(cursor.moveToFirst()){
            row++;
            Log.e("ROW ", String.valueOf(row));
            returnedString = cursor.getString(1);
            Log.e("RETURN DATE", returnedString);
            returnedDate = new int[] {
                    Integer.valueOf(returnedString.substring(0,2)),
                    Integer.valueOf(returnedString.substring(2,4)),
                    Integer.valueOf(returnedString.substring(4,8))
            };

            Log.e("TAR_DAY", String.valueOf(returnedDate[0]));
            Log.e("TAR_MONTH", String.valueOf(returnedDate[1]));
            Log.e("TAR_YEAR", String.valueOf(returnedDate[2]));

            if(!validateDate(currentDate, returnedDate)){
                idND.add(cursor.getLong(0));
                Log.e("ID ","ADDED");
            }

            while(cursor.moveToNext()){
                row++;
                Log.e("ROW ", String.valueOf(row));
                returnedString = cursor.getString(1);
                returnedDate = new int[] {
                        Integer.valueOf(returnedString.substring(0,2)),
                        Integer.valueOf(returnedString.substring(2,4)),
                        Integer.valueOf(returnedString.substring(4,8))
                };

                if(!validateDate(currentDate, returnedDate)){
                    idND.add(cursor.getLong(0));
                }
            }
        }

        db.close();

        if(idND.isEmpty()){
            return;
        }

        db = feedReaderDbHelper.getWritableDatabase();

        String selection = TableRenting._ID + " = ?";
        if(idND.size() > 1){
            for(int i = 0; i < idND.size() - 1; i++){
                selection = selection + " OR " + TableRenting._ID + " = ?";
            }
        }

        String[] selectionArgs = new String[idND.size()];

        for(int i = 0; i < idND.size(); i++){
            selectionArgs[i] = idND.elementAt(i).toString();
        }

        db.delete(
                TableRenting.TABLE_NAME,
                selection,
                selectionArgs
        );

        db.close();
    }

    private static boolean validateDate (int[] currDate, int[] targetDate){

        if(currDate[2] == targetDate[2]){
            if(currDate[1] == currDate[1]){
                if(currDate[0] > targetDate[0]){
                    return false;
                }
            }else if(currDate[1] > targetDate[1]){
                return false;
            }
        }else if(currDate[2] > targetDate[2]){
            return false;
        }

        return true;
    }

    public interface onConnectionReady{
        public void connectionIsReady ();
    }

    public interface onResponseResultListener{
        public final int CUSTOMER_INVALID = 0;
        public final int CUSTOMER_VALID = 1;

        public final int SUCCESS = 1;
        public final int FAIL = 0;

        public final int DATE_INVALID = 0;
        public final int DATE_VALID = 1;

        public void customerValidation (int resultCode);

        public void keyTransferComplete ();

        public void unlockDoorResponse (int resultCode);

        public void checkInValidation (int resultCode);

    }

    public static void updateName(Context context, long keyID, String newName){
        FeedReaderDbHelper feedReaderDbHelper = new FeedReaderDbHelper(context.getApplicationContext());
        SQLiteDatabase db;
        db = feedReaderDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TableRenting.COLUMN_NAME_KEYNAME, newName);

        String selections = TableRenting._ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(keyID)};
        int totalUpdated;
        totalUpdated = db.update(
                TableRenting.TABLE_NAME,
                values,
                selections,
                selectionArgs
        );


    }

    public void decodeRentCode(String deviceAddress, String rentCode, String keyName){

        StringBuilder inputBuilder = new StringBuilder();
        StringBuilder outputBuilder = new StringBuilder();
        StringBuilder startDateBuilder = new StringBuilder();
        StringBuilder endDateBuilder = new StringBuilder();

        //Input I (6 digit)
        //Output O (32 digit)
        //StarDate S (8 digit)
        //EndDate E (8 digit)

        //format : IIIOOOOOOOOOOOOOOOOSSSSEEEEEEEESSSSOOOOOOOOOOOOOOOOIII

        for(int i = 0; i < 54; i++){
            switch (codeFormat.charAt(i)){
                case 'I':{
                    inputBuilder.append(rentCode.charAt(i));
                }
                break;
                case 'O':{
                    outputBuilder.append(rentCode.charAt(i));
                }
                break;
                case 'S':{
                    startDateBuilder.append(rentCode.charAt(i));
                }
                break;
                case 'E':{
                    endDateBuilder.append(rentCode.charAt(i));
                }
                break;
            }
        }

        Log.e("DECODING_EDATE_RESULT",startDateBuilder.toString());
        Log.e("DECODING_SDATE_RESULT",endDateBuilder.toString());
        Log.e("DECODING_CODE_RESULT",inputBuilder.toString() + outputBuilder.toString());

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date = new Date();

        db = feedReaderDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TableRenting.COLUMN_NAME_KEYNAME, keyName);
        values.put(TableRenting.COLUMN_NAME_ADDRESS, deviceAddress);
        values.put(TableRenting.COLUMN_NAME_STARTDATE, startDateBuilder.toString());
        values.put(TableRenting.COLUMN_NAME_ENDDATE, endDateBuilder.toString());
        values.put(TableRenting.COLUMN_NAME_RENTCODE, inputBuilder.toString() + outputBuilder.toString());
        values.put(TableRenting.COLUMN_NAME_CHECKIN,"notCheckIn");
        values.put(TableRenting.COLUMN_NAME_REDEEMDATE, dateFormat.format(date));

        int rowCount = 0;

        String selections =
                TableRenting.COLUMN_NAME_RENTCODE + " = ? AND " +
                TableRenting.COLUMN_NAME_STARTDATE + " = ? AND " +
                TableRenting.COLUMN_NAME_ENDDATE + " = ?";
        String[] selectionsArgs = new String[]{
                inputBuilder.toString() + outputBuilder.toString(),
                startDateBuilder.toString(),
                endDateBuilder.toString()
        };

        rowCount = db.update(TableRenting.TABLE_NAME, values, selections, selectionsArgs);
        Log.e("UPDATE", String.valueOf(rowCount));
        if(rowCount == 0){
            long key = db.insert(TableRenting.TABLE_NAME, null, values);
            Log.e("KEY", Long.toString(key));
        }

        currentCheckInAddress = deviceAddress;

        db.close();
    }

    public void saveWeightMatrix(String keyAddress, byte[] key){
        db = feedReaderDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TableKey.COLUMN_NAME_ADDRESS, keyAddress);
        values.put(TableKey.COLUMN_NAME_KEY, key);
        values.put(TableKey.COLUMN_NAME_STATUS, "Available");

        int rowCount = 0;

        String selections = TableKey.COLUMN_NAME_ADDRESS + " = ?";
        String[] selectionsArgs = new String[]{keyAddress};

        rowCount = db.update(TableKey.TABLE_NAME, values, selections, selectionsArgs);

        if(rowCount == 0){
            db.insert(TableKey.TABLE_NAME, null, values);
        }

        values = new ContentValues();
        values.put(TableRenting.COLUMN_NAME_CHECKIN, "CheckIn");

        selections = TableRenting.COLUMN_NAME_ADDRESS + " = ?";
        selectionsArgs = new String[]{keyAddress};

        db.update(TableRenting.TABLE_NAME, values, selections, selectionsArgs);

        db.close();
    }

    public void loadWeightMatrix(String deviceAddress){
        db = feedReaderDbHelper.getReadableDatabase();

        String[] projection = {
                TableKey._ID,
                TableKey.COLUMN_NAME_ADDRESS,
                TableKey.COLUMN_NAME_KEY
        };

        String selections = TableKey.COLUMN_NAME_ADDRESS + " = ?";

        String[] selectionsArgs = new String[]{deviceAddress};

        mainCursor = db.query(
                FeedReaderContract.TableKey.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selections,                                // The columns for the WHERE clause
                selectionsArgs,                            // The values for the WHERE clause
                null,                                     // The group the rows
                null,                                     // The filter by row groups
                null                                 // The sort order
        );

        if(mainCursor.moveToFirst()){
            setWeightMatrix(mainCursor.getBlob(2));
        }

        db.close();
    }

    public void customerCheckIn(String deviceAddress){
        db = feedReaderDbHelper.getReadableDatabase();
        byte[] startDateBytes;
        byte[] endDateBytes;
        byte[] tx = new byte[17];
        tx[0] = REQ_CUSTOMER_CHECK_IN;

        String[] projection = {
                TableRenting._ID,
                TableRenting.COLUMN_NAME_ADDRESS,
                TableRenting.COLUMN_NAME_STARTDATE,
                TableRenting.COLUMN_NAME_ENDDATE
        };

        String selections = TableRenting.COLUMN_NAME_ADDRESS + " = ?";

        String[] selectionsArgs = new String[]{deviceAddress};

        mainCursor = db.query(
                TableRenting.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selections,                                // The columns for the WHERE clause
                selectionsArgs,                            // The values for the WHERE clause
                null,                                     // The group the rows
                null,                                     // The filter by row groups
                null                                 // The sort order
        );

        if(mainCursor.moveToFirst()){
            startDateBytes = mainCursor.getString(2).getBytes();
            endDateBytes = mainCursor.getString(3).getBytes();

            for(int i = 0; i < 16 ; i++){
                if(i < 8){
                    tx[i + 1] = startDateBytes[i];
                }else {
                    tx[i + 1] = endDateBytes[i - 8];
                }
            }

            db.close();

            currentCheckInAddress = deviceAddress;
            characteristic.setValue(tx);
            rblService.writeCharacteristic(characteristic);

        }else {
            db.close();
        }
    }

    public void customerRegister(String deviceAddress){
        db = feedReaderDbHelper.getReadableDatabase();

        byte[] inputBytes = new byte[2];
        byte[] outputBytes = new byte[16];
        byte[] tx = new byte[19];

        tx[0] = REQ_CUSTOMER_REGISTER;

        StringBuilder stringBuilder;

        String[] projection = {
                TableRenting._ID,
                TableRenting.COLUMN_NAME_ADDRESS,
                TableRenting.COLUMN_NAME_RENTCODE
        };

        String selections = TableRenting.COLUMN_NAME_ADDRESS + " = ?";

        String[] selectionsArgs = new String[]{deviceAddress};

        mainCursor = db.query(
                TableRenting.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selections,                                // The columns for the WHERE clause
                selectionsArgs,                            // The values for the WHERE clause
                null,                                     // The group the rows
                null,                                     // The filter by row groups
                null                                 // The sort order
        );

        if(mainCursor.moveToFirst()){
            String rentCode = mainCursor.getString(2);

            int inputCounter = 0;
            int outputCounter = 0;
            int bufferInt = 0;

            char lastChar = 'A';
            stringBuilder = new StringBuilder();

            Log.e("RENTCODE", rentCode);

            //Rent Code Format = AAABBBCCDDEEFFGGHHIIJJKKLLMMNNOOPPQQRR

            for(int i = 0; i < 38; i++){
                if(lastChar != rentCodeFormat.charAt(i) || i == 37){
                    if(inputCounter < 2){
                        bufferInt = Integer.valueOf(stringBuilder.toString());
                        inputBytes[inputCounter] = (byte)bufferInt;
                        Log.e("INPUT_BYTE", new String(inputBytes));
                        inputCounter++;
                    }else if(i == 37){
                        stringBuilder.append(rentCode.charAt(i));
                        bufferInt = Integer.valueOf(stringBuilder.toString()) + 50;
                        outputBytes[outputCounter] = (byte)bufferInt;
                        Log.e("OUTPUT_BYTE", new String(outputBytes));
                    }else {
                        bufferInt = Integer.valueOf(stringBuilder.toString()) + 50;
                        outputBytes[outputCounter] = (byte)bufferInt;
                        Log.e("OUTPUT_BYTE", new String(outputBytes));
                        outputCounter++;
                    }
                    stringBuilder = new StringBuilder();
                    stringBuilder.append(rentCode.charAt(i));
                }else {
                    stringBuilder.append(rentCode.charAt(i));
                }
                lastChar = rentCodeFormat.charAt(i);
            }

            db.close();

            for(int i = 0; i < 18; i++){
                if(i < 2){
                    tx[i + 1] = inputBytes[i];
                }else{
                    tx[i + 1] = outputBytes[i - 2];
                }
            }

            characteristic.setValue(tx);
            rblService.writeCharacteristic(characteristic);

        }else {
            db.close();
        }
    }

    public void setWeightMatrix(byte[] weightMatrix){

        int resultIndex = 0;
        int weightIndex = 0;
        int counter = 0;

        for(resultIndex = 0; resultIndex < 16; resultIndex++){
            for(weightIndex = 0; weightIndex < 16; weightIndex++, counter++){
                this.weightMatrix[resultIndex][weightIndex] = weightMatrix[counter] & 0xFF;
            }
        }
    }


    public void requestWeightMatrix(){
        byte[] tx = {REQ_COMMON_WEIGHT_MATRIX};
        characteristic.setValue(tx);
        rblService.writeCharacteristic(characteristic);
    }

    public void requestCustomerUnlock(){
        byte[] tx = {REQ_CUSTOMER_LOCK_CONTROl};
        lockAction = RESP_COMMON_UNLOCK_DOOR;
        characteristic.setValue(tx);
        rblService.writeCharacteristic(characteristic);
    }

    public void requestCustomerLock(){
        byte[] tx = {REQ_CUSTOMER_LOCK_CONTROl};
        lockAction = RESP_COMMON_LOCK_DOOR;
        characteristic.setValue(tx);
        rblService.writeCharacteristic(characteristic);
    }

    public void requestCustomerOpen(){
        byte[] tx = {REQ_CUSTOMER_LOCK_CONTROl};
        lockAction = RESP_COMMON_OPEN_DOOR;
        characteristic.setValue(tx);
        rblService.writeCharacteristic(characteristic);
    }

    public void requestUnlockValidation(byte[] result){
        byte[] tx = new byte[18];

        //put request code
        tx[0] = REQ_COMMON_UNLOCK_VALIDATION;

        //put the rest of the data
        for(int i = 0; i < 16; i++){
            tx[i + 1] = result[i];
        }
        tx[17] = lockAction;

        characteristic.setValue(tx);
        rblService.writeCharacteristic(characteristic);

    }

    public void responseHandler(byte[] response, String deviceAddress){
        if(response[1] == 0){
            switch (response[0]){
                case RESP_COMMON_CONNECTION_READY:{
                    if(connectionReady != null) {
                        connectionReady.connectionIsReady();
                    }
                }
                break;
                case RESP_COMMON_TRANSACTION_COMPLETE:{
                    waitingForData = false;
                    dataCounter = 0;

                    setWeightMatrix(dataBuffer);
                    saveWeightMatrix(deviceAddress, dataBuffer);

                    Log.i(TAG,"Transaction Complete");
                    responseResultListener.keyTransferComplete();
                }
                break;
                case RESP_COMMON_WEIGHT_MATRIX_READY:{
                    waitingForData = true;

                    requestWeightMatrix();
                    Log.i(TAG,"Weight Matrix Ready");
                }
                break;
                case RESP_CUSTOMER_CURR_SEED:{
                    int currSeed[] = {response[2] & 0xFF, response[3] & 0xFF};
                    byte result[] = keyGenerator(currSeed);
                    requestUnlockValidation(result);
                }
                break;
                case RESP_COMMON_DOOR_UNLOCK:{
                    responseResultListener.unlockDoorResponse(onResponseResultListener.SUCCESS);
                }
                break;
                case RESP_COMMON_UNLOCK_VALIDATION_INVALID:{
                    responseResultListener.unlockDoorResponse(onResponseResultListener.FAIL);
                }
                break;
                case RESP_CUSTOMER_DATE_INVALID:{
                    responseResultListener.checkInValidation(onResponseResultListener.DATE_INVALID);
                }
                break;
                case RESP_CUSTOMER_DATE_VALID:{
                    responseResultListener.checkInValidation(onResponseResultListener.DATE_VALID);
                    customerRegister(currentCheckInAddress);
                }
                break;
                case RESP_CUSTOMER_VALID:{
                    responseResultListener.customerValidation(onResponseResultListener.CUSTOMER_VALID);
                }
                break;
                case RESP_CUSTOMER_INVALID:{
                    responseResultListener.customerValidation(onResponseResultListener.CUSTOMER_INVALID);
                }
                break;
            }
        }else if(waitingForData){
            int length = 0;
            byte tmp;
            try {

                for(int i = 0; i < 20; i++){
                    tmp = response[i];
                    length++;
                }

            }catch (Exception e){
                e.printStackTrace();
            }

            for(int i = 0; i < length; i++) {
                dataBuffer[dataCounter] = response[i];
                dataCounter++;
                Log.e("Data Counter ",String.valueOf(dataCounter));
            }

            //only send a request after processing 64 byte
            if(dataCounter % 64 == 0) {
                Log.e("Data Counter ","Triggered");
                requestWeightMatrix();
            }
        }
    }

    public byte[] keyGenerator(int[] seedArray){

        byte[] result = new byte[16];
        int[] seed = new int[2];
        int[] seedBit = new int[16];

        double resultBit = 0;
        int resultIndex = 0;
        int inputIndex = 0;
        //Initialize
        seed[0] = seedArray[0];
        seed[1] = seedArray[1];

        result[0] = 0;
        result[1] = 0;

        for(int i = 0; i < 16; i ++){
            if(i < 8){
                seedBit[i] = seed[0] % 2;
                seed[0] = seed[0] / 2;
            }else {
                seedBit[i] = seed[1] % 2;
                seed[1] = seed[1] / 2;
            }
        }

        for(resultIndex = 0; resultIndex < 16; resultIndex++){
            resultBit = 0;
            for(inputIndex = 0; inputIndex < 16; inputIndex++){
                resultBit += seedBit[inputIndex] * (weightMatrix[resultIndex][inputIndex]/200);
            }

            resultBit = 1 / (1 + Math.exp(-resultBit));

            result[resultIndex] = (byte)((int)(resultBit * 100));

        }

        return  result;

    }

    public void closeConnection(){
        if(rblService != null) {
            rblService.disconnect();
            rblService.close();

            rblService = null;
        }
    }

}
