package gr.uoa.di.ecommerce.myairbnb;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

class HostRatingAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> images;
    private ArrayList<String> from;
    private ArrayList<String> rates;

    HostRatingAdapter(Context context, ArrayList<String> images, ArrayList<String> from, ArrayList<String> rates) {
        this.context = context;
        this.images = images;
        this.from = from;
        this.rates = rates;
    }

    @Override
    public int getCount() {
        return rates.size();
    }

    @Override
    public String getItem(int position) {
        return rates.get(position);
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
            convertView = inflater.inflate(R.layout.hostrating, null);
        }

        TextView rateTxt = (TextView) convertView.findViewById(R.id.Ratetxt);
        ImageView raterImage = (ImageView) convertView.findViewById(R.id.RaterImage);

        if (!images.get(position).equals("")) {
            byte[] roomImage_byteArray = Base64.decode(images.get(position), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(roomImage_byteArray, 0, roomImage_byteArray.length);
            raterImage.setImageBitmap(bitmap);
        } else {
            raterImage.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.defaultimage));
        }
        rateTxt.setText(convertView.getResources().getString(R.string.From) + " " + from.get(position) + "\n\n" + rates.get(position));
        return convertView;
    }
}
