package gr.uoa.di.ecommerce.myairbnb;

import android.support.v7.app.AppCompatActivity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.SSLCertificateSocketFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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

public class MessagesScreen extends AppCompatActivity{

    private Context MSGScreen;
    private String jsonin;
    private ListView messages_LV;
    private Handler mHandler;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messagesscreen);
        MSGScreen = this;
        messages_LV = (ListView) findViewById(R.id.MessagesListView_MSG);

        /*Loading*/
        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Please wait");
        progress.setCancelable(false);
        progress.show();
        timerDelayRemoveDialog(7000, progress);

        JSONObject jsonToSend = new JSONObject();
        try{
            jsonToSend.put("username", USER_DATA.getUsername());
            ThreadedConnection fetchMessages = new ThreadedConnection("fetchMessages", "POST", jsonToSend.toString());
            fetchMessages.execute();
        } catch (JSONException e){
            e.printStackTrace();
        }

        messages_LV.setOnItemClickListener(handleItemClick());
        registerForContextMenu(messages_LV);

        mHandler = new Handler();
        mStatusChecker.run();
    }
/**************************************************************************************************/
    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mStatusChecker);
    }
/**************************************************************************************************/
    public void timerDelayRemoveDialog(long time, final ProgressDialog d){
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if(d.isShowing()){
                    d.dismiss();
                    new AlertDialog.Builder(MessagesScreen.this).setTitle(getResources().getString(R.string.AlertDialogTitle_SIS))
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
    private Runnable mStatusChecker = new Runnable() { //https://stackoverflow.com/questions/6242268/repeat-a-task-with-a-time-delay
        @Override
        public void run() {
            try {

            } finally { // 100% guarantee that this always happens, even if your update method throws an exception
                JSONObject jsonToSend = new JSONObject();
                try{
                    jsonToSend.put("username", USER_DATA.getUsername());
                    ThreadedConnection fetchMessages = new ThreadedConnection("fetchMessages", "POST", jsonToSend.toString());
                    fetchMessages.execute();
                    mHandler.postDelayed(mStatusChecker, 5000);
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }
    };
/**************************************************************************************************/
    private AdapterView.OnItemClickListener handleItemClick(){
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent toMessageView = new Intent(MSGScreen, MessageViewScreen.class);
                toMessageView.putExtra("messageJson", messages_LV.getItemAtPosition(position).toString());
                startActivity(toMessageView);
            }
        };
    }
/**************************************************************************************************/
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.MessagesListView_MSG) {
            menu.setHeaderTitle(getResources().getString(R.string.Message));
            String[] menuItems = {getResources().getString(R.string.longClickDelete)};
            for (int i = 0; i<menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }
/**************************************************************************************************/
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        String[] menuItems = {getResources().getString(R.string.longClickDelete)};
        if(menuItems[menuItemIndex].equals(getResources().getString(R.string.longClickDelete))){
            new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.Message)).setMessage(getResources().getString(R.string.Message_delete_MS))
                    .setPositiveButton(getResources().getString(R.string.AlertDialog_yes_button_MS), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            JSONObject jsonToSend = new JSONObject();
                            try{
                                JSONObject messageInfo = new JSONObject(messages_LV.getItemAtPosition(info.position).toString());
                                jsonToSend.put("from", messageInfo.getString("from"));
                                jsonToSend.put("to", USER_DATA.getUsername());
                                jsonToSend.put("message", messageInfo.getString("message"));
                                ThreadedConnection deleteRoom = new ThreadedConnection("msgDelete", "POST", jsonToSend.toString());
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
            JSONObject jsonReceived;
            try {
                jsonReceived = new JSONObject(jsonin);
                if(request.equals("fetchMessages")){
                    ArrayList<String> from = new ArrayList<>();
                    ArrayList<String> messages = new ArrayList<>();
                    ArrayList<String> images = new ArrayList<>();
                    for (int i=0;i<jsonReceived.getJSONArray("from").length();i++){
                        from.add(jsonReceived.getJSONArray("from").getString(i));
                        messages.add(jsonReceived.getJSONArray("messages").getString(i));
                        images.add(jsonReceived.getJSONArray("images").getString(i));
                    }
                    MessagesAdapter adapter = new MessagesAdapter(MSGScreen, from, messages, images);
                    messages_LV.setAdapter(adapter);
                } else if(request.equals("msgDelete")){
                    Toast.makeText(MSGScreen, jsonReceived.getString("msg"), Toast.LENGTH_LONG).show();
                    JSONObject jsonToSend = new JSONObject();
                    try{
                        jsonToSend.put("username", USER_DATA.getUsername());
                        ThreadedConnection fetchMessages = new ThreadedConnection("fetchMessages", "POST", jsonToSend.toString());
                        fetchMessages.execute();
                    } catch (JSONException e){
                        e.printStackTrace();
                    }
                }
                progress.dismiss();
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
