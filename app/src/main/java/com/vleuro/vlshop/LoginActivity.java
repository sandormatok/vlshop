package com.vleuro.vlshop;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.CommonStatusCodes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import static com.vleuro.vlshop.Config.DATA_RAKTAR_KESZLET_URL;
import static com.vleuro.vlshop.Config.DATA_RAKTAR_KESZLET_URL_ONLINE;

//import android.view.Gravity;

public class LoginActivity extends AppCompatActivity {

    //*** LOGIN - GLOBÁLIS VÁLTOZÓK ***
    private String globalVevokod, globalPassword, globalVevonev = "";
    private EditText loginInputVevokod, loginInputPassword;
    String globalSsid ="null";
    String wifissid, wifipass, wifissiddev, wifipassdev = "";
    String barcode3, barcode4 = "";
    private CompoundButton maradjonbeBox;
    Boolean allowLogin = true;
    Boolean qrcodeLogin = false;
    public static final String MY_PREFS_NAME = "loginPrefs";
    private static final String TAG = "LoginActivity";
    private static final int RC_QRCODE_LOGIN = 9001;
    private TextView vevonevLogin;
    Boolean devmode  = true;
    Boolean onlinemode = false;

//san suriel ADMIN VEVŐKÓDOK: (egyenlóre a MainActivity-n is meg kell őket adni)
    String[] adminok = new String[]{"0120401"};
    List<String> adminokList = Arrays.asList(adminok);

    //*** LOGIN ONCREATE ***
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //TOOLBAR
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("VL-SCAN Raktár");

        //UI ELEMEK
        loginInputVevokod = findViewById(R.id.input_vevokod);
        loginInputVevokod.setInputType(InputType.TYPE_CLASS_NUMBER);
        loginInputPassword = findViewById(R.id.input_password);

//san.suriel loginInputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        loginInputPassword.setInputType(InputType.TYPE_CLASS_NUMBER);
        vevonevLogin = findViewById(R.id.loginVevonev);
        Button btnlogin = findViewById(R.id.btn_login);
        Button btnQRCode = findViewById(R.id.btn_qrcode);
        maradjonbeBox = findViewById(R.id.maradjonBe);

        //SHARED PREFERENCES
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String prefsVevokod = prefs.getString("vevokod", "");
        String prefsVevojelszo = prefs.getString("vevojelszo", "");
        String prefsVevonev = prefs.getString("vevonev", "");
        onlinemode = prefs.getBoolean("onlinemode", false);


        vevonevLogin.setText(prefsVevonev);
        Boolean prefsMaradjonbe = prefs.getBoolean("maradjon", false);
        if(prefsMaradjonbe) maradjonbeBox.setChecked(true);
        loginInputVevokod.setText(prefsVevokod, TextView.BufferType.EDITABLE);
        loginInputPassword.setText(prefsVevojelszo, TextView.BufferType.EDITABLE);

        //WIFI KONFIGURÁCIÓ
//san suriel
        wifissid = "VLEURO";
        wifipass = "\"vleurokft\"";
        final WifiConfiguration wifiConfig = new WifiConfiguration();
//san suriel
        wifiConfig.SSID = String.format("\"%s\"", wifissid);
        wifiConfig.preSharedKey = String.format("\"%s\"", wifipass);
        WifiManager wifiManager=(WifiManager)getSystemService(WIFI_SERVICE);

        WifiInfo wifiInfo;
        wifiInfo = wifiManager.getConnectionInfo();

        if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
            globalSsid = wifiInfo.getSSID();
        }

        //Toast toast= Toast.makeText(getApplicationContext(),globalSsid, Toast.LENGTH_LONG);
        // toast.setGravity(Gravity.BOTTOM,0,20); toast.show();

