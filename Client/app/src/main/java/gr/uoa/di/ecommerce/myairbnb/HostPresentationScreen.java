package gr.uoa.di.ecommerce.myairbnb;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.SSLCertificateSocketFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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

public class HostPresentationScreen extends AppCompatActivity {

    private ImageView userImage;
    private TextView firstName;
    private TextView lastName;
    private TextView phone;
    private ListView ratesListView;
    private Context HPScreen;
    private String jsonin;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hostpresentationscreen);
        HPScreen = this;
        int HOSTPAGE_VISITED = 1;
        setResult(HOSTPAGE_VISITED);

        /*Loading*/
        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Please wait");
        progress.setCancelable(false);
        progress.show();
        timerDelayRemoveDialog(7000, progress);

        /*Find the views*/
        userImage = (ImageView) findViewById(R.id.UserImage_HPS);
        ratesListView = (ListView) findViewById(R.id.RatesListview_HPS);
        firstName = (TextView) findViewById(R.id.FirstName_HPS);
        lastName = (TextView) findViewById(R.id.LastName_HPS);
        phone = (TextView) findViewById(R.id.Telephone_HPS);

        try{
            JSONObject jsonToSend = new JSONObject();
            jsonToSend.put("hostName", getIntent().getStringExtra("hostName"));
            jsonToSend.put("roomName", getIntent().getStringExtra("roomName"));
            ThreadedConnection fetchRoomInfo = new ThreadedConnection("hostRoomInfo", "POST", jsonToSend.toString());
            fetchRoomInfo.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
/**************************************************************************************************/
    public void timerDelayRemoveDialog(long time, final ProgressDialog d){
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if(d.isShowing()){
                    d.dismiss();
                    new AlertDialog.Builder(HostPresentationScreen.this).setTitle(getResources().getString(R.string.AlertDialogTitle_SIS))
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
                firstName.setText(firstName.getText().toString() + ": " + jsonReceived.getString("firstName"));
                lastName.setText(lastName.getText().toString() + ": " + jsonReceived.getString("lastName"));
                phone.setText(phone.getText().toString() + " " + jsonReceived.getString("telephone"));
                Bitmap bitmap;
                if(!jsonReceived.getString("userImage").equals("")){
                    byte[] roomImage_byteArray = Base64.decode(jsonReceived.getString("userImage"), Base64.DEFAULT);
                    bitmap = BitmapFactory.decodeByteArray(roomImage_byteArray, 0, roomImage_byteArray.length);
                } else {
                    bitmap = null;
                }
                userImage.setImageBitmap(bitmap);
                ArrayList<String> rates = new ArrayList<>();
                ArrayList<String> from = new ArrayList<>();
                ArrayList<String> images = new ArrayList<>();
                for (int i=0;i<jsonReceived.getJSONArray("rates").length();i++){
                    rates.add(jsonReceived.getJSONArray("rates").getString(i));
                    from.add(jsonReceived.getJSONArray("from").getString(i));
                    images.add(jsonReceived.getJSONArray("images").getString(i));
                    }
                HostRatingAdapter adapter = new HostRatingAdapter(HPScreen, images, from, rates);
                ratesListView.setAdapter(adapter);
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
            } catch (Exception e) { // should never happen
                e.printStackTrace();
            }
        }
    }
}
