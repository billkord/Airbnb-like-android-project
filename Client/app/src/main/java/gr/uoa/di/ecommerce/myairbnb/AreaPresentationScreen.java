package gr.uoa.di.ecommerce.myairbnb;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.icu.text.SimpleDateFormat;
import android.location.Address;
import android.location.Geocoder;
import android.net.SSLCertificateSocketFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;


public class AreaPresentationScreen extends AppCompatActivity {

    private Context APScreen;
    private String jsonin;
    private ViewPager roomImages;
    private final int HOSTPAGE_VISITED = 1;

    private Calendar myCalendar;
    private String dateFrom;
    private String dateTo;

    private TextView roomName;
    private TextView hostName;
    private TextView country;
    private TextView city;
    private TextView address;
    private TextView maxVisitors;
    private TextView minPrice;
    private TextView roomType;
    private TextView rules;
    private TextView description;
    private TextView area;
    private TextView peopleRated;
    private TextView dateBegging;
    private TextView dateEnd;
    private EditText rateText;
    private RatingBar ratingBar;
    private Button rateBtn;

    private String hostNameStr;
    private String roomNameStr;
    private String addressStr;
    private String cityStr;
    private String countryStr;
    private Intent roomIntent;
    private ProgressDialog progress;

    private SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.areapresentationscreen);
        APScreen = this;
        myCalendar = Calendar.getInstance();

        roomIntent = getIntent();
        roomImages = (ViewPager) findViewById(R.id.ViewPager_APS);

        /*Loading*/
        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Please wait");
        progress.setCancelable(false);
        progress.show();

        try{
            JSONObject jsonToSend = new JSONObject();
            jsonToSend.put("jwt", USER_DATA.getJWT());
            jsonToSend.put("roomName", roomIntent.getStringExtra("roomName"));
            jsonToSend.put("type", "ONE");
            ThreadedConnection fetchRoomInfo = new ThreadedConnection("returnRooms", "POST", jsonToSend.toString());
            fetchRoomInfo.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        /*Find The Views*/
        roomName = (TextView) findViewById(R.id.RoomName_APS);
        hostName = (TextView) findViewById(R.id.HostName_APS);
        country = (TextView) findViewById(R.id.Country_APS);
        city = (TextView) findViewById(R.id.City_APS);
        address = (TextView) findViewById(R.id.Address_APS);
        maxVisitors = (TextView) findViewById(R.id.MaxVisitors_APS);
        minPrice = (TextView) findViewById(R.id.MinPrice_APS);
        roomType = (TextView) findViewById(R.id.RoomType_APS);
        rules = (TextView) findViewById(R.id.Rules_APS);
        description = (TextView) findViewById(R.id.Description_APS);
        area = (TextView) findViewById(R.id.Area_APS);
        ratingBar = (RatingBar) findViewById(R.id.RatingBar_APS);
        peopleRated = (TextView) findViewById(R.id.PeopleRated);
        rateBtn = (Button) findViewById(R.id.RateBtn_APS);
        rateText = (EditText) findViewById(R.id.RateText_APS);
        dateBegging = (TextView) findViewById(R.id.DateFrom_APS);
        dateEnd = (TextView) findViewById(R.id.DateTo_APS);
        Button reserveBtn = (Button) findViewById(R.id.ReserveBtn_APS);
        Button contactBtn = (Button) findViewById(R.id.ContactBtn_APS);

        reserveBtn.setOnClickListener(handleReserveClick());
        contactBtn.setOnClickListener(handleContactClick());
        rateBtn.setOnClickListener(handleRateClick());
    }
/**************************************************************************************************/
    public void timerDelayRemoveDialog(long time, final ProgressDialog d){
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if(d.isShowing()){
                    d.dismiss();
                    new AlertDialog.Builder(AreaPresentationScreen.this).setTitle(getResources().getString(R.string.AlertDialogTitle_SIS))
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
    private void loadMap(GoogleMap googleMap){
        if (googleMap != null) {
            // Map is ready
            googleMap.getUiSettings().setScrollGesturesEnabled(false);
            googleMap.getUiSettings().setZoomControlsEnabled(false);
            googleMap.getUiSettings().setZoomGesturesEnabled(false);
            LatLng position = getLocationFromAddress(mapFragment.getContext(), addressStr +", "+ cityStr +", "+ countryStr);
            if(position != null){
                googleMap.addMarker(new MarkerOptions().position(position));
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(position));
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, 14);
                googleMap.animateCamera(cameraUpdate);
            } else{
                Toast.makeText(APScreen, getResources().getString(R.string.Map_error), Toast.LENGTH_LONG).show();
            }
        }
    }
