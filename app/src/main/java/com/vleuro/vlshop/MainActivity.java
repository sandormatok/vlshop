package com.vleuro.vlshop;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.vleuro.vlshop.R;
import com.vleuro.vlshop.adapter.DrawerUtil;
import com.vleuro.vlshop.adapter.MessagesAdapter;
import com.vleuro.vlshop.helper.DividerItemDecoration;
import com.vleuro.vlshop.model.Message;
import com.vleuro.vlshop.network.ApiClient;
import com.vleuro.vlshop.network.ApiInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.vleuro.vlshop.Config.DATA_RAKTAR_KESZLET_URL;
import static com.vleuro.vlshop.Config.DATA_RAKTAR_KESZLET_URL_ONLINE;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, MessagesAdapter.MessageAdapterListener {
    private List<Message> messages = new ArrayList<>();
    private RecyclerView recyclerView;
    private MessagesAdapter mAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ActionModeCallback actionModeCallback;
    private ActionMode actionMode;

    //san suriel 09
    private static final int RC_BARCODE_CAPTURE = 9001;
    String globalvevoKod = "";
    String marka, termek, menny, nettoString, afaString, vevonev, url = "";
    Boolean akcios = false;

    // Get the application context
    private Context mContext;
    private Activity mActivity;

    //GLOBÁLIS VÁLTOZÓK
    String bruttoRound, nettoRound, nettototalRound;
    String globalAroszt;
    public static String barcode3;

    //san suriel ADMIN VEVŐKÓDOK: (egyenlóre a LoginActivity-n is meg kell őket adni)
    String[] adminok = new String[]{"0120401"};
    List<String> adminokList = Arrays.asList(adminok);

    private static final String TAG = "BarcodeMain";
    private String manualInput = "NO";
    boolean devMode = false;

    private String m_Text, m_Text2 = "";

    public static final String MY_PREFS_NAME = "MainPrefs";

    //san suriel global variables for showJSON

    String ar = "";


    Double netto = 0.00;
    Double brutto = 0.00;
    Double afa = 0.00;
    //
    String akciosstring;

    //suriel vl_shop ListView array-ek
    ArrayList<String> barcodeList = new ArrayList<String>();
    ArrayList<String> markaList = new ArrayList<String>();
    ArrayList<String> termekList = new ArrayList<String>();
    ArrayList<String> mennyisegList = new ArrayList<String>();
    ArrayList<String> shopammountList = new ArrayList<String>();
    ArrayList<String> nettoStringList = new ArrayList<String>();
    ArrayList<String> afaStringList = new ArrayList<String>();
    ArrayList<String> vevonevList = new ArrayList<String>();
    ArrayList<Boolean> akciosList = new ArrayList<Boolean>();
    //mergedlist
    ArrayList<String> mergedList = new ArrayList<String>();
    //extra ???
    ArrayList<String> barcodeListExtra = new ArrayList<String>();

//san suriel 09
    ArrayList<String> messagesStatic = new ArrayList<String>();
    ArrayList<String> messageStatic = new ArrayList<String>();

    //String Arrays
    String barcodeArray[];
    String barcodeExtraArray[];
    String mennyisegArray[];
    String markaArray[];
    String termekArray[];
    String shopammountArray[];
    String nettoStringArray[];
    String afaStringArray[];
    String vevonevArray[];
    Boolean akciosArray[];

    //mergedArray
    String mergedArray[];
    String messagesstaticArray[];
    String messagestaticArray[];

    //suriel todo: beolvasott lista mentése/visszatöltése (sharedpref, array-XJSON/JSON->array)
    //GET SHARED PREFS

    /*
    SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
    String prefsbarcodeArray = prefs.getString("barcodeArray", "");
    String prefsmennyisegArray = prefs.getString("mennyisegArray", "");
    String markaArray = prefs.getString("markaArray", "");
    String prefsshopammountArray = prefs.getString("shopammountArray", "");
    String prefsnettoStringArray = prefs.getString("nettoStringArray", "");
    String prefsafaStringArray = prefs.getString("afaStringArray", "");
    String prefsvevonevArray = prefs.getString("vevonevArray", "");
    String prefsakciosArray = prefs.getString("akciosArray", "");
    String prefsmergedArray = prefs.getString("mergedArray", "");
    */


    @BindView(R.id.toolbar)
    public Toolbar toolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mContext = getApplicationContext();
        mActivity = MainActivity.this;

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        mAdapter = new MessagesAdapter(this, messages, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);
        actionModeCallback = new ActionModeCallback();

        //MaterialDrawer
        DrawerUtil.getDrawer(this, toolBar);

        // show loader and fetch messages
        swipeRefreshLayout.post(
                new Runnable() {
                    @Override
                    public void run() {
                        getData();

                        //prepare staticmessages array


                        getInbox();



                    }
                }
        );
    }


    /**
     * Fetches mail messages by making HTTP request
     * url: http://api.androidhive.info/json/inbox.json
     */
