package gr.uoa.di.ecommerce.myairbnb;


import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.SSLCertificateSocketFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.Html;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
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
import java.util.ArrayList;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

public class MainScreenHost extends AppCompatActivity {
    private String jsonin;
    private Context MHScreen;
    private final int REFRESH_ROOMS = 1;
    private ListView myRoomsView;
    private ProgressDialog progress;
    private String[] text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainscreenhost);
        MHScreen = this;
        int REFRESH_FAMOUS = 2;
        setResult(REFRESH_FAMOUS);

        /*Find the views*/
        myRoomsView = (ListView) findViewById(R.id.RoomsListView_MSH);
        FloatingActionButton plusButton = (FloatingActionButton) findViewById(R.id.PlusBtn_MHS) ;
        ImageButton messagesButton = (ImageButton) findViewById(R.id.Messages_MSH);
        ImageButton profileButton = (ImageButton) findViewById(R.id.Profile_MSH);
        ImageButton userButton = (ImageButton) findViewById(R.id.User_MSH);

        profileButton.setOnClickListener(handleProfileClick(this));
        messagesButton.setOnClickListener(handleMessagesClick());
        userButton.setOnClickListener(handleHostClick(this));
        plusButton.setOnClickListener(handlePlusClick(this));
        registerForContextMenu(myRoomsView);
        myRoomsView.setOnItemClickListener(handleItemClick());
    }
/**************************************************************************************************/
    @Override
    protected void onResume() {
        super.onResume();
        /*Loading*/
        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Please wait");
        progress.setCancelable(false);
        progress.show();
        timerDelayRemoveDialog(7000, progress);
        try{
            JSONObject jsonToSend = new JSONObject();
            jsonToSend.put("hostName", USER_DATA.getUsername());
            jsonToSend.put("type", "HOST_ROOMS");
            ThreadedConnection fetchHostRooms = new ThreadedConnection("returnRooms", "POST", jsonToSend.toString());
            fetchHostRooms.execute();
        } catch (JSONException e){
            e.printStackTrace();
        }
    }
/**************************************************************************************************/
    public void timerDelayRemoveDialog(long time, final ProgressDialog d){
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if(d.isShowing()){
                    d.dismiss();
                    new AlertDialog.Builder(MainScreenHost.this).setTitle(getResources().getString(R.string.AlertDialogTitle_SIS))
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
    private AdapterView.OnItemClickListener handleItemClick(){
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent reviewRoom = new Intent(MHScreen, AddRoomScreen.class);
                reviewRoom.putExtra("roomName", myRoomsView.getItemAtPosition(position).toString());
                startActivityForResult(reviewRoom, REFRESH_ROOMS);
            }
        };
    }
/**************************************************************************************************/
    private View.OnClickListener handleMessagesClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MHScreen, MessagesScreen.class));
            }
        };
    }
/**************************************************************************************************/
    private View.OnClickListener handleProfileClick(final Context mainScreenHost) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mainScreenHost, UserProfileScreen.class));
            }
        };
    }
/**************************************************************************************************/
    private View.OnClickListener handleHostClick(final Context mainScreenHost) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mainScreenHost, MainScreen.class));
                finish();
            }
        };
    }
/**************************************************************************************************/
    private View.OnClickListener handlePlusClick(final Context mainScreenHost) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(mainScreenHost, AddRoomScreen.class), REFRESH_ROOMS);
            }
        };
    }
/**************************************************************************************************/
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.RoomsListView_MSH) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            menu.setHeaderTitle(myRoomsView.getItemAtPosition(info.position).toString());
            String[] menuItems = {getResources().getString(R.string.longClickDates), getResources().getString(R.string.longClickEdit), getResources().getString(R.string.longClickDelete)};
            for (int i = 0; i<menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }
