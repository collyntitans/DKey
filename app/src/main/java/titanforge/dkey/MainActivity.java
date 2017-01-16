package titanforge.dkey;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import titanforge.dkey.FeedReaderContract.TableRenting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private static final String TAG = "MainActivity";

    private final int ACTION_CHECK_IN = 0;
    private final int ACTION_UNLOCK = 1;
    private final int ACTION_LOCK = 2;
    private final int ACTION_OPEN = 3;

    private int currentAction;

    private List<CustListContent> doorList = new ArrayList<>();
    private RecyclerView recyclerView;
    private custListAdapter mAdapter;
    private CustListContent custListContent;
    private Snackbar snackbar;
    private DrawerLayout drawerLayout;
    private Dialog dialog;

    private final int PERMISSIONS_REQUEST_CODE = 666;
    private Vector<String> NEEDED_PERMISSION;

    private FeedReaderDbHelper feedReaderDbHelper;
    private SQLiteDatabase db;
    private Cursor mainCursor;

    private final int ENABLE_BLUETOOTH_CODE = 1;

    private BluetoothAdapter bluetoothAdapter;

    private RBLService mBluetoothLeService;
    private DKeyCustomerPrivileges dKeyCustomerPrivileges;
    private Map<UUID, BluetoothGattCharacteristic> map = new HashMap<>();
    private BluetoothGattCharacteristic characteristic;
    private String deviceAddress;
    private String deviceName;

    private final ServiceConnection connectionService = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mBluetoothLeService = ((RBLService.LocalBinder) iBinder).getService();
            Log.e("SERVICE", "Service Connected");
            if(!mBluetoothLeService.initialize()){
                Log.e(TAG, "Unable to initialize rblService");
                finish();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(RBLService.ACTION_GATT_CONNECTED)) {


            }else if(action.equals(RBLService.ACTION_GATT_DISCONNECTED)){

            }else if(action.equals(RBLService.ACTION_GATT_SERVICES_DISCOVERED)){
                getGattService(mBluetoothLeService.getSupportedGattService());

                characteristic = map.get(RBLService.UUID_BLE_SHIELD_TX);
                dKeyCustomerPrivileges = new DKeyCustomerPrivileges(getApplicationContext(), mBluetoothLeService, characteristic);
                dKeyCustomerPrivileges.setOnResponseResultListener(responseResultListener);
                dKeyCustomerPrivileges.setOnConnectionReady(connectionReady);

            }else if(action.equals(RBLService.ACTION_DATA_AVAILABLE)){
                dKeyCustomerPrivileges.responseHandler(
                        intent.getByteArrayExtra(RBLService.EXTRA_DATA),
                        deviceAddress
                );
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);

        final BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(BLUETOOTH_SERVICE);

        dialog = new Dialog(this);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        NEEDED_PERMISSION = new Vector<>(10);

        recyclerView = (RecyclerView) findViewById(R.id.main_door_list);
        mAdapter = new custListAdapter(doorList);

        mAdapter.setOnActionClickListener(new custListAdapter.onActionClickListener() {
            @Override
            public void onAction(String doorName, boolean isCheckIn, String deviceAddress) {
                MainActivity.this.deviceName = doorName;
                MainActivity.this.deviceAddress = deviceAddress;

                if(!bluetoothAdapter.isEnabled()) {
                    Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBluetoothIntent, ENABLE_BLUETOOTH_CODE);
                }else {
                    if(mBluetoothLeService == null){
                        Snackbar.make(drawerLayout, "Preparing Bluetooth", Snackbar.LENGTH_LONG).show();
                    }else {
                        if (!isCheckIn) {
                            dialog.setContentView(R.layout.dialog_pairing);
                            dialog.show();

                            currentAction = ACTION_CHECK_IN;
                            mBluetoothLeService.connect(deviceAddress);
                        } else {

                            currentAction = ACTION_OPEN;
                            mBluetoothLeService.connect(deviceAddress);
                        }
                    }
                }
            }

            @Override
            public void onLock(String doorName, String deviceAddress) {
                MainActivity.this.deviceName = doorName;
                MainActivity.this.deviceAddress = deviceAddress;

                currentAction = ACTION_LOCK;
                mBluetoothLeService.connect(deviceAddress);
            }

            @Override
            public void onUnlock(String doorName, String deviceAddress) {
                MainActivity.this.deviceName = doorName;
                MainActivity.this.deviceAddress = deviceAddress;

                currentAction = ACTION_UNLOCK;
                mBluetoothLeService.connect(deviceAddress);
            }
        });


        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        if(!getPackageManager().hasSystemFeature(getPackageManager().FEATURE_BLUETOOTH_LE)){
            Toast.makeText(this,"Bluetooth Low Energy Not Supported",Toast.LENGTH_LONG).show();

        }else {
            bluetoothAdapter = bluetoothManager.getAdapter();
            if(bluetoothAdapter.isEnabled()){
                Intent gattServiceIntent = new Intent(this, RBLService.class);
                bindService(gattServiceIntent, connectionService, BIND_AUTO_CREATE);
            }

        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                NEEDED_PERMISSION.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                NEEDED_PERMISSION.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                NEEDED_PERMISSION.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                NEEDED_PERMISSION.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }

            if (!NEEDED_PERMISSION.isEmpty()) {
                String[] neededPermissionsString = NEEDED_PERMISSION.toArray(new String[NEEDED_PERMISSION.size()]);
                requestPermissions(neededPermissionsString, PERMISSIONS_REQUEST_CODE);
            }else {
                feedReaderDbHelper = new FeedReaderDbHelper(this);
                prepareDoorData();
            }

        }else {
            feedReaderDbHelper = new FeedReaderDbHelper(this);
            prepareDoorData();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSIONS_REQUEST_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                feedReaderDbHelper = new FeedReaderDbHelper(this);
                prepareDoorData();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == ENABLE_BLUETOOTH_CODE){
            if(resultCode == RESULT_OK){
                new Thread(){
                    @Override
                    public void run() {

                        try{
                            Thread.sleep(2000);
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent gattServiceIntent = new Intent(MainActivity.this, RBLService.class);
                                bindService(gattServiceIntent, connectionService, BIND_AUTO_CREATE);
                            }
                        });
                    }
                };
            }
        }
    }

    private void prepareDoorData() {
        DKeyCustomerPrivileges.prepareDataBase(this);

        db = feedReaderDbHelper.getReadableDatabase();

        doorList.clear();

        String projections[] = {
                TableRenting._ID,
                TableRenting.COLUMN_NAME_KEYNAME,
                TableRenting.COLUMN_NAME_CHECKIN,
                TableRenting.COLUMN_NAME_ADDRESS,
                TableRenting.COLUMN_NAME_STARTDATE,
                TableRenting.COLUMN_NAME_ENDDATE
        };

        mainCursor = db.query(
                TableRenting.TABLE_NAME,
                projections,
                null,
                null,
                null,
                null,
                null);

        String rentDate;
        String startDate;
        String endDate;

        if(mainCursor.moveToFirst()){
            startDate = mainCursor.getString(4);
            startDate = startDate.substring(0,2) + "-" + startDate.substring(2,4) + "-" + startDate.substring(4,8);
            endDate = mainCursor.getString(5);
            endDate = endDate.substring(0,2) + "-" + endDate.substring(2,4) + "-" + endDate.substring(4,8);
            rentDate = "Rent Date : " + startDate + " Until : " + endDate;
            doorList.add(new CustListContent(mainCursor.getString(1), mainCursor.getString(2), mainCursor.getString(3), rentDate));
            while(mainCursor.moveToNext()){
                startDate = mainCursor.getString(4);
                startDate = startDate.substring(0,2) + "-" + startDate.substring(2,4) + "-" + startDate.substring(4,8);
                endDate = mainCursor.getString(5);
                endDate = endDate.substring(0,2) + "-" + endDate.substring(2,4) + "-" + endDate.substring(4,8);
                rentDate = "Rent Date : " + startDate + " Until : " + endDate;
                doorList.add(new CustListContent(mainCursor.getString(1), mainCursor.getString(2), mainCursor.getString(3), rentDate));
            }
        }else {
            snackbar = Snackbar.make(drawerLayout, getString(R.string.empty_key_list), Snackbar.LENGTH_INDEFINITE)
                    .setAction("Dismiss", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            snackbar.dismiss();
                        }
                    });
            snackbar.show();
        }

        db.close();

        mAdapter.notifyDataSetChanged();
    }

    public void go_to_redeem(View view) {
        Intent intent = new Intent(this, RedeemNewKeyActivity.class);
        startActivity(intent);
    }

    DKeyCustomerPrivileges.onResponseResultListener responseResultListener = new DKeyCustomerPrivileges.onResponseResultListener() {

        @Override
        public void customerValidation(int resultCode) {
            if(resultCode == CUSTOMER_INVALID){
                Snackbar.make(drawerLayout,"Check-In Unsuccessful",Snackbar.LENGTH_LONG).show();
                dialog.dismiss();
                mBluetoothLeService.disconnect();
            }else {
                Snackbar.make(drawerLayout,"Check-In in Progress",Snackbar.LENGTH_LONG).show();
            }
        }

        @Override
        public void keyTransferComplete() {
            Snackbar.make(drawerLayout,"Check-In Complete",Snackbar.LENGTH_LONG).show();
            dialog.dismiss();
            mBluetoothLeService.disconnect();

            prepareDoorData();
        }

        @Override
        public void unlockDoorResponse(int resultCode) {
            if(resultCode == SUCCESS){
                if (currentAction == ACTION_UNLOCK) {
                    Snackbar.make(drawerLayout,"Unlock Successful",Snackbar.LENGTH_LONG).show();
                }else if (currentAction == ACTION_LOCK) {
                    Snackbar.make(drawerLayout,"Lock Successful",Snackbar.LENGTH_LONG).show();
                }else if (currentAction == ACTION_OPEN) {
                    Snackbar.make(drawerLayout,"Open Successful",Snackbar.LENGTH_LONG).show();
                }
            }else{
                if (currentAction == ACTION_UNLOCK) {
                    Snackbar.make(drawerLayout,"Unlock Failed",Snackbar.LENGTH_LONG).show();
                }else if (currentAction == ACTION_LOCK) {
                    Snackbar.make(drawerLayout,"Lock Failed",Snackbar.LENGTH_LONG).show();
                }else if (currentAction == ACTION_OPEN) {
                    Snackbar.make(drawerLayout,"Open Failed",Snackbar.LENGTH_LONG).show();
                }
            }
            mBluetoothLeService.disconnect();
        }

        @Override
        public void checkInValidation(int resultCode) {
            if(resultCode == DATE_INVALID){
                Snackbar.make(drawerLayout,"Unable to perform Check-In",Snackbar.LENGTH_LONG).show();
                dialog.dismiss();
                mBluetoothLeService.disconnect();
            }
        }
    };

    DKeyCustomerPrivileges.onConnectionReady connectionReady = new DKeyCustomerPrivileges.onConnectionReady() {
        @Override
        public void connectionIsReady() {
            if(currentAction == ACTION_CHECK_IN){
                dKeyCustomerPrivileges.customerCheckIn(deviceAddress);
                Snackbar.make(drawerLayout,"Checking-In To " + deviceName,Snackbar.LENGTH_LONG).show();
            }else if (currentAction == ACTION_UNLOCK) {
                dKeyCustomerPrivileges.loadWeightMatrix(deviceAddress);
                dKeyCustomerPrivileges.requestCustomerUnlock();
                Snackbar.make(drawerLayout,"Unlocking " + deviceName,Snackbar.LENGTH_LONG).show();
            }else if (currentAction == ACTION_LOCK) {
                dKeyCustomerPrivileges.loadWeightMatrix(deviceAddress);
                dKeyCustomerPrivileges.requestCustomerLock();
                Snackbar.make(drawerLayout,"Locking " + deviceName,Snackbar.LENGTH_LONG).show();
            }else if (currentAction == ACTION_OPEN) {
                dKeyCustomerPrivileges.loadWeightMatrix(deviceAddress);
                dKeyCustomerPrivileges.requestCustomerOpen();
                Snackbar.make(drawerLayout,"Opening " + deviceName,Snackbar.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        if(feedReaderDbHelper != null) {
            prepareDoorData();
        }
        registerReceiver(mGattUpdateListener, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(dKeyCustomerPrivileges != null) {
            dKeyCustomerPrivileges.closeConnection();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(mGattUpdateListener);
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_unlock_door) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);

        } else if(id == R.id.nav_list_of_keys) {
            Intent intent = new Intent(this, ListOfKeysActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_change_keys_name) {
            Intent intent = new Intent(this, ChangeKeysNameActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_exit) {
            finishAffinity();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void getGattService(BluetoothGattService gattService) {
        if (gattService == null) {
            return;
        }

        BluetoothGattCharacteristic characteristic = gattService.getCharacteristic(RBLService.UUID_BLE_SHIELD_TX);
        map.put(characteristic.getUuid(), characteristic);

        BluetoothGattCharacteristic characteristicRx = gattService.getCharacteristic(RBLService.UUID_BLE_SHIELD_RX);
        mBluetoothLeService.setCharacteristicNotification(characteristicRx, true);
        mBluetoothLeService.readCharacteristic(characteristicRx);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(RBLService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(RBLService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(RBLService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(RBLService.ACTION_DATA_AVAILABLE);

        return intentFilter;
    }
}