//WIFI ELLENŐRZÉS
// san.suriel       if(!globalSsid.equals(wifissid);
        if (!globalSsid.equals("\"VLEURO\"")) {
            //san suriel security override??
            allowLogin = true;
            View view = View.inflate(this, R.layout.alert_dialog_net, null);

            //CSATLAKOZÁS A VLEURO WIFI-HEZ
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Csatlakozhatok a VL-Euro Kft. hálózatára?");
            final WifiManager wifiManager3 = (WifiManager) getSystemService(WIFI_SERVICE);
            alert.setPositiveButton("Kilépés", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    finish();
                    System.exit(0);
                }
            });
            alert.setNegativeButton("Csatlakozás",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            wifiManager3.setWifiEnabled(true);

                            int netId = wifiManager3.addNetwork(wifiConfig);
                            wifiManager3.disconnect();
                            wifiManager3.enableNetwork(netId, true);
                            wifiManager3.reconnect();
                            allowLogin = true;
                        }
                    });
            String globalSsidCHK = wifiInfo.getSSID();
            //Toast toast = Toast.makeText(getApplicationContext(),globalSsidCHK , Toast.LENGTH_LONG);
            alert.show();
        }

        //BEJELENTKEZÉS GOMB
        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (allowLogin) {
                    loginUser(loginInputVevokod.getText().toString(),
                            loginInputPassword.getText().toString());
                } else {

                    Snackbar.make(findViewById(R.id.logincoordinatorLayout), "Nincs kapcsolat a kiszolgálóval!",
                            Snackbar.LENGTH_LONG)
                            .show();
                }
            }
        });

        //BEJELENTKEZÉS QRKÓDDAL
        btnQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent qrcodeReader = new Intent(LoginActivity.this, BarcodeCaptureActivity.class);
                qrcodeReader.putExtra("qrcodeLogin", true);
                startActivityForResult(qrcodeReader, RC_QRCODE_LOGIN);
                //
            }

        });
    }

    //*** ON ACTIVITY RESULT: RC_QRCODE_LOGIN ***
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_QRCODE_LOGIN) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    barcode3 = data.getStringExtra("barcode3");
                    qrcodeLogin = true;
                    maradjonbeBox.setChecked(true);

                    //QRCODE FELDOLGOZÁSA
                    String[] separated = barcode3.split(":");

                    String qrcodeString1 = "qrcode";
                    String qrcodeString2 = separated[0];

                    //    if (qrcode == "qrcode") {

                    if (qrcodeString2.equals("qrcode")) {
                        globalVevokod = separated[1];
                        globalPassword = separated[2];

                        Snackbar.make(findViewById(R.id.logincoordinatorLayout), "Sikeres bejelentkezés QR Kóddal!",
                                Snackbar.LENGTH_SHORT)
                                .show();

                        loginInputVevokod.setText(globalVevokod, TextView.BufferType.EDITABLE);
                        loginInputPassword.setText(globalPassword, TextView.BufferType.EDITABLE);

                    } else {

                        Snackbar.make(findViewById(R.id.logincoordinatorLayout), "Hibás QR KÓD!",
                                Snackbar.LENGTH_LONG)
                                .show();

                        }

                    Log.d(TAG, "Vonalkód (LoginAvtivity) " + barcode3);
                } else {
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    //*** LOGIN ***
    private void loginUser( final String vevokod, final String password) {

// LOGIN INFO CHECK OVERRIDE allowLogin = false;

        if (qrcodeLogin) {
            getData2();
        } else {
            globalVevokod = vevokod;
            globalPassword = password;

            getData2();
        }

    }


    //*** LOGIN GET DATA ***
    private void getData2() {
        String id = "5997076721852";
        String url = "";

        Snackbar.make(findViewById(R.id.logincoordinatorLayout), globalVevokod,
                Snackbar.LENGTH_SHORT)
                .show();



        //Admin user-ek az online adatbázishoz kapcsolódnak...
        if(adminokList.contains(globalVevokod) && onlinemode) {
            url = DATA_RAKTAR_KESZLET_URL_ONLINE + id + "&vkod=" + globalVevokod;
        } else {
            url = DATA_RAKTAR_KESZLET_URL_ONLINE + id + "&vkod=" + globalVevokod;

//san suriel LOCAL LOGIN OWERRIDE!!! -------------------------------------------
            //url = DATA_RAKTAR_KESZLET_URL + id + "&vkod=" + globalVevokod;
        }

        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //        loading.dismiss();
                showJSON(response);
            }
        },
                new Response.ErrorListener() {
                    @Override
                    //Todo: meg kell nezni, hogy van-e error message mielott tostringet hivsz ra....
                    public void onErrorResponse(VolleyError error) {
//san suriel                        Toast.makeText(LoginActivity.this, error.getMessage().toString(), Toast.LENGTH_LONG).show();
                        Snackbar.make(findViewById(R.id.logincoordinatorLayout), "Nem sikerült kapcsolódni!",
                                Snackbar.LENGTH_LONG)
                                .show();

                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);

// StringRequest Timeout, empty cache
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                3000,
                1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        stringRequest.setShouldCache(false);

        requestQueue.add(stringRequest);
    }

    //*** LOGIN SHOWJSON ***
    private void showJSON(String response) {

        String aroszt = "";
        String vevonev = "";
        String jujel = "";

        //VÁLASZ ADATOK TÁROLÁSA
        try {
            JSONObject jsonObject2 = new JSONObject(response);
            JSONArray result2 = jsonObject2.getJSONArray(Config.JSON_ARRAY);
            JSONObject termekData2 = result2.getJSONObject(0);
            jujel = termekData2.getString(Config.KEY_JUJEL);
            vevonev = termekData2.getString(Config.KEY_VEVONEV);
            aroszt = termekData2.getString(Config.KEY_AROSZT);

            globalVevonev = vevonev;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Toast toast = Toast.makeText(getApplicationContext(),jujel + " = " + globalVevokod , Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER,0,20); toast.show();

        if (globalPassword.equals(jujel)){
            //SAVE TO SHARED PREFERENCES
            if (maradjonbeBox.isChecked()) {
                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("vevokod", globalVevokod);
                editor.putString("vevonev", globalVevonev);
                editor.putString("vevojelszo", globalPassword);
                editor.putBoolean("onlinemode", onlinemode);
                editor.putInt("tippCount", 1);
                editor.putBoolean("maradjon", true);
                editor.apply();
            } else {
                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.clear();
                editor.apply();
            }

            //START MAIN ACTIVITY
            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
            mainIntent.putExtra("intentvevonev", vevonev);
            mainIntent.putExtra("intentaroszt", aroszt);
            mainIntent.putExtra("intentvevokod", globalVevokod);
            mainIntent.putExtra("itentonlinemode", onlinemode);
            setResult(CommonStatusCodes.SUCCESS, mainIntent);
            startActivity(mainIntent);
            finish();
        } else {
            Snackbar.make(findViewById(R.id.logincoordinatorLayout), "Hibás vevőkód, vagy jelszó!",
                    Snackbar.LENGTH_LONG)
                    .show();
        }
    }


    //*** OPTIONS MENU ***
    // todo: kijelentkezés, gyors mód,
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        MenuItem onlinemodeMenu = menu.findItem(R.id.action_online);
        MenuItem logoutMenu = menu.findItem(R.id.action_settings);
        logoutMenu.setVisible(true);
        onlinemodeMenu.setVisible(false);

        //onCreate shared frefs not avaible at menu creation time, so call itt first here...
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        globalVevokod = prefs.getString("vevokod", "");
        onlinemode = prefs.getBoolean("onlinemode", false);

        if(adminokList.contains(globalVevokod)) {
            onlinemodeMenu.setVisible(true);
            onlinemodeMenu.setChecked(onlinemode);
        }

        return true;
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

        if (id == R.id.action_online) {
            if(item.isChecked()){
                // If item already checked then unchecked it
                item.setChecked(false);
                onlinemode = false;
            }else {
                // If item is unchecked then checked it
                item.setChecked(true);
                onlinemode = true;
            }
            // return true;
        }
        return super.onOptionsItemSelected(item);
    }
}