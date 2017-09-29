package gr.uoa.di.ecommerce.myairbnb;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.icu.text.SimpleDateFormat;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.SSLCertificateSocketFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
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

public class AddRoomScreen extends AppCompatActivity{
    private final int PICK_IMAGE_MULTIPLE = 200;
    private String jsonin;
    private JSONArray images;
    private JSONArray names;

    private EditText roomName;
    private EditText country;
    private EditText city;
    private EditText address;
    private EditText dateFrom;
    private EditText dateTo;
    private EditText maxVisitors;
    private EditText minPrice;
    private EditText roomType;
    private EditText rules;
    private EditText description;
    private EditText area;
    private ImageView roomImage;

    private Context ARScreen;
    private Intent intent;
    private ProgressDialog progress;

    private Calendar myCalendar;
    private SupportMapFragment mapFragment;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addroomscreen);
        ARScreen = getApplicationContext();
        myCalendar = Calendar.getInstance();

        intent = getIntent();
        /*Means the user pressed the room to edit*/
        if (intent.getStringExtra("roomName") != null) {
            /*Loading*/
            progress = new ProgressDialog(this);
            progress.setTitle("Loading");
            progress.setMessage("Please wait");
            progress.setCancelable(false);
            progress.show();
            timerDelayRemoveDialog(15000, progress);
            JSONObject jsonToSend = new JSONObject();
            try {
                jsonToSend.put("roomName", intent.getStringExtra("roomName"));
                jsonToSend.put("type", "ONE");
                jsonToSend.put("jwt", USER_DATA.getJWT());
                ThreadedConnection reviewRoom = new ThreadedConnection("returnRooms", "POST", jsonToSend.toString());
                reviewRoom.execute();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        /*Find The Views*/
        roomName = (EditText) findViewById(R.id.RoomName_ARS);
        country = (EditText) findViewById(R.id.Country_ARS);
        city = (EditText) findViewById(R.id.City_ARS);
        address = (EditText) findViewById(R.id.Address_ARS);
        dateFrom = (EditText) findViewById(R.id.DateFrom_ARS);
        dateTo = (EditText) findViewById(R.id.DateTo_ARS);
        maxVisitors = (EditText) findViewById(R.id.MaxVisitors_ARS);
        minPrice = (EditText) findViewById(R.id.MinPrice_ARS);
        roomType = (EditText) findViewById(R.id.RoomType_ARS);
        rules = (EditText) findViewById(R.id.Rules_ARS);
        description = (EditText) findViewById(R.id.Description_ARS);
        area = (EditText) findViewById(R.id.Area_ARS);
        roomImage = (ImageView) findViewById(R.id.RoomImage_ARS);
        Button saveButton = (Button) findViewById(R.id.SaveButton_ARS);
        FloatingActionButton addImage = (FloatingActionButton) findViewById(R.id.SelectImageButton_ARS);
        mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.Map_ARS));

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ARScreen, CustomPhotoGalleryScreen.class);
                startActivityForResult(intent, PICK_IMAGE_MULTIPLE);
            }
        });
        saveButton.setOnClickListener(handleSaveClick());
        dateFrom.setOnClickListener(handleDateClick(dateFrom));
        dateTo.setOnClickListener(handleDateClick(dateTo));

        address.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.i("BillMEssage", ""+ EditorInfo.IME_ACTION_SEARCH +" "+ EditorInfo.IME_ACTION_DONE + " -----> " + actionId);
                Log.i("BillMEssage", ""+KeyEvent.ACTION_DOWN+" "+KeyEvent.KEYCODE_ENTER+" ---> "+ event.getAction());
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (!event.isShiftPressed()) {
                        /*Location permissions*/
                        PackageManager pm = getPackageManager();
                        int hasStoragePermission = pm.checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, getPackageName());
                        if (hasStoragePermission != PackageManager.PERMISSION_GRANTED) {
                            new AlertDialog.Builder(ARScreen).setTitle(R.string.AlertDialog_title_ARS).setMessage(R.string.Storage_permission_text_ARS)
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
                        } else {
                            if (mapFragment != null) {
                                mapFragment.getMapAsync(new OnMapReadyCallback() {
                                    @Override
                                    public void onMapReady(GoogleMap map) {
                                        if (marker != null) {
                                            marker.remove();
                                        }
                                        loadMap(map);
                                    }
                                });
                            }
                        }
                        return true;
                    }
                }
                return false;
            }
        });
    }
