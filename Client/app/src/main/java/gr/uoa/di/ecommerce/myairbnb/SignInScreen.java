package gr.uoa.di.ecommerce.myairbnb;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.SSLCertificateSocketFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

public class SignInScreen extends AppCompatActivity {

    private EditText username_editText;
    private EditText password_editText;

    private Context SIScreen;
    private String jsonin = "";

    private SharedPreferences mPrefs;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signinscreen);
        SIScreen = getApplicationContext();

        /*Initialize loading*/
        progress = new ProgressDialog(SignInScreen.this);
        progress.setTitle("Loading");
        progress.setMessage("Please wait");
        progress.setCancelable(false);

        /*Find the Views*/
        username_editText = (EditText) findViewById(R.id.Username_SIS);
        password_editText = (EditText) findViewById(R.id.Password_SIS);
        Button signIn = (Button) findViewById(R.id.LogInBtn_SIS);
        signIn.setOnClickListener(handleSignInClick());
        Button signUp = (Button) findViewById(R.id.RegisterBtn_SIS);
        signUp.setOnClickListener(handleSignUpClick());

        /*app permissions*/
        PackageManager pm = getPackageManager();
        int hasStoragePermission = pm.checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, getPackageName());
        if(hasStoragePermission != PackageManager.PERMISSION_GRANTED){
            new AlertDialog.Builder(this).setTitle(R.string.AlertDialog_title_FES).setMessage(R.string.Storage_permission_text_FES)
                    .setPositiveButton(R.string.AlertDialog_yes_button_FES, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        }
                    }).setNegativeButton(R.string.AlertDialog_no_button_FES, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).show();
        }

        final Button setIP = (Button) findViewById(R.id.SetIp_SIS);
        setIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SIScreen, SetIP.class));
            }
        });
    }
/**************************************************************************************************/
    @Override
    protected void onResume() {
        super.onResume();
        mPrefs = getSharedPreferences("LSprefs",0);
        JSONObject jsonToSend = new JSONObject();
        try {
            jsonToSend.put("username", mPrefs.getString("username", ""));
            jsonToSend.put("jwt", mPrefs.getString("jwt", ""));
            ThreadedConnection threadedConnection = new ThreadedConnection("checkJWT","POST",jsonToSend.toString());
            threadedConnection.execute();
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
                    new AlertDialog.Builder(SignInScreen.this).setTitle(getResources().getString(R.string.AlertDialogTitle_SIS))
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
    private View.OnClickListener handleSignInClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Loading*/
                progress.show();
                timerDelayRemoveDialog(7000, progress);
                JSONObject jsonToSend = new JSONObject();
                try {
                    jsonToSend.put("username", username_editText.getText().toString());
                    jsonToSend.put("password", password_editText.getText().toString());
                    jsonToSend.put("jwt", "");
                    ThreadedConnection threadedConnection = new ThreadedConnection("signIn","POST",jsonToSend.toString());
                    threadedConnection.execute();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
    }
/**************************************************************************************************/
    private View.OnClickListener handleSignUpClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SIScreen, SignUpScreen.class));
            }
        };
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
            String jsonString = (String) o;
            try {
                jsonReceived = new JSONObject(jsonString);
                if(request.equals("signIn")){
                    if (jsonReceived.getString("res").equals("valid")) {
                        USER_DATA.setJWT(jsonReceived.getString("jwt"));
                        USER_DATA.setUsername(jsonReceived.getString("username"));
                        USER_DATA.setPassword(jsonReceived.getString("password"));
                        USER_DATA.setFirstName(jsonReceived.getString("firstName"));
                        USER_DATA.setLastName(jsonReceived.getString("lastName"));
                        USER_DATA.setEmail(jsonReceived.getString("email"));
                        if(!jsonReceived.getString("telephone").equals(""))
                            USER_DATA.setTelephone(jsonReceived.getString("telephone"));
                        else
                            USER_DATA.setTelephone("N/A");
                        USER_DATA.setHost(jsonReceived.getBoolean("host"));
                        String userImage_String = jsonReceived.getString("userImage");
                        byte[] userImage_byteArray = Base64.decode(userImage_String, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(userImage_byteArray, 0, userImage_byteArray.length);
                        USER_DATA.setUserImage_bitmap(bitmap);

                        /*Write in memory*/
                        SharedPreferences.Editor editor;
                        editor = mPrefs.edit();
                        editor.putString("jwt", USER_DATA.getJWT());
                        editor.commit();
                        startActivity(new Intent(SIScreen,MainScreen.class));
                        if(progress.isShowing()){
                            progress.dismiss();
                        }
                        finish();
                    }
                    else if (jsonReceived.getString("res").equals("error")) {
                        password_editText.setText("");
                        Toast.makeText(SIScreen, jsonReceived.getString("msg"), Toast.LENGTH_LONG).show();
                        progress.dismiss();
                    }
                } else if(request.equals("checkJWT")){
                    if(jsonReceived.getString("res").equals("Valid")){
                        JSONObject jsonToSend = new JSONObject();
                        try {
                            jsonToSend.put("jwt", jsonReceived.getString("jwt"));
                            ThreadedConnection threadedConnection = new ThreadedConnection("signIn","POST",jsonToSend.toString());
                            threadedConnection.execute();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        finish();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    }
}