/**************************************************************************************************/
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        String[] menuItems = {getResources().getString(R.string.longClickDates), getResources().getString(R.string.longClickEdit), getResources().getString(R.string.longClickDelete)};
        if(menuItems[menuItemIndex].equals(getResources().getString(R.string.longClickDates))){
            closeContextMenu();
            JSONObject jsonToSend = new JSONObject();
            try{
                jsonToSend.put("hostName", USER_DATA.getUsername());
                jsonToSend.put("roomName", myRoomsView.getItemAtPosition(info.position).toString());
                ThreadedConnection reservations = new ThreadedConnection("fetchReservations", "POST", jsonToSend.toString());
                reservations.execute();
            } catch (JSONException e){
                e.printStackTrace();
            }
        }else if(menuItems[menuItemIndex].equals(getResources().getString(R.string.longClickEdit))){
            Intent reviewRoom = new Intent(MHScreen, AddRoomScreen.class);
            reviewRoom.putExtra("roomName", myRoomsView.getItemAtPosition(info.position).toString());
            startActivityForResult(reviewRoom, REFRESH_ROOMS);
        } else if(menuItems[menuItemIndex].equals(getResources().getString(R.string.longClickDelete))){
            new AlertDialog.Builder(this).setTitle(myRoomsView.getItemAtPosition(info.position).toString()).setMessage(getResources().getString(R.string.Room_deletion_text_MS))
                    .setPositiveButton(getResources().getString(R.string.AlertDialog_yes_button_MS), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            JSONObject jsonToSend = new JSONObject();
                            try{
                                jsonToSend.put("hostName", USER_DATA.getUsername());
                                jsonToSend.put("roomName", myRoomsView.getItemAtPosition(info.position).toString());
                                ThreadedConnection deleteRoom = new ThreadedConnection("deleteRoom", "POST", jsonToSend.toString());
                                deleteRoom.execute();
                            } catch (JSONException e){
                                e.printStackTrace();
                            }
                        }
                    }).setNegativeButton(getResources().getString(R.string.AlertDialog_no_button_MS), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    closeContextMenu();
                }
            }).show();
        }
        return true;
    }
