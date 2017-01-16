package titanforge.dkey;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import titanforge.dkey.FeedReaderContract.TableRenting;

import java.util.ArrayList;
import java.util.List;

public class ChangeKeysNameActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    Boolean isListSelected = false;
    Boolean isRoomNameFilled = false;
    Boolean isRoomNameMore3 = false;
    Boolean isRoomNameLess10 = false;
    EditText editChangeRoomName;
    FloatingActionButton fab;

    private DrawerLayout drawerLayout;
    private List<ChangeKeyNameListView> keyList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ChangeKeyNameListAdapter mAdapter;
    private Snackbar snackbar;

    private FeedReaderDbHelper feedReaderDbHelper;
    private SQLiteDatabase db;
    private Cursor mainCursor;
    private long selectedKeyID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_keys_name);
        Toolbar toolbar = (Toolbar) findViewById(R.id.change_keys_name_toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout,toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        recyclerView = (RecyclerView) findViewById(R.id.list_change_keys_name);

        mAdapter = new ChangeKeyNameListAdapter(keyList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        mAdapter.setOnTapListener(new ChangeKeyNameListAdapter.OnTapListener() {
            @Override
            public void onTap(String keyName, long keyID) {
                isListSelected = true;
                enableButtonIfReady();
                selectedKeyID = keyID;
            }
        });

        feedReaderDbHelper = new FeedReaderDbHelper(this.getApplicationContext());

        prepareKeyData();

        editChangeRoomName = (EditText) findViewById(R.id.edit_change_room_name);

        fab = (FloatingActionButton) findViewById(R.id.change_name_floating_action_button);

        fab.setAlpha(.5f);
        fab.setEnabled(false);

        editChangeRoomName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                enableButtonIfReady();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        fab.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                DKeyCustomerPrivileges.updateName(ChangeKeysNameActivity.this, selectedKeyID, editChangeRoomName.getText().toString());
                prepareKeyData();
            }
        });

    }

    private void prepareKeyData() {

        DKeyCustomerPrivileges.prepareDataBase(this);

        db = feedReaderDbHelper.getReadableDatabase();

        keyList.clear();

        String projections[] = {
                TableRenting._ID,
                TableRenting.COLUMN_NAME_KEYNAME
        };

        mainCursor = db.query(
                TableRenting.TABLE_NAME,
                projections,
                null,
                null,
                null,
                null,
                null);

        if(mainCursor.moveToFirst()){
            keyList.add(new ChangeKeyNameListView(mainCursor.getString(1), mainCursor.getLong(0)));
            while(mainCursor.moveToNext()){
                keyList.add(new ChangeKeyNameListView(mainCursor.getString(1), mainCursor.getLong(0)));
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

    public void enableButtonIfReady(){
        isRoomNameFilled = !editChangeRoomName.getText().toString().isEmpty();
        isRoomNameMore3 = editChangeRoomName.getText().toString().length()>2;
        isRoomNameLess10 = editChangeRoomName.getText().toString().length()<11;
        if(isListSelected && isRoomNameFilled && isRoomNameMore3 && isRoomNameLess10){
            fab.setAlpha(1f);
            fab.setEnabled(true);
        }
        else {
            fab.setAlpha(.5f);
            fab.setEnabled(false);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
}
