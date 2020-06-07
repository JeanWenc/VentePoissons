package jean.wencelius.ventepoissons.controller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import jean.wencelius.ventepoissons.R;
import jean.wencelius.ventepoissons.model.AppPreferences;
import jean.wencelius.ventepoissons.recopemValues;

public class MainActivity extends AppCompatActivity {

    private int SPLASH_TIME = 3000;

    private String mFisherName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        TextView mGreetingText = (TextView) findViewById(R.id.activity_main_greeting_text);

        mFisherName = AppPreferences.getDefaultsString(recopemValues.PREF_KEY_FISHER_NAME,getApplicationContext());

        if(mFisherName!=null){
            String fulltext = getResources().getString(R.string.activity_main_hello);
            if(mFisherName.length()>0) fulltext+=mFisherName;
            mGreetingText.setText(fulltext);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        Thread timer = new Thread() {
            public void run() {
                try {
                    sleep(SPLASH_TIME);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (mFisherName!=null){
                        Intent menuActivityIntent = new Intent(MainActivity.this, MenuActivity.class);
                        startActivity(menuActivityIntent);
                    }else{
                        Intent loginActivityIntent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(loginActivityIntent);
                    }
                }// end tryCatch
            }//end run()
        };//end timerThread

        timer.start();
    }
}

