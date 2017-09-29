package gr.uoa.di.ecommerce.myairbnb;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.icu.util.ULocale;
import android.net.SSLCertificateSocketFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

public class MainScreen extends AppCompatActivity {
    private final int CHECK_HOST = 12;
    private final int REFRESH_FAMOUS = 2;

    private Calendar myCalendar;

    private ImageButton hostButton;
    private Context MScreen;
    private String jsonin;
    private ListView famousRooms;
    private PopupWindow searchPopUp;
    private RelativeLayout positionOfPopUp;
    private EditText country_editText;
    private EditText city_editText;
    private EditText address_editTex;
    private EditText dateFrom_editText;
    private EditText dateTo_editText;
    private EditText people_editText;

    private Intent toAreaPresentation;
    private SharedPreferences mPrefs;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainscreen);
        MScreen = this;
        myCalendar = Calendar.getInstance();
        toAreaPresentation = new Intent(MScreen, AreaPresentationScreen.class);

        /*Loading*/
        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Please wait");
        progress.setCancelable(false);
        progress.show();
        timerDelayRemoveDialog(7000, progress);

        JSONObject jsonToSend = new JSONObject();
        try {
            jsonToSend.put("type", "FAMOUS");
            jsonToSend.put("jwt", USER_DATA.getJWT());
            ThreadedConnection fetchFamousRooms = new ThreadedConnection("returnRooms", "POST", jsonToSend.toString());
            fetchFamousRooms.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        /*Find the views*/
        famousRooms = (ListView) findViewById(R.id.RoomsListView_MS);
        hostButton = (ImageButton) findViewById(R.id.Host_MS);
        ImageButton profileButton = (ImageButton) findViewById(R.id.Profile_MS);
        ImageButton messagesButton = (ImageButton) findViewById(R.id.Messages_MS);
        ImageButton searchButton = (ImageButton) findViewById(R.id.SearchBar_MS);
        ImageButton signOutButton = (ImageButton) findViewById(R.id.SignOut_MS);
        positionOfPopUp = (RelativeLayout) findViewById(R.id.RelativeLayout_MS);

        profileButton.setOnClickListener(handleProfileClick());
        messagesButton.setOnClickListener(handleMessagesClick());
        searchButton.setOnClickListener(handleSearchClick());
        signOutButton.setOnClickListener(handleSignOutClick());
        hostButton.setOnClickListener(handleHostClick());
        famousRooms.setOnItemClickListener(handleRoomClick());
    }
/**************************************************************************************************/
    @Override
    protected void onResume() {
        super.onResume();
        if (USER_DATA.isHost()) {
            hostButton.setVisibility(View.VISIBLE);
        } else {
            hostButton.setVisibility(View.GONE);
        }
    }
/**************************************************************************************************/
    public void timerDelayRemoveDialog(long time, final ProgressDialog d){
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if(d.isShowing()){
                    d.dismiss();
                    new AlertDialog.Builder(MainScreen.this).setTitle(getResources().getString(R.string.AlertDialogTitle_SIS))
                            .setMessage(getResources().getString(R.string.Connectioon_not_found))
                            .setPositiveButton(getResources().getString(R.string.Ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    closeContextMenu();
                                }
                            }).show();
                }
            }
        }, time);
    }