/**************************************************************************************************/
    public void timerDelayRemoveDialog(long time, final ProgressDialog d){
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if(d.isShowing()){
                    d.dismiss();
                    new AlertDialog.Builder(AddRoomScreen.this).setTitle(getResources().getString(R.string.AlertDialogTitle_SIS))
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
    private void loadMap(GoogleMap googleMap) {
        if (googleMap != null) {
            // Map is ready
            googleMap.getUiSettings().setScrollGesturesEnabled(false);
            googleMap.getUiSettings().setZoomControlsEnabled(false);
            googleMap.getUiSettings().setZoomGesturesEnabled(false);
            LatLng position = getLocationFromAddress(mapFragment.getContext(), address.getText().toString() +", "+ city.getText().toString()+", "+country.getText().toString());
            if (position != null) {
                marker = googleMap.addMarker(new MarkerOptions().position(position));
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(position));
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, 14);
                googleMap.animateCamera(cameraUpdate);
            } else {
                Toast.makeText(ARScreen, getResources().getString(R.string.Map_error), Toast.LENGTH_LONG).show();
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
            if (address.size() == 0){
                throw new IOException();
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
    private View.OnClickListener handleDateClick(final EditText editText) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String myFormat = "dd/MM/yyyy";
                final int ONE_DAY_MILLIS = 86400000;
                final SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ENGLISH);
                DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        editText.setText(sdf.format(myCalendar.getTime()));
                    }
                };
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddRoomScreen.this, date,
                        myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));
                try {
                    if (editText.getId() == R.id.DateFrom_ARS) {
                        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                        dateTo.setText("");
                    } else if (editText.getId() == R.id.DateTo_ARS) {
                        datePickerDialog.getDatePicker().setMinDate(sdf.parse(dateFrom.getText().toString()).getTime() + ONE_DAY_MILLIS);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                datePickerDialog.show();
            }
        };
    }
/**************************************************************************************************/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_MULTIPLE) {
                String[] imagesPath = data.getStringExtra("data").split("\\|");
                ExifInterface exif;
                try{
                    if (imagesPath.length != 0) {
                        exif = new ExifInterface(imagesPath[0]);
                        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                        ArrayList<Bitmap> bitmaps = new ArrayList<>();
                        images = new JSONArray();
                        names = new JSONArray();
                        for (int i = 0; i < imagesPath.length; i++) {
                            names.put(imagesPath[i]);
                            Bitmap bitmap = decodeSampledBitmapFromFile(imagesPath[i], 100, 100);
                            bitmaps.add(rotateBitmap(bitmap, orientation));
                            ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
                            bitmaps.get(i).compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOS);
                            images.put(Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT));
                        }
                        roomImage.setImageBitmap(bitmaps.get(0));
                    }
                } catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
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
/**************************************************************************************************/
    private boolean allRequiredRoomFilled() {
        boolean filled = !roomName.getText().toString().equals("");
        filled = filled && !country.getText().toString().equals("");
        filled = filled && !city.getText().toString().equals("");
        filled = filled && !address.getText().toString().equals("");
        filled = filled && !dateFrom.getText().toString().equals("");
        filled = filled && !dateTo.getText().toString().equals("");
        filled = filled && !maxVisitors.getText().toString().equals("");
        filled = filled && !minPrice.getText().toString().equals("");
        filled = filled && !roomType.getText().toString().equals("");
        filled = filled && !rules.getText().toString().equals("");
        filled = filled && !description.getText().toString().equals("");
        filled = filled && !area.getText().toString().equals("");
        return filled;
    }
/**************************************************************************************************/
    private void findTheEmptyRoomFields() {
        if (roomName.getText().toString().equals(""))
            roomName.setHintTextColor(Color.parseColor("#990000"));
        if (country.getText().toString().equals(""))
            country.setHintTextColor(Color.parseColor("#990000"));
        if (city.getText().toString().equals(""))
            city.setHintTextColor(Color.parseColor("#990000"));
        if (address.getText().toString().equals(""))
            address.setHintTextColor(Color.parseColor("#990000"));
        if (dateFrom.getText().toString().equals(""))
            dateFrom.setHintTextColor(Color.parseColor("#990000"));
        if (dateTo.getText().toString().equals(""))
            dateTo.setHintTextColor(Color.parseColor("#990000"));
        if (maxVisitors.getText().toString().equals(""))
            maxVisitors.setHintTextColor(Color.parseColor("#990000"));
        if (minPrice.getText().toString().equals(""))
            minPrice.setHintTextColor(Color.parseColor("#990000"));
        if (roomType.getText().toString().equals(""))
            roomType.setHintTextColor(Color.parseColor("#990000"));
        if (rules.getText().toString().equals(""))
            rules.setHintTextColor(Color.parseColor("#990000"));
        if (description.getText().toString().equals(""))
            description.setHintTextColor(Color.parseColor("#990000"));
        if (area.getText().toString().equals(""))
            area.setHintTextColor(Color.parseColor("#990000"));
    }
