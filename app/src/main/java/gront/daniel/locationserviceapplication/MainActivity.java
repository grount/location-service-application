package gront.daniel.locationserviceapplication;

import android.location.LocationListener;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    private ImageButton button;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        modifySearchesTextView();

        button = (ImageButton) findViewById(R.id.currentLocationRefreshImageButton);
        textView = (TextView) findViewById(R.id.currentLocationInputTextView);

    }


    protected void modifySearchesTextView()
    {
        String amountToDisplay = "5";
        TextView searchesID = (TextView) findViewById(R.id.searchesTextView);
        searchesID.setText("Last " + amountToDisplay + " searched addresses:");
    }
}



