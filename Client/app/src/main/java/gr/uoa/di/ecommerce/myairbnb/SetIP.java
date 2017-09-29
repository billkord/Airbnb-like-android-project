package gr.uoa.di.ecommerce.myairbnb;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SetIP extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setip);
        final EditText IPtxt = (EditText) findViewById(R.id.IPtxt);
        Button IPbtn = (Button) findViewById(R.id.IPbtn);
        IPbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                USER_DATA.setIP("https://"+IPtxt.getText().toString()+":4000/");
                finish();
            }
        });

    }
}