/**************************************************************************************************/
    private LatLng getLocationFromAddress(Context context, String strAddress) {
        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;
        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();
            p1 = new LatLng(location.getLatitude(), location.getLongitude());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return p1;
    }
/**************************************************************************************************/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == HOSTPAGE_VISITED){
            hostName.setTextColor(Color.parseColor("#551A8B"));
        }
    }
/**************************************************************************************************/
    public void hostInfoClick(View v){
        Intent toHostPresentationScreen = new Intent(APScreen, HostPresentationScreen.class);
        toHostPresentationScreen.putExtra("hostName", hostNameStr);
        toHostPresentationScreen.putExtra("roomName", roomNameStr);
        startActivityForResult(toHostPresentationScreen, HOSTPAGE_VISITED);
    }
/**************************************************************************************************/
    private View.OnClickListener handleReserveClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(roomIntent.getStringExtra("type").equals("search")){
                    dateFrom = roomIntent.getStringExtra("dateFrom");
                    dateTo = roomIntent.getStringExtra("dateTo");
                    JSONObject jsonToSend = new JSONObject();
                    try {
                        jsonToSend.put("username", USER_DATA.getUsername());
                        jsonToSend.put("hostName", hostNameStr);
                        jsonToSend.put("roomName", roomNameStr);
                        jsonToSend.put("dateFrom", dateFrom);
                        jsonToSend.put("dateTo", dateTo);
                        ThreadedConnection makeResevation = new ThreadedConnection("addReservation", "POST", jsonToSend.toString());
                        makeResevation.execute();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if(roomIntent.getStringExtra("type").equals("famous")){
                    makeDates();
                }
            }
        };
    }
/**************************************************************************************************/
    private View.OnClickListener handleContactClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toSendMessage = new Intent(APScreen, SendMessageScreen.class);
                toSendMessage.putExtra("hostName", hostNameStr);
                startActivity(toSendMessage);
            }
        };
    }
/**************************************************************************************************/
    private View.OnClickListener handleRateClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject jsonToSend = new JSONObject();
                try {
                    jsonToSend.put("roomName", roomNameStr);
                    jsonToSend.put("hostName", hostNameStr);
                    jsonToSend.put("rate", ratingBar.getRating());
                    jsonToSend.put("from", USER_DATA.getUsername());
                    jsonToSend.put("rateText", rateText.getText().toString());
                    ThreadedConnection rateRoom = new ThreadedConnection("rateRoom", "POST", jsonToSend.toString());
                    rateRoom.execute();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
    }
