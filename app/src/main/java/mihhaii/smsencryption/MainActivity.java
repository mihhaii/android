package mihhaii.smsencryption;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
    }

    public void onClickSms(View v)
    {
        Intent intent = new Intent(this, SmsMainActivity.class);
        startActivity(intent);
    }
}
