package jean.wencelius.ventepoissons.controller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import jean.wencelius.ventepoissons.R;
import jean.wencelius.ventepoissons.model.AppPreferences;
import jean.wencelius.ventepoissons.model.User;
import jean.wencelius.ventepoissons.recopemValues;

public class LoginActivity extends AppCompatActivity{

    private EditText mFisherNameInput;
    private EditText mFisherIdInput;

    private Button mSubmitButton;

    private User mUser;

    public LoginActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mFisherNameInput = (EditText) findViewById(R.id.activity_login_name_input);
        mFisherIdInput = (EditText) findViewById(R.id.activity_login_id_input);
        mSubmitButton = (Button) findViewById(R.id.activity_login_submit_btn);

        mUser = new User();

        mSubmitButton.setEnabled(false);

        mFisherIdInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSubmitButton.setEnabled(s.toString().length() != 0);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Permission not yet granted => Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE},
                    recopemValues.MY_DANGEROUS_PERMISSIONS_REQUESTS);
        }

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fishername = mFisherNameInput.getText().toString();
                String fisherid = mFisherIdInput.getText().toString();

                mUser.setFisherName(fishername);
                mUser.setFisherId(fisherid);

                AppPreferences.setDefaultsString(recopemValues.PREF_KEY_FISHER_NAME,fishername,getApplicationContext());
                AppPreferences.setDefaultsString(recopemValues.PREF_KEY_FISHER_ID,fisherid,getApplicationContext());

                //User clicked button
                Intent menuActivityIntent = new Intent(LoginActivity.this, MenuActivity.class);
                startActivity(menuActivityIntent);
            }
        });
    }

    //What happens after requesting permission? (Optional)
    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case recopemValues.MY_DANGEROUS_PERMISSIONS_REQUESTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    //TODO: MENU_ACTIVITY: Gérer éventualité où utilisateur refuse autorisations.
                    mSubmitButton.setEnabled(false);
                }
                return;
            }
        }
    }

    @Override
    public void onBackPressed() {

    }
}