/**************************************************************************************************/
    private void makeDates(){
        String myFormat = "dd/MM/yyyy";
        final int ONE_DAY_MILLIS = 86400000;
        final SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ENGLISH);
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                dateFrom = sdf.format(myCalendar.getTime());
                DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        dateTo = sdf.format(myCalendar.getTime());

                        /*Loading*/
                        progress = new ProgressDialog(APScreen);
                        progress.setTitle("Loading");
                        progress.setMessage("Please wait");
                        progress.setCancelable(false);
                        progress.show();
                        timerDelayRemoveDialog(7000, progress);

                        JSONObject jsonToSend = new JSONObject();
                        try {
                            jsonToSend.put("roomName", roomNameStr);
                            jsonToSend.put("hostName", hostNameStr);
                            jsonToSend.put("dateFrom", dateFrom);
                            jsonToSend.put("dateTo", dateTo);
                            ThreadedConnection checkAvailability = new ThreadedConnection("checkAvailability", "POST", jsonToSend.toString());
                            checkAvailability.execute();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                DatePickerDialog datePickerDialog = new DatePickerDialog(AreaPresentationScreen.this, date,
                        myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));
                try {
                    datePickerDialog.getDatePicker().setMinDate(sdf.parse(dateFrom).getTime() + ONE_DAY_MILLIS);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                datePickerDialog.show();
            }
        };
        DatePickerDialog datePickerDialog = new DatePickerDialog(AreaPresentationScreen.this, date,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
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
            final JSONObject jsonReceived;
            try {
                jsonReceived = new JSONObject(jsonin);
                switch (request) {
                    case "returnRooms":
                        hostName.setPaintFlags(hostName.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                        hostName.setText(getResources().getString(R.string.HostName_APS) + " " + jsonReceived.getString("hostName"));
                        roomName.setText(getResources().getString(R.string.RoomName_APS) + " " + jsonReceived.getString("roomName"));
                        country.setText(getResources().getString(R.string.Country_APS) + " " + jsonReceived.getString("country"));
                        city.setText(getResources().getString(R.string.City_APS) + " " + jsonReceived.getString("city"));
                        address.setText(getResources().getString(R.string.Address_APS) + " " + jsonReceived.getString("address"));
                        dateBegging.setText(getResources().getString(R.string.DateFrom_APS) + " " + jsonReceived.get("dateFrom"));
                        dateEnd.setText(getResources().getString(R.string.DateTo_APS) + " " + jsonReceived.get("dateTo"));
                        maxVisitors.setText(getResources().getString(R.string.MaxVisitors_APS) + " " + jsonReceived.getString("maxVisitors"));
                        minPrice.setText(getResources().getString(R.string.MinPrice_APS) + " " + jsonReceived.getString("minPrice"));
                        roomType.setText(getResources().getString(R.string.RoomType_APS) + " " + jsonReceived.getString("roomType"));
                        rules.setText(getResources().getString(R.string.Rules_APS) + " " + jsonReceived.getString("rules"));
                        description.setText(getResources().getString(R.string.Description_APS) + " " + jsonReceived.getString("description"));
                        area.setText(getResources().getString(R.string.Area_APS) + " " + jsonReceived.getString("area"));
                        ratingBar.setRating(Float.parseFloat(jsonReceived.getString("rate")));
                        peopleRated.setText(getResources().getString(R.string.PeopleRated) + " " + jsonReceived.getString("peopleRated"));
                        if(jsonReceived.getString("visited").equals("1")){
                            ratingBar.setClickable(true);
                            ratingBar.setIsIndicator(false);
                            rateBtn.setVisibility(View.VISIBLE);
                            rateText.setVisibility(View.VISIBLE);
                        }
                        hostNameStr = jsonReceived.getString("hostName");
                        roomNameStr = jsonReceived.getString("roomName");
                        addressStr = jsonReceived.getString("address");
                        cityStr = jsonReceived.getString("city");
                        countryStr = jsonReceived.getString("country");
                        ArrayList<String> images = new ArrayList<>();
                        for (int i = 0; i < jsonReceived.getJSONArray("images").length(); i++) {
                            images.add(jsonReceived.getJSONArray("images").getString(i));
                        }
                        AreaPresentation_PageViewAdapter adapter = new AreaPresentation_PageViewAdapter(APScreen, images);
                        roomImages.setAdapter(adapter);

                        /*Map initialization*/
                        mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.Map_APS));
                        if (mapFragment != null) {
                            mapFragment.getMapAsync(new OnMapReadyCallback() {
                                @Override
                                public void onMapReady(GoogleMap map) {
                                    loadMap(map);
                                }
                            });
                        }
                        progress.dismiss();
                        break;
                    case "checkAvailability":
                        if (jsonReceived.getString("res").equals("success")) {
                            new AlertDialog.Builder(APScreen).setTitle(getResources().getString(R.string.AlertDialogTitle)).setMessage(getResources().getString(R.string.AlertDialogMessage))
                                    .setPositiveButton(getResources().getString(R.string.AlertDialog_yes_button_MS), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            JSONObject jsonToSend = new JSONObject();
                                            try {
                                                jsonToSend.put("username", USER_DATA.getUsername());
                                                jsonToSend.put("hostName", hostNameStr);
                                                jsonToSend.put("roomName", roomNameStr);
                                                jsonToSend.put("dateFrom", dateFrom);
                                                jsonToSend.put("dateTo", dateTo);
                                                ThreadedConnection makeResevation = new ThreadedConnection("addReservation", "POST", jsonToSend.toString());
                                                makeResevation.execute();
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }).setNegativeButton(getResources().getString(R.string.AlertDialog_no_button_MS), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    closeContextMenu();
                                }
                            }).show();
                        } else {
                            new AlertDialog.Builder(APScreen).setTitle(getResources().getString(R.string.AlertDialogTitle))
                                    .setMessage(jsonReceived.getString("msg") + "\n" + getResources().getString(R.string.From) + "  " + dateFrom + "\n" + getResources().getString(R.string.To) + "       " + dateTo)
                                    .setPositiveButton(getResources().getString(R.string.Ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            closeContextMenu();
                                        }
                                    }).show();
                        }
                        progress.dismiss();
                        break;
                    case "addReservation":
                        Toast.makeText(APScreen, jsonReceived.getString("msg"), Toast.LENGTH_LONG).show();
                        int REFRESH_FAMOUS = 2;
                        setResult(REFRESH_FAMOUS);
                        finish();
                        break;
                    case "rateRoom":
                        ratingBar.setClickable(false);
                        ratingBar.setIsIndicator(true);
                        ratingBar.setRating(Float.parseFloat(jsonReceived.getString("rate")));
                        peopleRated.setText(getResources().getString(R.string.PeopleRated) + " " + jsonReceived.getString("peopleRated"));
                        rateBtn.setVisibility(View.GONE);
                        rateText.setVisibility(View.GONE);
                        break;
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void trustEveryone() {
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