//san suriel 09 TODO: Kell egy saját JSON request a letárolt eredményekre...
//TODO: Letárolt eredmények...!!! :)
    private void getInbox() {
        swipeRefreshLayout.setRefreshing(true);

        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        Call<List<Message>> call = apiService.getInbox();
        call.enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                // clear the inbox
                //messages.clear();

                // add all the messages
                 messages.addAll(response.body());

                // TODO - avoid looping
                // the loop was performed to add colors to each message
                for (Message message : response.body()) {
                    // generate a random color
                    message.setColor(getRandomMaterialColor("400"));
                    messages.add(message);
                }

                mAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<List<Message>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Unable to fetch json: " + t.getMessage(), Toast.LENGTH_LONG).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    //san suriel 09 - getdata url-ből arraylist az adapternek ... - valahogy le is kel majd tárolni

    /**
     * chooses a random color from array.xml
     */
    private int getRandomMaterialColor(String typeColor) {
        int returnColor = Color.GRAY;
        int arrayId = getResources().getIdentifier("mdcolor_" + typeColor, "array", getPackageName());

        if (arrayId != 0) {
            TypedArray colors = getResources().obtainTypedArray(arrayId);
            int index = (int) (Math.random() * colors.length());
            returnColor = colors.getColor(index, Color.GRAY);
            colors.recycle();
        }
        return returnColor;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            Toast.makeText(getApplicationContext(), "Search...", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        // swipe refresh is performed, fetch the messages again
        getInbox();
    }

    @Override
    public void onIconClicked(int position) {
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback);
        }

        toggleSelection(position);
    }

    @Override
    public void onIconImportantClicked(int position) {
        // Star icon is clicked,
        // mark the message as important
        Message message = messages.get(position);
        message.setImportant(!message.isImportant());
        messages.set(position, message);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMessageRowClicked(int position) {
        // verify whether action mode is enabled or not
        // if enabled, change the row state to activated
        if (mAdapter.getSelectedItemCount() > 0) {
            enableActionMode(position);
        } else {
            // read the message which removes bold from the row
            Message message = messages.get(position);
            message.setRead(true);
            messages.set(position, message);
            mAdapter.notifyDataSetChanged();

            Toast.makeText(getApplicationContext(), "Read: " + message.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRowLongClicked(int position) {
        // long press is performed, enable action mode
        enableActionMode(position);
    }

    private void enableActionMode(int position) {
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback);
        }
        toggleSelection(position);
    }

    private void toggleSelection(int position) {
        mAdapter.toggleSelection(position);
        int count = mAdapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }


    private class ActionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_action_mode, menu);

            // disable swipe refresh if action mode is enabled
            swipeRefreshLayout.setEnabled(false);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    // delete all the selected messages
                    deleteMessages();
                    mode.finish();
                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdapter.clearSelections();
            swipeRefreshLayout.setEnabled(true);
            actionMode = null;
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    mAdapter.resetAnimationIndex();
                    // mAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    // deleting the messages from recycler view
    private void deleteMessages() {
        mAdapter.resetAnimationIndex();
        List<Integer> selectedItemPositions =
                mAdapter.getSelectedItems();
        for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
            mAdapter.removeData(selectedItemPositions.get(i));
        }
        mAdapter.notifyDataSetChanged();
    }

    private void getData() {
//san suriel ONLINEMODE owerride!!
        String id = "5999881783650"; //szentkirályi :)
        globalvevoKod = "0120401";
          if(adminokList.contains(globalvevoKod)){

        url = DATA_RAKTAR_KESZLET_URL_ONLINE + id + "&vkod=" + globalvevoKod;
          } else {
              url = DATA_RAKTAR_KESZLET_URL + id + "&vkod=" + globalvevoKod;
          }

        Toast toast= Toast.makeText(getApplicationContext(),"gatData() url:" +  url , Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM,0,20); toast.show();

        StringRequest stringRequest = new StringRequest(url, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //        loading.dismiss();
                showJSON(response);

//san suriel 09 rem
                //shopAmmount();
                //fillArrays();
            }
        },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Hibás lekérdezés!", Toast.LENGTH_LONG).show();
                        //Toast.makeText(MainActivity.this, error.getMessage().toString(), Toast.LENGTH_LONG).show();
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);

// san suriel
// StringRequest Timeout, empty cache
        /*
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                3000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        stringRequest.setShouldCache(false);
        */
        requestQueue.add(stringRequest);
        }

    //*** MAIN SHOW JSON ***
    private void showJSON(String response) {
        //Toast.makeText(MainActivity.this, "showJSON()", Toast.LENGTH_SHORT).show();

//san suriel
        /*
        String marka = "";
        String termek = "";
        String ar = "";
        String menny = "";
        String vevonev= "";
        Double netto = 0.00;
        Double afa = 0.00;
        Boolean akcios = false;
        */

        //Válasz adatok tárolása
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray result = jsonObject.getJSONArray(Config.JSON_ARRAY);
            JSONObject termekData = result.getJSONObject(0);
            marka = termekData.getString(Config.KEY_MARKA);
            termek = termekData.getString(Config.KEY_TERMEK);
            menny = termekData.getString(Config.KEY_KESZLET);
            nettoString = termekData.getString(Config.KEY_NETTO);
            //realnettoString = String.valueOf(nettoString);
            afaString = termekData.getString(Config.KEY_AFA);
            akcios = termekData.getBoolean(Config.KEY_AKCIO);
            vevonev = termekData.getString(Config.KEY_VEVONEV);

            Toast toast= Toast.makeText(getApplicationContext(),"showJSON():"+ marka + ", " + termek + ", " + menny + ", " + nettoString + ", " + afaString + ", " + akciosstring, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM,0,20); toast.show();

        } catch (JSONException e) {
            e.printStackTrace();
        }