/**************************************************************************************************/
    private View.OnClickListener handleSaveClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allRequiredRoomFilled()) {
                    /*Loading*/
                    progress = new ProgressDialog(AddRoomScreen.this);
                    progress.setTitle("Loading");
                    progress.setMessage("Please wait");
                    progress.setCancelable(false);
                    progress.show();
                    timerDelayRemoveDialog(7000, progress);
                    JSONObject jsonToSend = new JSONObject();
                    try {
                        jsonToSend.put("hostName", USER_DATA.getUsername());
                        jsonToSend.put("country", country.getText().toString());
                        jsonToSend.put("city", city.getText().toString());
                        jsonToSend.put("address", address.getText().toString());
                        jsonToSend.put("dateFrom", dateFrom.getText().toString());
                        jsonToSend.put("dateTo", dateTo.getText().toString());
                        jsonToSend.put("maxVisitors", Integer.parseInt(maxVisitors.getText().toString()));
                        jsonToSend.put("minPrice", minPrice.getText().toString());
                        jsonToSend.put("roomType", roomType.getText().toString());
                        jsonToSend.put("rules", rules.getText().toString());
                        jsonToSend.put("description", description.getText().toString());
                        jsonToSend.put("area", area.getText().toString());
                        jsonToSend.put("rate", 0);
                        if (images != null) {
                            jsonToSend.put("images", images);
                        } else {
                            jsonToSend.put("images", "");
                        }
                        jsonToSend.put("names", names);
                        ThreadedConnection send;
                        if (intent.getStringExtra("roomName") != null) {
                            jsonToSend.put("roomName", intent.getStringExtra("roomName"));
                            send = new ThreadedConnection("updateRoom", "POST", jsonToSend.toString());
                        } else {
                            jsonToSend.put("roomName", roomName.getText().toString());
                            send = new ThreadedConnection("addRoom", "POST", jsonToSend.toString());
                        }
                        send.execute();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    findTheEmptyRoomFields();
                    Toast.makeText(ARScreen, getResources().getString(R.string.ToastFillAllReq), Toast.LENGTH_LONG).show();
                }
            }
        };
    }
/**************************************************************************************************/
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
                switch (request) {
                    case "addRoom":
                        Toast.makeText(ARScreen, jsonReceived.getString("msg"), Toast.LENGTH_LONG).show();
                        if (jsonReceived.getString("res").equals("success")) {
                            setResult(1);
                            finish();
                        }
                        progress.dismiss();
                        break;
                    case "returnRooms":
                        roomName.setText(jsonReceived.getString("roomName"));
                        country.setText(jsonReceived.getString("country"));
                        city.setText(jsonReceived.getString("city"));
                        address.setText(jsonReceived.getString("address"));
                        dateFrom.setText(jsonReceived.getString("dateFrom"));
                        dateTo.setText(jsonReceived.getString("dateTo"));
                        maxVisitors.setText(jsonReceived.getString("maxVisitors"));
                        minPrice.setText(jsonReceived.getString("minPrice"));
                        roomType.setText(jsonReceived.getString("roomType"));
                        rules.setText(jsonReceived.getString("rules"));
                        description.setText(jsonReceived.getString("description"));
                        area.setText(jsonReceived.getString("area"));
                        Bitmap bitmap;
                        if (jsonReceived.getJSONArray("images").length() != 0) {
                            byte[] roomImage_byteArray = Base64.decode(jsonReceived.getJSONArray("images").getString(0), Base64.DEFAULT);
                            bitmap = BitmapFactory.decodeByteArray(roomImage_byteArray, 0, roomImage_byteArray.length);
                        } else {
                            bitmap = null;
                        }
                        roomImage.setImageBitmap(bitmap);

                        /*Map initialization*/
                        if (!address.getText().toString().equals("")) {
                            if (mapFragment != null) {
                                mapFragment.getMapAsync(new OnMapReadyCallback() {
                                    @Override
                                    public void onMapReady(GoogleMap map) {
                                        if (marker != null) {
                                            marker.remove();
                                        }
                                        loadMap(map);
                                    }
                                });
                            }
                        }
                        progress.dismiss();
                        break;
                    case "updateRoom":
                        Toast.makeText(ARScreen, jsonReceived.getString("msg"), Toast.LENGTH_LONG).show();
                        if (jsonReceived.getString("res").equals("success")) {
                            setResult(1);
                            finish();
                        }
                        progress.dismiss();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                progress.dismiss();
            }
        }

        private void trustEveryone() {
            try {
                HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, new X509TrustManager[]{new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] chain,
                                                   String authType) throws CertificateException {
                    }

                    public void checkServerTrusted(X509Certificate[] chain,
                                                   String authType) throws CertificateException {
                    }

                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }}, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(
                        context.getSocketFactory());
            } catch (Exception e) { // should never happen
                e.printStackTrace();
            }
        }
    }
}
