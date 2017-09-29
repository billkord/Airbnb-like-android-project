package gr.uoa.di.ecommerce.myairbnb;


import android.support.v7.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class MessageViewScreen extends AppCompatActivity{

    private Context MVScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messageviewscreen);
        MVScreen = this;

        /*Find Views*/
        ImageView image = (ImageView) findViewById(R.id.SenderImage_MVS);
        TextView from = (TextView) findViewById(R.id.From_MVS);
        TextView message = (TextView) findViewById(R.id.Message_MVS);
        FloatingActionButton replyBtn = (FloatingActionButton) findViewById(R.id.Reply_MVS);

        try {
            JSONObject messageInfo = new JSONObject(getIntent().getStringExtra("messageJson"));
            from.setText(getResources().getString(R.string.From) + " " + messageInfo.getString("from"));
            message.setText(messageInfo.getString("message"));
            Bitmap bitmap;
            if (!messageInfo.getString("image").equals("")) {
                byte[] roomImage_byteArray = Base64.decode(messageInfo.getString("image"), Base64.DEFAULT);
                bitmap = BitmapFactory.decodeByteArray(roomImage_byteArray, 0, roomImage_byteArray.length);
            } else {
                bitmap = null;
            }
            image.setImageBitmap(bitmap);

            replyBtn.setOnClickListener(handleReplyClick(messageInfo.getString("from")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
/**************************************************************************************************/
    private View.OnClickListener handleReplyClick(final String from){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toSendMessage = new Intent(MVScreen, SendMessageScreen.class);
                toSendMessage.putExtra("hostName", from);
                startActivity(toSendMessage);
            }
        };
    }
}
