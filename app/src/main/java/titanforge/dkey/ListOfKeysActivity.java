package titanforge.dkey;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.view.MenuItem;
import android.view.View;

import titanforge.dkey.FeedReaderContract.TableRenting;

import java.util.ArrayList;
import java.util.List;

public class ListOfKeysActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private List<ListOfKeysList> keyList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ListOfKeysAdapter mAdapter;
    private DrawerLayout drawerLayout;
    private Snackbar snackbar;

    private FeedReaderDbHelper feedReaderDbHelper;
    private SQLiteDatabase db;
    private Cursor mainCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_keys);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_list_of_keys);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout,toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        feedReaderDbHelper = new FeedReaderDbHelper(this);

        recyclerView = (RecyclerView) findViewById(R.id.list_of_keys);

        mAdapter = new ListOfKeysAdapter(keyList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);


        prepareKeyData();
    }

    private void prepareKeyData() {
        DKeyCustomerPrivileges.prepareDataBase(this);
        keyList.clear();

        db = feedReaderDbHelper.getReadableDatabase();

        String projections[] = {
                TableRenting._ID,
                TableRenting.COLUMN_NAME_KEYNAME,
                TableRenting.COLUMN_NAME_STARTDATE,
                TableRenting.COLUMN_NAME_ENDDATE,
                TableRenting.COLUMN_NAME_REDEEMDATE
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
            keyList.add(new ListOfKeysList(mainCursor.getString(1), mainCursor.getString(2), mainCursor.getString(3), mainCursor.getString(4)));
            while(mainCursor.moveToNext()){
                keyList.add(new ListOfKeysList(mainCursor.getString(1), mainCursor.getString(2), mainCursor.getString(3), mainCursor.getString(4)));
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
