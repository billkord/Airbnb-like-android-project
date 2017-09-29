package gr.uoa.di.ecommerce.myairbnb;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

class AreaPresentation_PageViewAdapter extends PagerAdapter{
    private Context context;
    private ArrayList<String> images;

    AreaPresentation_PageViewAdapter(Context context, ArrayList<String> images){
        this.context = context;
        this.images = images;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view == object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.swipe, null);
        ImageView roomImage = (ImageView) v.findViewById(R.id.ImageView_swipe);
        ImageButton leftArrow = (ImageButton) v.findViewById(R.id.LeftArrow_swipe);
        ImageButton rightArrow = (ImageButton) v.findViewById(R.id.RightArrow_swipe);
        if(position == 0){
            leftArrow.setVisibility(View.INVISIBLE);
        } else{
            leftArrow.setVisibility(View.VISIBLE);
        }
        if(position == images.size()-1){
            rightArrow.setVisibility(View.INVISIBLE);
        } else{
            rightArrow.setVisibility(View.VISIBLE);
        }
        Bitmap bitmap;
        if (!images.get(position).equals("")) {
            byte[] roomImage_byteArray = Base64.decode(images.get(position), Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(roomImage_byteArray, 0, roomImage_byteArray.length);
        } else {
            bitmap = null;
        }
        roomImage.setImageBitmap(bitmap);
        container.addView(v);
        return v;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }
}
