package mihhaii.smsencryption;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;

import crypto.Crypto;
import crypto.RSA;
import sftp.SftpProtocol;
import utils.Preferences;

import static android.Manifest.permission.SEND_SMS;

/**
 * Created by mp_13 on 2/6/2017.
 */
public class KeyGeneratorActivity extends Activity {

        private TextView generated;

        private ProgressBar progress;

        private TextView privateKey;

        private View completedContainer;

        private TextView publicKey;

        private Button registerKeys;

        private static final int REQUEST_SEND_SMS = 12;

        private static final int REQUEST_READ_SMS = 13;

        private String myNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_generate_rsa);
        generated = (TextView) findViewById(R.id.generated);
        progress = (ProgressBar) findViewById(R.id.progress);
        privateKey = (TextView) findViewById(R.id.private_key);
        publicKey = (TextView) findViewById(R.id.public_key);
        completedContainer = findViewById(R.id.completed);
        registerKeys = (Button) findViewById(R.id.registerOnTheServer);
        registerKeys.setVisibility(View.GONE);
            if (Preferences.getString(Preferences.RSA_PRIVATE_KEY) != null) {
                showKeyPair();
            }
            findViewById(R.id.generate).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Preferences.clear();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    completedContainer.setVisibility(View.GONE);
                                    generated.setVisibility(View.GONE);
                                    publicKey.setVisibility(View.GONE);
                                    privateKey.setVisibility(View.GONE);
                                    progress.setVisibility(View.VISIBLE);
                                    registerKeys.setVisibility(View.GONE);
                                }
                            });
                            final long timeStarted = System.currentTimeMillis();
                            KeyPair keyPair = RSA.generate();
                            Crypto.writePrivateKeyToPreferences(keyPair);
                            Crypto.writePublicKeyToPreferences(keyPair);
                            final long timeFinished = System.currentTimeMillis();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    generated.setText(getString(R.string.rsa_key_generated) + " in " + (timeFinished - timeStarted) + " ms");
                                    showKeyPair();
                                }
                            });

                        }
                    }).start();
                }
            });
            registerKeys.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progress.setVisibility(View.VISIBLE);
                                }
                            });
                            try {
                                SftpProtocol ftpObj = new SftpProtocol("188.24.40.250", 990, "admin", "admin");

                                mayRequestNumber();
                                //// TODO: 2/11/2017  Inlocuire numar cu numarul telefonului.
                                //File keyFile = generateFileOnSD(myNumber,keyPair.getPublic().getEncoded().toString());

                                int permissionCheck = ContextCompat.checkSelfPermission(KeyGeneratorActivity.this,
                                        Manifest.permission.READ_SMS);
                                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(KeyGeneratorActivity.this,
                                            new String[]{Manifest.permission.READ_SMS},
                                            REQUEST_READ_SMS); // define this constant yourself
                                } else {
                                    // you have the permission
                                }

                                TelephonyManager tMgr = (TelephonyManager) KeyGeneratorActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
                                String mPhoneNumber = tMgr.getLine1Number();
                                File keyFile = generateFileOnSD(mPhoneNumber, Preferences.getString(Preferences.RSA_PUBLIC_KEY));

                                ftpObj.uploadFTPFile(keyFile.getPath(),mPhoneNumber +".txt", "/");
                                // ftpObj.uploadFTPFile(keyFile.getPath(),"0727000671.txt", "/");
                                //  ftpobj.downloadFTPFile("Shruti.txt", "/users/shruti/Shruti.txt");
                                //  System.out.println("FTP File downloaded successfully");
                                //  boolean result = ftpobj.listFTPFiles("/users/shruti", "shruti.txt");
                                //  System.out.println(result);
                                   ftpObj.disconnect();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progress.setVisibility(View.GONE);
                                }
                            });
                            Intent intent = new Intent(KeyGeneratorActivity.this, SmsMainActivity.class);
                            startActivity(intent);
                        }

                    }).start();


                }
            });

        }

        public void showKeyPair() {
            progress.setVisibility(View.GONE);
            generated.setVisibility(View.VISIBLE);
            completedContainer.setVisibility(View.VISIBLE);
            publicKey.setVisibility(View.VISIBLE);
            privateKey.setVisibility(View.VISIBLE);
            privateKey.setText(Crypto.stripPrivateKeyHeaders(Preferences.getString(Preferences.RSA_PRIVATE_KEY)));
            publicKey.setText(Crypto.stripPublicKeyHeaders(Preferences.getString(Preferences.RSA_PUBLIC_KEY)));
            registerKeys.setVisibility(View.VISIBLE);

        }

    public File generateFileOnSD(String sFileName, String sBody) {

        File gpxFile = null;
        try {
            File root = new File(getApplicationContext().getFilesDir(), "Keys");
            if (!root.exists()) {
                root.mkdirs();
            }

            gpxFile = new File(root, sFileName + ".txt");
            gpxFile.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(gpxFile);
            outputStream.write(sBody.getBytes());
            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return gpxFile;
    }

    public String getMyPhoneNumber()
    {
        return ((TelephonyManager) getSystemService(TELEPHONY_SERVICE))
                .getLine1Number();
    }

    private boolean mayRequestNumber() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(SEND_SMS)) {
            Snackbar.make(findViewById(R.id.mainLayout), R.string.permission_rationale, Snackbar.LENGTH_LONG)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{SEND_SMS}, REQUEST_SEND_SMS);
                        }
                    });
        } else {
            requestPermissions(new String[]{SEND_SMS}, REQUEST_SEND_SMS);
        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        switch(requestCode){
            case REQUEST_SEND_SMS:
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    myNumber = getMyPhoneNumber();
                }
                break;
            case REQUEST_READ_SMS:
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    myNumber = getMyPhoneNumber();
                }
        }
    }







}