/**************************************************************************************************/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == REFRESH_ROOMS){
            JSONObject jsonToSend = new JSONObject();
            try {
                jsonToSend.put("hostName", USER_DATA.getUsername());
                jsonToSend.put("type", "HOST_ROOMS");
                ThreadedConnection fetchHostRooms = new ThreadedConnection("returnRooms", "POST", jsonToSend.toString());
                fetchHostRooms.execute();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onPostExecute(Object o) {
            final JSONObject jsonReceived;
            try {
                jsonReceived = new JSONObject(jsonin);
                switch (request) {
                    case "returnRooms":
                        if (jsonReceived.getString("res").equals("success")) {
                            ArrayList<String> countries = new ArrayList<>();
                            ArrayList<String> roomImages = new ArrayList<>();
                            ArrayList<String> roomDescriptions = new ArrayList<>();
                            ArrayList<String> roomRates = new ArrayList<>();
                            JSONObject images;
                            for (int i = 0; i < jsonReceived.getJSONArray("names").length(); i++) {
                                countries.add(jsonReceived.getJSONArray("names").getString(i));
                                images = jsonReceived.getJSONObject("images");
                                roomImages.add(images.getString("" + i));
                                roomDescriptions.add(jsonReceived.getJSONArray("descriptions").getString(i));
                                roomRates.add(jsonReceived.getJSONArray("rates").getString(i));
                            }
                            RoomAdapter myRoomAdapter = new RoomAdapter(MHScreen, countries, roomImages, roomDescriptions, roomRates);
                            myRoomsView.setAdapter(myRoomAdapter);
                        } else if (jsonReceived.getString("res").equals("error")) {
                            Toast.makeText(MHScreen, jsonReceived.getString("msg"), Toast.LENGTH_LONG).show();
                        }
                        progress.dismiss();
                        break;
                    case "deleteRoom":
                        JSONObject jsonToSend = new JSONObject();
                        try {
                            jsonToSend.put("hostName", USER_DATA.getUsername());
                            jsonToSend.put("type", "HOST_ROOMS");
                            ThreadedConnection fetchHostRooms = new ThreadedConnection("returnRooms", "POST", jsonToSend.toString());
                            fetchHostRooms.execute();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(MHScreen, jsonReceived.getString("msg"), Toast.LENGTH_LONG).show();
                        break;
                    case "fetchReservations":
                        if(jsonReceived.getString("res").equals("success")){
                            int length = jsonReceived.getJSONArray("username").length();
                            text = new String[length];
                            for (int i = 0; i < length; i++) {
                                text[i] = getResources().getString(R.string.Username_textView_text) +" "+ jsonReceived.getJSONArray("username").getString(i) +"\n"+
                                        getResources().getString(R.string.DateFrom_APS) +" "+ jsonReceived.getJSONArray("dateFrom").getString(i) +"\n"+
                                        getResources().getString(R.string.DateTo_APS) +" "+ jsonReceived.getJSONArray("dateTo").getString(i) + "\n";
                                if ((Build.VERSION.SDK_INT) >= 24){
                                    text[i] += Html.fromHtml("<hr>", Html.FROM_HTML_MODE_COMPACT);
                                } else{
                                    text[i] += Html.fromHtml("<hr>");
                                }
                            }
                            new AlertDialog.Builder(MainScreenHost.this).setTitle(getResources().getString(R.string.processMenuReservation_title) +" "+ jsonReceived.getString("roomName"))
                                    .setItems(text, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, final int id) {
                                            new AlertDialog.Builder(MainScreenHost.this).setPositiveButton(getResources().getString(R.string.Contact_MSH), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Intent toSendMessage = new Intent(MHScreen, SendMessageScreen.class);
                                                    try {
                                                        String usernameStr = text[id].split("\n")[0].split(": ")[1];
                                                        toSendMessage.putExtra("hostName", usernameStr);
                                                        startActivity(toSendMessage);
                                                    }catch (Exception e){
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }).setNegativeButton(getResources().getString(R.string.cancel_MSH), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    JSONObject jsonToSend = new JSONObject();
                                                    try {
                                                        String usernameStr = text[id].split("\n")[0].split(": ")[1];
                                                        String dateFrom = text[id].split("\n")[1].split(": ")[1];
                                                        String dateTo = text[id].split("\n")[2].split(": ")[1];
                                                        jsonToSend.put("roomName", jsonReceived.getString("roomName"));
                                                        jsonToSend.put("hostName", USER_DATA.getUsername());
                                                        jsonToSend.put("username", usernameStr);
                                                        jsonToSend.put("dateFrom", dateFrom);
                                                        jsonToSend.put("dateTo", dateTo);
                                                        ThreadedConnection calcelReservation = new ThreadedConnection("deleteReservation", "POST", jsonToSend.toString());
                                                        calcelReservation.execute();
                                                        String message = getResources().getString(R.string.cancellation_txt0_MSH) +" "+ usernameStr +"\n"+
                                                                getResources().getString(R.string.cancellation_txt1_MSH) +" "+ jsonReceived.getString("roomName") +"\n\n"+
                                                                getResources().getString(R.string.cancellation_txt2_MSH) +" "+ dateFrom +"\n"+
                                                                getResources().getString(R.string.cancellation_txt3_MSH) +" "+ dateTo +"\n\n"+
                                                                getResources().getString(R.string.cancellation_txt4_MSH) +"\n"+ USER_DATA.getUsername();
                                                        jsonToSend.put("from", USER_DATA.getUsername());
                                                        jsonToSend.put("to", usernameStr);
                                                        jsonToSend.put("message", message);
                                                        ThreadedConnection sendMessage = new ThreadedConnection("msgForward", "POST", jsonToSend.toString());
                                                        sendMessage.execute();
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }).show();
                                        }
                                    }).show();
                        } else{
                            Toast.makeText(MHScreen, jsonReceived.getString("msg"), Toast.LENGTH_LONG).show();
                        }

                        break;
                    case "deleteReservation":
                        if(jsonReceived.getString("res").equals("success")){
                            Toast.makeText(MHScreen, jsonReceived.getString("msg") +" "+ getResources().getString(R.string.user_MSH) +" "+ jsonReceived.getString("to") +" "+ getResources().getString(R.string.userInformed_MSH), Toast.LENGTH_LONG).show();
                        } else{
                            Toast.makeText(MHScreen, jsonReceived.getString("msg"), Toast.LENGTH_LONG).show();
                        }
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
