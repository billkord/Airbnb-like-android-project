package gr.uoa.di.ecommerce.myairbnb;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

class MessagesAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> from;
    private ArrayList<String> messages;
    private ArrayList<String> images;

    MessagesAdapter(Context context, ArrayList<String> from, ArrayList<String> messages, ArrayList<String> images){
        this.context = context;
        this.from = from;
        this.messages = messages;
        this.images = images;
    }

    @Override
    public int getCount() {
        return from.size();
    }

    @Override
    public String getItem(int position) {
        JSONObject jsonToSend = new JSONObject();
        try{
            jsonToSend.put("from", from.get(position));
            jsonToSend.put("message", messages.get(position));
            jsonToSend.put("image", images.get(position));
        } catch (JSONException e){
            e.printStackTrace();
        }
        return jsonToSend.toString();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.messageinflator, null);
        }

        TextView from_TV = (TextView) convertView.findViewById(R.id.From_MSGInflator);
        TextView message = (TextView) convertView.findViewById(R.id.Message_MSGInflator);
        ImageView image = (ImageView) convertView.findViewById(R.id.SenderImage_MsgInfaltor);
        Bitmap bitmap;
        if (!images.get(position).equals("")) {
            byte[] roomImage_byteArray = Base64.decode(images.get(position), Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(roomImage_byteArray, 0, roomImage_byteArray.length);
        } else {
            bitmap = null;
        }

        from_TV.setText(convertView.getResources().getString(R.string.From)+" "+from.get(position));
        message.setText(messages.get(position));
        image.setImageBitmap(bitmap);
        return convertView;
    }
}