/**************************************************************************************************/
    private View.OnClickListener handleSignOutClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Write in memory*/
                mPrefs = getSharedPreferences("LSprefs",0);
                SharedPreferences.Editor editor;
                editor = mPrefs.edit();
                editor.putString("jwt", "");
                editor.commit();
                startActivity(new Intent(MScreen, SignInScreen.class));
                finish();
            }
        };
}
/**************************************************************************************************/
    private View.OnClickListener handleSearchClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater=(LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                final View customView = inflater.inflate(R.layout.popup_layout, null);
                searchPopUp = new PopupWindow(customView, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                ImageButton closeSearchPopUp = (ImageButton) customView.findViewById(R.id.SearchBackButton_PopUp);
                closeSearchPopUp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        searchPopUp.dismiss();
                    }
                });
                searchPopUp.showAtLocation(positionOfPopUp, Gravity.CENTER, 0 ,0 );

                /*Keyboard to pop up*/
                searchPopUp.setFocusable(true);
                searchPopUp.update();

                country_editText = (EditText) customView.findViewById(R.id.Country_PopUp);
                city_editText = (EditText) customView.findViewById(R.id.City_PopUp);
                address_editTex = (EditText) customView.findViewById(R.id.Address_PopUp);
                dateFrom_editText = (EditText) customView.findViewById(R.id.DateFrom_PopUp);
                dateTo_editText = (EditText) customView.findViewById(R.id.DateTo_PopUp);
                people_editText = (EditText) customView.findViewById(R.id.People_PopUp);

                dateFrom_editText.setOnClickListener(handleDateClick(dateFrom_editText));
                dateTo_editText.setOnClickListener(handleDateClick(dateTo_editText));

                /*Change visibility when country is filled*/
                country_editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }
                    @Override
                    public void afterTextChanged(Editable s) {
                        if(s.toString().trim().length()==0){
                            city_editText.setVisibility(View.GONE);
                        } else {
                            city_editText.setVisibility(View.VISIBLE);
                        }
                    }
                });

                /*Change visibility when city is filled*/
                city_editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }
                    @Override
                    public void afterTextChanged(Editable s) {
                        if(s.toString().trim().length()==0){
                            address_editTex.setVisibility(View.GONE);
                        } else {
                            address_editTex.setVisibility(View.VISIBLE);
                        }
                    }
                });

                Button submitSearch = (Button) customView.findViewById(R.id.SearchConfirm_PopUp);
                submitSearch.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        JSONObject jsonToSend = new JSONObject();
                        try{
                            if(datePeople()){
                                /*Loading*/
                                progress = new ProgressDialog(MScreen);
                                progress.setTitle("Loading");
                                progress.setMessage("Please wait");
                                progress.setCancelable(false);
                                progress.show();
                                timerDelayRemoveDialog(7000, progress);
                                toAreaPresentation.putExtra("type", "search");
                                if(countryCityAddress()){
                                    jsonToSend.put("type", "countryCityAddress");
                                } else if(countryCity()){
                                    jsonToSend.put("type", "countryCity");
                                } else if(country()){
                                    jsonToSend.put("type", "country");
                                } else{
                                    jsonToSend.put("type", "simple");
                                }
                                jsonToSend.put("country", country_editText.getText().toString());
                                jsonToSend.put("city", city_editText.getText().toString());
                                jsonToSend.put("address", address_editTex.getText().toString());
                                jsonToSend.put("dateFrom", dateFrom_editText.getText().toString());
                                jsonToSend.put("dateTo", dateTo_editText.getText().toString());
                                jsonToSend.put("maxVisitors", Integer.parseInt(people_editText.getText().toString()));
                                jsonToSend.put("jwt", USER_DATA.getJWT());
                                ThreadedConnection searchConnection = new ThreadedConnection("search", "POST", String.valueOf(jsonToSend));
                                searchConnection.execute();
                                searchPopUp.dismiss();
                            } else {
                                findTheEmptySearchFields();
                                Toast.makeText(MainScreen.this, getResources().getString(R.string.ToastFillAllReq), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
    }
/**************************************************************************************************/
    private View.OnClickListener handleDateClick(final EditText editText) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String myFormat = "dd/MM/yyyy";
                final int ONE_DAY_MILLIS = 86400000;
                final SimpleDateFormat sdf = new SimpleDateFormat(myFormat, ULocale.ENGLISH);
                DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        editText.setText(sdf.format(myCalendar.getTime()));
                    }
                };
                DatePickerDialog datePickerDialog = new DatePickerDialog(MainScreen.this, date,
                        myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));
                try {
                    if(editText.getId() == R.id.DateFrom_PopUp){
                        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                        dateTo_editText.setText("");
                    } else if(editText.getId() == R.id.DateTo_PopUp){
                        datePickerDialog.getDatePicker().setMinDate(sdf.parse(dateFrom_editText.getText().toString()).getTime() + ONE_DAY_MILLIS);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                datePickerDialog.show();
            }
        };
    }
/**************************************************************************************************/
    private View.OnClickListener handleMessagesClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MScreen, MessagesScreen.class));
            }
        };
    }
/**************************************************************************************************/
    private View.OnClickListener handleProfileClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent MS_intent = new Intent(MScreen, UserProfileScreen.class);
                startActivityForResult(MS_intent,CHECK_HOST);
            }
        };
    }
