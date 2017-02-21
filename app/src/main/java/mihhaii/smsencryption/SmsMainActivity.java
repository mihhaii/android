package mihhaii.smsencryption;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.apache.commons.net.util.Base64;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import crypto.Crypto;
import crypto.RSA;
import sftp.SftpProtocol;

import static android.Manifest.permission.READ_CONTACTS;
/**
 * Created by mp_13 on 1/8/2017.
 */
public class SmsMainActivity extends Activity{

    private AutoCompleteTextView contactsTextView;
    private ArrayList<Map<String,String>> listOfContacts;
    private static final int REQUEST_READ_CONTACTS = 13;
    private String phoneNumber = "0727000671";
    private File keyFile;
    private String publicKey;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_main);

        if (mayRequestContacts()){
            listOfContacts=getAllContacts();
        }

        contactsTextView = (AutoCompleteTextView) findViewById(R.id.toText);
        contactsTextView.setAdapter(new SimpleAdapter(getApplicationContext(), listOfContacts, R.layout.contacts_layout,
                new String[]{"Name", "Phone"}, new int[]{
                R.id.contactName, R.id.contactPhone}));

        contactsTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> av, View arg1, int index,
                                    long arg3) {
                Map<String, String> map = (Map<String, String>) av.getItemAtPosition(index);

                String name = map.get("Name");
                String number = map.get("Phone");
                contactsTextView.setText("" + name + "<" + number + ">");

                //TODO cautare cheie publica
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            SftpProtocol ftpObject = new SftpProtocol("188.24.40.250", 990, "admin", "admin");
                            //search for file
                            if(receiverHasKeyStored(ftpObject,phoneNumber)){
                                ftpObject.downloadFTPFile(phoneNumber + ".txt", getApplicationContext().getFilesDir() + "/Keys/" + phoneNumber + ".txt");
                                keyFile = new File(getApplicationContext().getFilesDir(), "Keys/" + phoneNumber + ".txt");
                                parseKey(keyFile);
                            }
                            ftpObject.disconnect();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }).start();
            }
        });
        Button send = (Button) findViewById(R.id.send_button);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText textEdit = (EditText) findViewById(R.id.smsTextField);
                if (!textEdit.getText().toString().isEmpty()) {

                    KeyPair keyPair = RSA.generate();

                    String encryptedText = RSA.encryptToBase64(getPublicKey(), textEdit.getText().toString());


                    sendSMS(encryptedText, "5556");
                }

            }
        });


    }

    private Key getPublicKey() {
        byte[] publicBytes = Base64.decodeBase64(publicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        KeyFactory keyFactory = null;
        PublicKey publicK = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
            publicK = keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return publicK;
    }

    private void parseKey(File keyFile) {

        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(keyFile));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        publicKey = Crypto.stripPublicKeyHeaders(text.toString());
    }



    private boolean receiverHasKeyStored(SftpProtocol ftp, String phoneNumber){
        try {
            return  ftp.listFTPFiles("/", phoneNumber + ".txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    void sendSMS(String text,String number)
    {
        try {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(number, null, text, null, null);
            Toast.makeText(getApplicationContext(),"SMS Sent",Toast.LENGTH_LONG).show();
        }catch (Exception e ){
            Toast.makeText(getApplicationContext(),"SMS Failed sending",Toast.LENGTH_LONG).show();
        }

// last two parameters in sendTextMessage method are PendingInten
// sentIntent & deliveryIntent.
    }


    /**
     * Get All the Contact Names
     * @return
     */
    private ArrayList<Map<String,String>> getAllContacts() {
        ArrayList<Map<String,String>> contactList = new ArrayList<Map<String,String>>();

        try {
            // Get all Contacts
            Cursor allPeople = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            if (allPeople != null) {
                while (allPeople.moveToNext()) {
                    // Add Contact's Name into the List
                    Map<String,String> person = new HashMap<String,String>();
                    person.put("Name",allPeople.getString(allPeople.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
                    person.put("Phone", allPeople.getString(allPeople.getColumnIndex(ContactsContract.Contacts._ID)));
                    contactList.add(person);

                }
            }
        } catch (NullPointerException e) {
            Log.e("getAllContactNames()", e.getMessage());
        }
        return contactList;
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            try {
                Snackbar.make(findViewById(R.id.mainLayout), R.string.permission_rationale, Snackbar.LENGTH_LONG)
                        .setAction(android.R.string.ok, new View.OnClickListener() {
                            @Override
                            @TargetApi(Build.VERSION_CODES.M)
                            public void onClick(View v) {
                                requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                            }
                        });
            }catch(Exception e){
                e.printStackTrace();
            }
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                listOfContacts = getAllContacts();
            }
        }
    }
}
