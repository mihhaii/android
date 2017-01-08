package mihhaii.smsencryption;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mp_13 on 1/8/2017.
 */
public class SmsMainActivity extends Activity{

    private AutoCompleteTextView contactsTextView;
    private ArrayList<Map<String,String>> listOfContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_main);

        listOfContacts = getAllContacts();
        contactsTextView = (AutoCompleteTextView) findViewById(R.id.toText);
        contactsTextView.setAdapter(new SimpleAdapter(getApplicationContext(),listOfContacts, R.layout.contacts_layout,
                new String[] { "Name", "Phone"}, new int[] {
                R.id.contactName, R.id.contactPhone }));

        contactsTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> av, View arg1, int index,
                                    long arg3) {
                Map<String, String> map = (Map<String, String>) av.getItemAtPosition(index);

                String name = map.get("Name");
                String number = map.get("Phone");
                contactsTextView.setText("" + name + "<" + number + ">");

            }


        });
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
}