/**************************************************************************************************/
    private View.OnClickListener handleHostClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MScreen, MainScreenHost.class), REFRESH_FAMOUS);
                finish();
            }
        };
    }
/**************************************************************************************************/
    private AdapterView.OnItemClickListener handleRoomClick(){
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                toAreaPresentation.putExtra("roomName", famousRooms.getItemAtPosition(position).toString());
                startActivityForResult(toAreaPresentation, REFRESH_FAMOUS);
            }
        };
    }
/**************************************************************************************************/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == CHECK_HOST) {
            if (!USER_DATA.isHost()){
                hostButton.setVisibility(View.GONE);
            } else{
                hostButton.setVisibility(View.VISIBLE);
            }
        } else if(resultCode == REFRESH_FAMOUS){
            JSONObject jsonToSend = new JSONObject();
            try {
                jsonToSend.put("type", "FAMOUS");
                jsonToSend.put("jwt", USER_DATA.getJWT());
                ThreadedConnection fetchFamousRooms = new ThreadedConnection("returnRooms", "POST", jsonToSend.toString());
                fetchFamousRooms.execute();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
/**************************************************************************************************/
    private boolean datePeople(){
        boolean filled = !dateFrom_editText.getText().toString().equals("");
        filled = filled && !dateTo_editText.getText().toString().equals("");
        filled = filled && !people_editText.getText().toString().equals("");
        return filled;
    }
/**************************************************************************************************/
    private boolean countryCityAddress(){
        boolean filled = !city_editText.getText().toString().equals("");
        filled = filled && !country_editText.getText().toString().equals("");
        filled = filled && !address_editTex.getText().toString().equals("");
        return filled;
    }
/**************************************************************************************************/
    private boolean countryCity(){
        boolean filled = !country_editText.getText().toString().equals("");
        filled = filled && ! address_editTex.getText().toString().equals("");
        return filled;
    }
/**************************************************************************************************/
    private boolean country(){
        return !country_editText.getText().toString().equals("");
    }
/**************************************************************************************************/
    private void findTheEmptySearchFields(){
        if(dateFrom_editText.getText().toString().equals(""))
            dateFrom_editText.setHintTextColor(Color.parseColor("#990000"));
        if(dateTo_editText.getText().toString().equals(""))
            dateTo_editText.setHintTextColor(Color.parseColor("#990000"));
        if(people_editText.getText().toString().equals(""))
            people_editText.setHintTextColor(Color.parseColor("#990000"));
    }
/*******************************************************************************************************************************************/
    private class ThreadedConnection extends AsyncTask<Object, Object, Object> {
        private final String IP = USER_DATA.getIP();

        private String request = "";
        private String requestMethod = "";
        private String jsonout = "";

        private ThreadedConnection(String request, String requestMethod, String jsonout) {
            this.request = request;
            this.requestMethod = requestMethod;
            this.jsonout = jsonout;
        }

        @Override
        protected Object doInBackground(Object[] params) {
            HttpsURLConnection httpsConn = null;
            Writer writer;
            InputStream inputStream;
            StringBuilder stringBuilder = new StringBuilder();
            try {
                /*Connection Settings*/
                trustEveryone();
                URL url = new URL(IP + request);
                httpsConn = (HttpsURLConnection) url.openConnection();
                httpsConn.setSSLSocketFactory(SSLCertificateSocketFactory.getInsecure(0, null));
                httpsConn.setConnectTimeout(7000);
                httpsConn.setDoInput(true);
                httpsConn.setDoOutput(true);
                httpsConn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                httpsConn.setRequestMethod(requestMethod);
                if (!requestMethod.equals("GET")) {
                    /*Output Data*/
                    writer = new BufferedWriter(new OutputStreamWriter(httpsConn.getOutputStream(), "UTF-8"));
                    writer.write(jsonout);
                    writer.close();
                }
                /*Get Response*/
                int HttpResult = httpsConn.getResponseCode();
                if (HttpResult == HttpsURLConnection.HTTP_OK) {
                    inputStream = httpsConn.getInputStream();
                    if (inputStream == null) {
                        return null;
                    }
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                    String line;
                    while ((line = buffer.readLine()) != null) {
                        stringBuilder.append(line).append('\n');
                    }
                    buffer.close();
                    jsonin = stringBuilder.toString();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (httpsConn != null)
                httpsConn.disconnect();
            return jsonin;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (request.equals("returnRooms")){
                JSONObject jsonReceived;
                try {
                    jsonReceived = new JSONObject(jsonin);
                    ArrayList<String> countries = new ArrayList<>();
                    ArrayList<String> roomImages = new ArrayList<>();
                    ArrayList<String> roomPrices = new ArrayList<>();
                    ArrayList<String> roomAddresses = new ArrayList<>();
                    ArrayList<String> roomRates = new ArrayList<>();
                    JSONObject images;
                    for (int i = 0; i < jsonReceived.getJSONArray("names").length(); i++) {
                        countries.add(jsonReceived.getJSONArray("names").getString(i));
                        images = jsonReceived.getJSONObject("images");
                        roomImages.add(images.getString(""+i));
                        roomPrices.add(jsonReceived.getJSONArray("prices").getString(i));
                        roomAddresses.add(jsonReceived.getJSONArray("addresses").getString(i));
                        roomRates.add(jsonReceived.getJSONArray("rates").getString(i)); //PYTHON GET "rates" FROM DB AS INT AND GIVES THEM TO ANDROID AS STRINGS
                    }
                    RoomFamousAdapter famousRoomAdapter = new RoomFamousAdapter(MScreen, countries, roomImages, roomPrices, roomAddresses, roomRates);
                    famousRooms.setAdapter(famousRoomAdapter);
                    toAreaPresentation.putExtra("type", "famous");
                    progress.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if(request.equals("search")) {
                famousRooms.setAdapter(null);
                JSONObject jsonReceived;
                try {
                    jsonReceived = new JSONObject(jsonin);
                    if(jsonReceived.getString("res").equals("error")){
                        Toast.makeText(MScreen, jsonReceived.getString("msg"), Toast.LENGTH_LONG).show();
                        JSONObject jsonToSend = new JSONObject();
                        try {
                            jsonToSend.put("type", "FAMOUS");
                            jsonToSend.put("jwt", USER_DATA.getJWT());
                            ThreadedConnection fetchFamousRooms = new ThreadedConnection("returnRooms", "POST", jsonToSend.toString());
                            fetchFamousRooms.execute();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        toAreaPresentation.putExtra("dateFrom", jsonReceived.getString("dateFrom"));
                        toAreaPresentation.putExtra("dateTo", jsonReceived.getString("dateTo"));
                        int visitors = Integer.parseInt(jsonReceived.getString("visitors"));
                        ArrayList<String> countries = new ArrayList<>();
                        ArrayList<String> roomImages = new ArrayList<>();
                        ArrayList<String> roomPrices = new ArrayList<>();
                        ArrayList<String> roomAddresses = new ArrayList<>();
                        ArrayList<String> roomRates = new ArrayList<>();
                        JSONObject images;
                        for (int i = 0; i < jsonReceived.getJSONArray("names").length(); i++) {
                            countries.add(jsonReceived.getJSONArray("names").getString(i));
                            images = jsonReceived.getJSONObject("images");
                            roomImages.add(images.getString(""+i));
                            roomPrices.add(""+(Integer.parseInt(jsonReceived.getJSONArray("prices").getString(i)))*visitors);
                            roomAddresses.add(jsonReceived.getJSONArray("addresses").getString(i));
                            roomRates.add(jsonReceived.getJSONArray("rates").getString(i)); //PYTHON GET "rates" FROM DB AS INT AND GIVES THEM TO ANDROID AS STRINGS
                        }
                        RoomFamousAdapter searchRoomAdapter = new RoomFamousAdapter(MScreen, countries, roomImages, roomPrices, roomAddresses, roomRates);
                        famousRooms.setAdapter(searchRoomAdapter);
                        Toast.makeText(MScreen, jsonReceived.getString("msg"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progress.dismiss();
            }
        }

        private void trustEveryone(){
            try {
                HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }});
                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, new X509TrustManager[]{new X509TrustManager(){
                    public void checkClientTrusted(X509Certificate[] chain,
                                                   String authType) throws CertificateException {}
                    public void checkServerTrusted(X509Certificate[] chain,
                                                   String authType) throws CertificateException {}
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }}}, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(
                        context.getSocketFactory());
            } catch (Exception e) { // should never happen
                e.printStackTrace();
            }
        }
    }
}