//san suriel
//a fő fügvényből nem akar lefutni a fillTables(), de miert?

//átmehetne alfügvénybe valahogy... (fillArrays)
        //finals for intent extras
        final String ar1 = ar;
        final String barcode1 = barcode3;
        final String marka1 = marka;
        final String termek1 = termek;


        fillArrays();

    }
//-----------------------------------------------------------------

    //san suriel - új fügvény a bevásárlólista tömbjeinek feltöltésére...
    private void fillArrays() {
        Toast toast = Toast.makeText(getApplicationContext(), "fillArrays()" + m_Text2, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
        if (barcodeList.contains(barcode3)) {
            toast = Toast.makeText(getApplicationContext(), "MÁR SZEREPEL A LISTÁN!", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            //error already scanned vibrate
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            long[] pattern = {0, 200, 100, 200};
            assert v != null;
            v.vibrate(pattern, -1);
        } else if (marka == "null") {
            toast = Toast.makeText(getApplicationContext(), "ISMERETLEN VONALKÓD!", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM, 0, 300);
            toast.show();
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            long[] pattern = {0, 50, 50, 50, 50, 50};
            assert v != null;
            v.vibrate(pattern, -1);
        } else {
            toast = Toast.makeText(getApplicationContext(), "marka:" + marka + " termek:" + termek + " menny:" + menny + " netto:" + netto + " afa:" + afa, Toast.LENGTH_LONG);


            barcodeList.add(barcode3);
            mennyisegList.add(menny);
            termekList.add(termek);
            markaList.add(marka);
            shopammountList.add(m_Text2);
//san suriel
            nettoStringList.add(nettoString);
            afaStringList.add(afaString);
            vevonevList.add(vevonev);
            akciosList.add(akcios);


            Double brutto = 0.00;
            brutto = netto * (afa + 100) / 100;
            bruttoRound = String.format("%.2f", brutto);
            nettoRound = String.format("%.2f", netto);

            Double nettoTotal = (netto * brutto);
            nettototalRound = String.format("%.2f", nettoTotal);
            mergedList.add(marka + " / " + termek + "\n" + "Raktáron: " + menny + " db / Rendlés: " + m_Text2 + " db" + "\n" + "Nettó: " + nettoRound + "Ft / Bruttó: " + bruttoRound + "Ft / Nettó Össz.:" + nettototalRound + "Ft");







        }

        if (akcios) {
            akciosstring = "igen";
        } else {
            akciosstring = "nem";
        }

        barcodeArray = barcodeList.toArray(new String[barcodeList.size()]);
        mennyisegArray = mennyisegList.toArray(new String[mennyisegList.size()]);
        termekArray = termekList.toArray(new String[termekList.size()]);
        markaArray = markaList.toArray(new String[markaList.size()]);
        shopammountArray = shopammountList.toArray(new String[shopammountList.size()]);
        nettoStringArray = nettoStringList.toArray(new String[nettoStringList.size()]);
        afaStringArray = afaStringList.toArray(new String[afaStringList.size()]);
        vevonevArray = vevonevList.toArray(new String[vevonevList.size()]);
        //akciosArray = akciosList.toArray(new Boolean[akciosList.size()]);

        mergedArray = mergedList.toArray(new String[mergedList.size()]);
        //ListView itemsListView = (ListView) findViewById(itemListView);




    //    toast = Toast.makeText(getApplicationContext(), termekArray[0], Toast.LENGTH_LONG);
    //    toast.setGravity(Gravity.CENTER, 0, 0);
    //    toast.show();

    }//fillArrays!


    //san suriel - új fügvény, bekéri a vásárolt mennyiséget...
    //innen hívom meg a fillArays t
    private void shopAmmount() {
        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setTitle("Írja be a vásárolt mennyiséget:");
        // Set up the input
        final EditText input2 = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input2.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder2.setView(input2);
        // Set up the buttons
        builder2.setPositiveButton("Rendben", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text2 = input2.getText().toString();
                fillArrays();
                //Toast toast= Toast.makeText(getApplicationContext(),"ok pressed" + m_Text2, Toast.LENGTH_SHORT);
                //toast.setGravity(Gravity.CENTER,0,0); toast.show();

            }
        });
        builder2.setNegativeButton("Mégsem rendelek", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder2.show();
        //Toast.makeText(MainActivity.this, m_Text2, Toast.LENGTH_SHORT).show();
    }







}
