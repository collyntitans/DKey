package titanforge.dkey;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class RedeemNewKeyActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private static final String HOST_NAME = "DKey.Rent";

    Boolean isKeyCodeFilled = false;
    Boolean isRoomNameFilled = false;
    Boolean isRoomNameMore3 = false;
    Boolean isRoomNameLess10 = false;

    EditText editKeyCode;
    EditText editRoomName;

    FloatingActionButton fab;

    DKeyCustomerPrivileges dKeyCustomerPrivileges;

    private String deviceAddress;
    private String rentCode;

    private boolean fromLink = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redeem_new_key);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent linkIntent = getIntent();
        if(linkIntent != null){
            if(linkIntent.getData() != null){
                rentCode = linkIntent.getData().toString();
                rentCode = rentCode.replace("http://", "");
                fromLink = true;
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer,toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        dKeyCustomerPrivileges = new DKeyCustomerPrivileges(this);

        fab = (FloatingActionButton) findViewById(R.id.redeem_floating_action_button);

        fab.setAlpha(.5f);
        fab.setEnabled(false);

        fab.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){

                String result[] = editKeyCode.getText().toString().split("/");

                dKeyCustomerPrivileges.decodeRentCode(result[1],result[2],editRoomName.getText().toString());

                Intent intent = new Intent(RedeemNewKeyActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });



        editKeyCode = (EditText) findViewById(R.id.edit_key_code);
        editRoomName = (EditText) findViewById(R.id.edit_room_name);

        editKeyCode.addTextChangedListener(new TextWatcher() {
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

        if(fromLink) {
            editKeyCode.setText(rentCode);
            editKeyCode.setEnabled(false);
        }

        editRoomName.addTextChangedListener(new TextWatcher() {
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



    }

    public void enableButtonIfReady(){
        isKeyCodeFilled = !editKeyCode.getText().toString().isEmpty();
        isRoomNameFilled = !editRoomName.getText().toString().isEmpty();
        isRoomNameMore3 = editRoomName.getText().toString().length()>2;
        isRoomNameLess10 = editRoomName.getText().toString().length()<11;
        if(isKeyCodeFilled && isRoomNameFilled && isRoomNameMore3 && isRoomNameLess10){
            fab.setAlpha(1f);
            fab.setEnabled(true);
        }
        else {
            fab.setAlpha(.5f);
            fab.setEnabled(false);
        }
    }

//    public void go_to_main (View view){
//        Intent intent = new Intent(this, MainActivity.class);
//        startActivity(intent);
//    }

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
