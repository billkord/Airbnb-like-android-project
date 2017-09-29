package gr.uoa.di.ecommerce.myairbnb;


import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.provider.DocumentsContract;
import android.support.v7.app.AppCompatActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.SSLCertificateSocketFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
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

public class EditProfileScreen extends AppCompatActivity {
    private int PICK_IMAGE_REQUEST = 1;

    private EditText password_editText;
    private EditText validatePassword_editText;
    private EditText firstName_editText;
    private EditText lastName_editText;
    private EditText email_editText;
    private EditText telephone_editText;
    private CheckBox host_checkBox;
    private ImageView userImage_imageView;

    private String jsonin = "";
    private Context EPScreen;
    private Intent UPS_intent;

/**************************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editprofilescreen);
        EPScreen = getApplicationContext();
        UPS_intent = getIntent();

        /*Find all the Views from Resource*/
        password_editText           = (EditText) findViewById(R.id.Password_EPS);
        validatePassword_editText   = (EditText) findViewById(R.id.ValidatePassword_EPS);
        firstName_editText          = (EditText) findViewById(R.id.FirstName_EPS);
        lastName_editText           = (EditText) findViewById(R.id.LastName_EPS);
        email_editText              = (EditText) findViewById(R.id.Email_EPS);
        telephone_editText          = (EditText) findViewById(R.id.Telephone_EPS);
        host_checkBox               = (CheckBox) findViewById(R.id.Host_EPS);
        userImage_imageView         = (ImageView) findViewById(R.id.UserImage_EPS);

        /*Set Values to upper Views*/
        password_editText.setText(USER_DATA.getPassword());
        validatePassword_editText.setText(USER_DATA.getPassword());
        firstName_editText.setText(USER_DATA.getFirstName());
        lastName_editText.setText(USER_DATA.getLastName());
        email_editText.setText(USER_DATA.getEmail());
        if(!USER_DATA.getTelephone().equals("N/A"))
            telephone_editText.setText(USER_DATA.getTelephone());
        else
            telephone_editText.setText("");
        host_checkBox.setChecked(USER_DATA.isHost());
        userImage_imageView.setImageBitmap(USER_DATA.getUserImage_bitmap());


        Button doneButton = (Button) findViewById(R.id.DoneButton_EPS);
        doneButton.setText(R.string.SaveChanges_button_name_EPS);
        doneButton.setOnClickListener(handleDoneClick(this));
        FloatingActionButton selectImage = (FloatingActionButton) findViewById(R.id.selectImageButton_EPS);
        selectImage.setOnClickListener(handleSelectImageClick());
    }
/**************************************************************************************************/
    private View.OnClickListener handleSelectImageClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Picture"), PICK_IMAGE_REQUEST);
            }
        };
    }
/**************************************************************************************************/
    private View.OnClickListener handleDoneClick(final Context editprofilescreen) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(allRequiredFilled()) {
                    if(correctPassword()){
                        JSONObject jsonToSend = new JSONObject();
                        try {
                            jsonToSend.put("username", USER_DATA.getUsername());
                            jsonToSend.put("password", password_editText.getText().toString());
                            jsonToSend.put("firstName", firstName_editText.getText().toString());
                            jsonToSend.put("lastName", lastName_editText.getText().toString());
                            jsonToSend.put("email", email_editText.getText().toString());
                            if (telephone_editText.getText().toString().equals(""))
                                jsonToSend.put("telephone", "N/A");
                            else
                                jsonToSend.put("telephone", telephone_editText.getText().toString());
                            jsonToSend.put("host", host_checkBox.isChecked());
                            Bitmap bitmap = USER_DATA.getUserImage_bitmap();
                            String userImage_String;
                            if(bitmap != null) {
                                ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOS);
                                userImage_String = Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
                            } else {
                                 userImage_String = "";
                            }
                            jsonToSend.put("userImage", userImage_String);
                            ThreadedConnection threadedConnection = new ThreadedConnection("updateUser", "POST", String.valueOf(jsonToSend));
                            threadedConnection.execute();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        password_editText.setText("");
                        password_editText.setHintTextColor(Color.parseColor("#990000"));

                        validatePassword_editText.setText("");
                        validatePassword_editText.setHintTextColor(Color.parseColor("#990000"));

                        Toast.makeText(editprofilescreen,"Incorrect Password", Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    findTheEmptyFields();
                }
            }
        };
    }
/**************************************************************************************************/
    private boolean correctPassword() {
        String pass1 = password_editText.getText().toString();
        String pass2 = validatePassword_editText.getText().toString();
        return pass1.equals(pass2) && !pass1.equals("");
    }
/**************************************************************************************************/
    private boolean allRequiredFilled() {
        boolean filled = !password_editText.getText().toString().equals("");
        filled = filled && !validatePassword_editText.getText().toString().equals("");
        filled = filled && !firstName_editText.getText().toString().equals("");
        filled = filled && !lastName_editText.getText().toString().equals("");
        filled = filled && !email_editText.getText().toString().equals("");
        return filled;
    }
