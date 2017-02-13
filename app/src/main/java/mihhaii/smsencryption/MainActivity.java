package mihhaii.smsencryption;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import utils.Preferences;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Preferences.init(this);
        Preferences.clear();



    }

    public void onClickSms(View v)
    {
        Log.d("Activity","activity is about to start");
        checkIfApplicationHasKeysGenerated();

    }

    private void checkIfApplicationHasKeysGenerated() {

        if(Preferences.getString(Preferences.RSA_PUBLIC_KEY)==null && Preferences.getString(Preferences.RSA_PRIVATE_KEY)==null){
            Intent intent = new Intent(this, KeyGeneratorActivity.class);
            startActivity(intent);
        }else {
            Intent intent = new Intent(this, SmsMainActivity.class);
            startActivity(intent);
        }


    }


}
