package gr.uoa.di.ecommerce.myairbnb;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;


public class UserProfileScreen extends AppCompatActivity {
    private String username_label;
    private String password_label;
    private String firstName_label;
    private String lastName_label;
    private String email_label;
    private String telephone_label;

    private TextView show_username;
    private TextView show_password;
    private TextView show_firstName;
    private TextView show_lastName;
    private TextView show_email;
    private TextView show_telephone;
    private CheckBox show_host;
    private ImageView show_userImage;

/**************************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userprofilescreen);

        show_username = (TextView) findViewById(R.id.Username_UPS);
        show_password = (TextView) findViewById(R.id.Password_UPS);
        show_firstName = (TextView) findViewById(R.id.FirstName_UPS);
        show_lastName = (TextView) findViewById(R.id.LastName_UPS);
        show_email = (TextView) findViewById(R.id.Email_UPS);
        show_telephone = (TextView) findViewById(R.id.Telephone_UPS);
        show_host = (CheckBox) findViewById(R.id.Host_UPS);
        show_userImage = (ImageView) findViewById(R.id.UserImage_UPS);

        username_label = show_username.getText().toString();
        password_label = show_password.getText().toString();
        firstName_label = show_firstName.getText().toString();
        lastName_label = show_lastName.getText().toString();
        email_label = show_email.getText().toString();
        telephone_label = show_telephone.getText().toString();

        show_username.setText(username_label + " " + USER_DATA.getUsername());
        show_password.setText(password_label + " " + USER_DATA.getPassword());
        show_firstName.setText(firstName_label + " " + USER_DATA.getFirstName());
        show_lastName.setText(lastName_label + " " + USER_DATA.getLastName());
        show_email.setText(email_label + " " + USER_DATA.getEmail());
        show_telephone.setText(telephone_label + " " + USER_DATA.getTelephone());
        show_host.setChecked(USER_DATA.isHost());
        show_userImage.setImageBitmap(USER_DATA.getUserImage_bitmap());


        Button editButton = (Button) findViewById(R.id.EditButton_UPS);
        editButton.setOnClickListener(handleEditClick(this));

        Button saveButton = (Button) findViewById(R.id.SaveButton_UPS);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                USER_DATA.setPassword(USER_DATA.getPassword());
                USER_DATA.setFirstName(USER_DATA.getFirstName());
                USER_DATA.setLastName(USER_DATA.getLastName());
                USER_DATA.setEmail(USER_DATA.getEmail());
                USER_DATA.setTelephone(USER_DATA.getTelephone());
                USER_DATA.setHost(USER_DATA.isHost());
                USER_DATA.setUserImage_bitmap(USER_DATA.getUserImage_bitmap());
                setResult(12);
                finish();
            }
        });
    }
/**************************************************************************************************/
    private View.OnClickListener handleEditClick(final Context userprofilescreen) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent UPS_intent = new Intent(userprofilescreen, EditProfileScreen.class);
                startActivityForResult(UPS_intent ,100);
            }
        };
    }
/**************************************************************************************************/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 100) {
            try {
                JSONObject jsonReceived = new JSONObject(data.getStringExtra("JSONString"));
                if (jsonReceived.getString("res").equals("valid")) {
                    USER_DATA.setPassword(jsonReceived.getString("password"));
                    USER_DATA.setFirstName(jsonReceived.getString("firstName"));
                    USER_DATA.setLastName(jsonReceived.getString("lastName"));
                    USER_DATA.setEmail(jsonReceived.getString("email"));
                    if (!jsonReceived.getString("telephone").equals(""))
                        USER_DATA.setTelephone(jsonReceived.getString("telephone"));
                    else
                    USER_DATA.setTelephone("N/A");
                    USER_DATA.setHost(jsonReceived.getBoolean("host"));
                    String userImage_String = jsonReceived.getString("userImage");
                    byte[] userImage_byteArray = Base64.decode(userImage_String, Base64.DEFAULT);
                    USER_DATA.setUserImage_bitmap(BitmapFactory.decodeByteArray(userImage_byteArray, 0, userImage_byteArray.length));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            show_username.setText(username_label + " " + USER_DATA.getUsername());
            show_password.setText(password_label + " " + USER_DATA.getPassword());
            show_firstName.setText(firstName_label + " " + USER_DATA.getFirstName());
            show_lastName.setText(lastName_label + " " + USER_DATA.getLastName());
            show_email.setText(email_label + " " + USER_DATA.getEmail());
            show_telephone.setText(telephone_label + " " + USER_DATA.getTelephone());
            show_host.setChecked(USER_DATA.isHost());
            show_userImage.setImageBitmap(USER_DATA.getUserImage_bitmap());
        }
    }
}