/**************************************************************************************************/
    private void findTheEmptyFields() {
        if(password_editText.getText().toString().equals(""))
            password_editText.setHintTextColor(Color.parseColor("#990000"));
        if(validatePassword_editText.getText().toString().equals(""))
            validatePassword_editText.setHintTextColor(Color.parseColor("#990000"));
        if(firstName_editText.getText().toString().equals(""))
            firstName_editText.setHintTextColor(Color.parseColor("#990000"));
        if(lastName_editText.getText().toString().equals(""))
            lastName_editText.setHintTextColor(Color.parseColor("#990000"));
        if(email_editText.getText().toString().equals(""))
            email_editText.setHintTextColor(Color.parseColor("#990000"));
    }
/**************************************************************************************************/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /* The following code happens when the user selects a photo from the device */
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            ImageFactory image = new ImageFactory(selectedImageUri);
            image.execute();
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

        @Override
        protected void onPostExecute(Object o) {
            JSONObject jsonReceived;
            String jsonString = (String) o;
            UPS_intent.putExtra("JSONString", jsonin);
            try {
                jsonReceived = new JSONObject(jsonString);
                if (jsonReceived.getString("res").equals("valid")) {
                    setResult(100, UPS_intent);
                    finish();
                }
                if (jsonReceived.getString("res").equals("error")) {
                    Toast.makeText(EPScreen, jsonReceived.getString("msg"), Toast.LENGTH_LONG).show();
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
/*******************************************************************************************************************************************/
    private class ImageFactory extends AsyncTask<Object, Object, Object>{
        private Bitmap bitmap;
        private final int height = 100;
        private final int width = 100;
        private Uri uri;

        private ImageFactory(Uri uri){
            this.uri = uri;
        }

        @Override
        protected String doInBackground(Object[] Params){
            ContentResolver cr = EditProfileScreen.this.getContentResolver();
            String filePath;
            ExifInterface exif;
            try {
                filePath = getFilePath(cr);
                exif = new ExifInterface(filePath);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                bitmap = decodeSampledBitmapFromFile(filePath, width, height);
                bitmap = rotateBitmap(bitmap, orientation);
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                Toast.makeText(EditProfileScreen.this, getResources().getString(R.string.out_of_memory_text), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object str){
            try{
                userImage_imageView.setImageBitmap(null);
                userImage_imageView.setImageBitmap(bitmap);
                USER_DATA.setUserImage_bitmap(bitmap);
            }catch(OutOfMemoryError e){
                e.printStackTrace();
                Toast.makeText(EditProfileScreen.this, getResources().getString(R.string.out_of_memory_text), Toast.LENGTH_LONG).show();
            }
        }
/**************************************************************************************************/
        private String getFilePath(ContentResolver cr){
            String path = "";
            try{
                String[] source = uri.toString().split("//")[1].split("/");
                if((source[0]+"/"+source[1]).equals("media/external")){
                    Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                    if (cursor == null) { // Source is Dropbox or other similar local file path
                        path = uri.getPath();
                    } else {
                        cursor.moveToFirst();
                        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                        path = cursor.getString(idx);
                        cursor.close();
                    }
                } else {
                    String wholeID = DocumentsContract.getDocumentId(uri);
                    String[] splits = wholeID.split(":");
                    if (splits.length == 2) {
                        String id = splits[1];
                        String[] column = {MediaStore.Images.Media.DATA};
                        String sel = MediaStore.Images.Media._ID + "=?";
                        Cursor cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, sel, new String[]{id}, null);
                        assert cursor != null;
                        int columnIndex = cursor.getColumnIndex(column[0]);
                        if (cursor.moveToFirst()) {
                            path = cursor.getString(columnIndex);
                        } else{
                            path = "";
                        }
                        cursor.close();
                    } else{
                        path = uri.getPath();
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
            return path;
        }
/**************************************************************************************************/
        private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;
            if (height > reqHeight || width > reqWidth){
                final int halfHeight = height / 2;
                final int halfWidth = width / 2;
                    /*Calculate the largest inSampleSize value that is a power of 2 and keeps both
                     *height and width larger than the requested height and width.*/
                while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth){
                    inSampleSize *= 2;
                }
            }
            return inSampleSize;
        }
/**************************************************************************************************/
        private Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight){
                /*First decode with inJustDecodeBounds = true to check dimensions*/
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(path, options);
        }
/**************************************************************************************************/
        private Bitmap rotateBitmap(Bitmap bitmap, int orientation){
            Matrix matrix = new Matrix();
            switch (orientation){
                case ExifInterface.ORIENTATION_NORMAL:
                    return bitmap;
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.setScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.setRotate(180);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.setRotate(180);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    matrix.setRotate(90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.setRotate(90);
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    matrix.setRotate(-90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.setRotate(-90);
                    break;
                default:
                    return bitmap;
            }
            try{
                Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                bitmap.recycle();
                return bmRotated;
            } catch (OutOfMemoryError e){
                e.printStackTrace();
                return null;
            }
        }
    }
}
