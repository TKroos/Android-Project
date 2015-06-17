package edu.stevens.cs522.chat.oneway.app.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.view.View.OnClickListener;
import java.util.UUID;

import edu.stevens.cs522.chat.oneway.app.adapter.ChatAdapter;
import edu.stevens.cs522.chat.oneway.app.provider.ChatProvider;
import edu.stevens.cs522.chat.oneway.app.services.RegisterService;
import edu.stevens.cs522.chat.oneway.client.R;

/**
 * Created by Xiang on 2015/4/18.
 */
public class DisplaySelection extends Activity{
    public static final String CLIENT_NAME_KEY = "client_name";
    public static final String SERVER_URI = "server_uri";
    public static final String DEFAULT_CLIENT_NAME = "client";
    public static String clientName;
    public static String serverUri;
    public static String uri;
    public static String username_regid;
    public static String regid_username;
    public static UUID uuid  =  null;
    public static final String DEFAULT_SERVER_URI = "http://localhost:8080/chat";
    private Button peer;
    private Button chatroom;
    private Button clear;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_selection);


        Intent callingIntent = getIntent();
        if (callingIntent != null && callingIntent.getExtras() != null) {

            clientName = callingIntent.getExtras().getString(CLIENT_NAME_KEY, DEFAULT_CLIENT_NAME);
            serverUri = callingIntent.getExtras().getString(SERVER_URI);

        } else {

            clientName = DEFAULT_CLIENT_NAME;
            serverUri = DEFAULT_SERVER_URI;
        }
        regid_username = "regid=" + uuid + "&username=" + clientName;
        username_regid = "username=" + clientName + "&regid=" + uuid;
        uri = serverUri + "?" + username_regid;
        /**
         * Let's be clear, this is a HACK to allow you to do network communication on the main thread.
         * This WILL cause an ANR, and is only provided to simplify the pedagogy.  We will see how to do
         * this right in a future assignment (using a Service managing background threads).
         */
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        peer = (Button)findViewById(R.id.peers);
        peer.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Peers.class);
                startActivity(intent);
            }
        });
        chatroom = (Button)findViewById(R.id.chatrooms);
        chatroom.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ChatRooms.class);
                startActivity(intent);
            }
        });
        clear = (Button)findViewById(R.id.clear);
        clear.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                ContentResolver contentResolver = getContentResolver();
                contentResolver.delete(ChatProvider.MESSAGE_TABLE_URI, null, null);
                new AlertDialog.Builder(DisplaySelection.this)
                        .setTitle("Information")
                        .setMessage("Database Cleared!")
                        .setPositiveButton("CONFIRM", null)
                        .setNegativeButton("CANCEL", null)
                        .show();
            }
        });
        //lm.restartLoader(0, null, ChatApp.this);

    }
}
