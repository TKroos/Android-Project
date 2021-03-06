package edu.stevens.cs522.chat.oneway.app.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import edu.stevens.cs522.chat.oneway.app.fragments.ChatRoomDetail;
import edu.stevens.cs522.chat.oneway.app.fragments.ChatRoomList;
import edu.stevens.cs522.chat.oneway.app.fragments.PeerDetail;
import edu.stevens.cs522.chat.oneway.app.provider.ChatProvider;
import edu.stevens.cs522.chat.oneway.app.services.SendMessageService;
import edu.stevens.cs522.chat.oneway.client.R;

/**
 * Created by Xiang on 2015/4/18.
 */
public class ChatRooms extends Activity implements ChatRoomList.OnChatRoomSelectedListener{
    public static String chatRoom;
    private String message;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatroom_list);
        int windowHeight = this.getResources().getDisplayMetrics().heightPixels; //获取当前屏幕的高
        int windowWidth = this.getResources().getDisplayMetrics().widthPixels; //获取当前屏幕的宽

        ChatRoomList f1 = new ChatRoomList();
        ChatRoomDetail f2 = new ChatRoomDetail();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        f2.chatRoom = "";
        ft.replace(R.id.chatroom_list_container, f1);
        if (windowWidth > windowHeight) {          //横屏
            ft.add(R.id.chatroom_message_container, f2);
        }
        ft.commit();
    }
    public void onChatRoomSelected(String chatroom) {
        // The user selected the headline of an article from the HeadlinesFragment
        // Do something here to display that article
        Configuration mConfiguration = this.getResources().getConfiguration(); //获取设置的配置信息
        int ori = mConfiguration.orientation ; //获取屏幕方向
        chatRoom = chatroom;

        if (ori == mConfiguration.ORIENTATION_LANDSCAPE) {
            // If article frag is available, we're in two-pane layout...
            ChatRoomDetail articleFrag = (ChatRoomDetail) getFragmentManager().findFragmentById(R.id.chatroom_message_container);
            // Call a method in the ArticleFragment to update its content
            articleFrag.updatePeerView(chatroom);
        } else {
            // Otherwise, we're in the one-pane layout and must swap frags...

            // Create fragment and give it an argument for the selected article
            ChatRoomDetail newFragment = new ChatRoomDetail();
            Bundle args = new Bundle();
            args.putString(PeerDetail.ARG_POSITION, chatroom);
            newFragment.setArguments(args);

            FragmentTransaction transaction = getFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.chatroom_list_container, newFragment);
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();
        }
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            //do something...
            Configuration mConfiguration = this.getResources().getConfiguration(); //获取设置的配置信息
            int ori = mConfiguration.orientation ; //获取屏幕方向
            ChatRoomDetail articleFrag = (ChatRoomDetail) getFragmentManager().findFragmentById(R.id.chatroom_message_container);
            if ((ori == mConfiguration.ORIENTATION_LANDSCAPE) && (articleFrag.chatRoom != "")) {
                articleFrag.updatePeerView("");
            }
            else if ((ori == mConfiguration.ORIENTATION_LANDSCAPE) && (articleFrag.chatRoom == "")){
                startActivity(new Intent(this, DisplaySelection.class));
            }
            else{
                return super.onKeyDown(keyCode, event);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chatroom_list, menu);
        return super.onCreateOptionsMenu(menu);
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.send_message:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                // Get the layout inflater
                LayoutInflater inflater = getLayoutInflater();
                final View layout = inflater.inflate(R.layout.send_message_dialogue, null);
                final EditText messageText =(EditText)layout.findViewById(R.id.message_to_send);
                final EditText chatroom =(EditText)layout.findViewById(R.id.chatroom);
                chatroom.setText(ChatRoomDetail.chatRoom);
                // Inflate and set the layout for the dialog
                // Pass null as the parent view because its going in the dialog layout
                builder.setView(layout)
                        .setPositiveButton("SEND", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                message = messageText.getText().toString();
                                chatRoom = chatroom.getText().toString();
                                Intent sendMessageIntent = new Intent(getApplicationContext(), SendMessageService.class);
                                sendMessageIntent.putExtra("message", message);
                                sendMessageIntent.putExtra("chatroom", chatRoom);
                                startService(sendMessageIntent);
                            }
                        })
                        .setNegativeButton("CANCEL", null)
                        .show();
                return true;
            case R.id.add_chatroom:
                final EditText editText = new EditText(this);
                new AlertDialog.Builder(this).setTitle("Create a new Chat Room")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setView(editText)
                        .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                chatRoom = editText.getText().toString();
                                Cursor cursor = getContentResolver().query(ChatProvider.MESSAGE_TABLE_URI, null, ChatProvider.CHATROOM + "=?", new String[]{chatRoom}, null);
                                if (cursor.moveToFirst() == true) {
                                    new AlertDialog.Builder(ChatRooms.this)
                                            .setTitle("Create Failed")
                                            .setMessage("The Chat Room already existed!")
                                            .setPositiveButton("CONFIRM", null)
                                            .setNegativeButton("CANCEL", null)
                                            .show();
                                } else {
                                    ContentValues contentValues = new ContentValues();
                                    writeToProvider(contentValues, " ", chatRoom);
                                    getContentResolver().insert(ChatProvider.MESSAGE_TABLE_URI, contentValues);
                                    Intent i = new Intent("Chat App");
                                    i.putExtra("receive_or_send", "receive");
                                    sendBroadcast(i);
                                }
                            }
                        })
                        .setNegativeButton("CANCEL", null)
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    void writeToProvider(ContentValues contentValues, String message, String chatroom) {
        contentValues.put(ChatProvider._ID, (byte[]) null);
        contentValues.put(ChatProvider.MESSAGE, message);
        contentValues.put(ChatProvider.CHATROOM, chatroom);
        contentValues.put(ChatProvider.MESSAGE_ID, 1);
        contentValues.put(ChatProvider.SENDER, "");
        contentValues.put(ChatProvider.SENDER_ID, 1);
        contentValues.put(ChatProvider.DATE, "");
        contentValues.put(ChatProvider.LATITUDE, 0.0);
        contentValues.put(ChatProvider.LONGITUDE, 0.0);
        contentValues.put(ChatProvider.ADDRESS, "No address!");
    }
}
