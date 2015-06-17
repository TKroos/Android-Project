package edu.stevens.cs522.chat.oneway.app.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import edu.stevens.cs522.chat.oneway.app.fragments.PeerDetail;
import edu.stevens.cs522.chat.oneway.app.fragments.PeerList;
import edu.stevens.cs522.chat.oneway.app.services.SendMessageService;
import edu.stevens.cs522.chat.oneway.client.R;

/**
 * Created by Xiang on 2015/4/18.
 */
public class Peers extends Activity implements PeerList.OnPeerSelectedListener{
    FragmentManager fm = getFragmentManager();
    FragmentTransaction ft = fm.beginTransaction();
    private String senderName = "";
    public static String chatRoom;
    private String message;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.peer_list);
        int windowHeight = this.getResources().getDisplayMetrics().heightPixels; //获取当前屏幕的高
        int windowWidth = this.getResources().getDisplayMetrics().widthPixels; //获取当前屏幕的宽

        PeerList f1 = new PeerList();
        PeerDetail f2 = new PeerDetail();
        f2.senderName = "";
        ft.replace(R.id.peer_list_container, f1);
        if (windowWidth > windowHeight) {          //横屏
            //ft.addToBackStack(null);
            ft.replace(R.id.peer_message_container, f2);
            //ft.addToBackStack(null);
        }
        ft.commit();
    }
    public void onPeerSelected(String sender) {
        // The user selected the headline of an article from the HeadlinesFragment
        // Do something here to display that article
        Configuration mConfiguration = this.getResources().getConfiguration(); //获取设置的配置信息
        int ori = mConfiguration.orientation ; //获取屏幕方向
        senderName = sender;

        if (ori == mConfiguration.ORIENTATION_LANDSCAPE) {
            // If article frag is available, we're in two-pane layout...
            PeerDetail articleFrag = (PeerDetail) getFragmentManager().findFragmentById(R.id.peer_message_container);
            // Call a method in the ArticleFragment to update its content
            articleFrag.updatePeerView(sender);
        } else {
            // Otherwise, we're in the one-pane layout and must swap frags...

            // Create fragment and give it an argument for the selected article
            PeerDetail newFragment = new PeerDetail();
            Bundle args = new Bundle();
            args.putString(PeerDetail.ARG_POSITION, sender);
            newFragment.setArguments(args);

            FragmentTransaction transaction = getFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.peer_list_container, newFragment);
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
            PeerDetail articleFrag = (PeerDetail) getFragmentManager().findFragmentById(R.id.peer_message_container);
            if ((ori == mConfiguration.ORIENTATION_LANDSCAPE) && (articleFrag.senderName != "")) {
                articleFrag.updatePeerView("");
            }
            else if ((ori == mConfiguration.ORIENTATION_LANDSCAPE) && (articleFrag.senderName == "")){
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
        inflater.inflate(R.menu.peer_list, menu);
        return super.onCreateOptionsMenu(menu);
    }
    public boolean onOptionsItemSelected(MenuItem item) {


        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.peer_list:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                // Get the layout inflater
                LayoutInflater inflater = getLayoutInflater();
                final View layout = inflater.inflate(R.layout.send_message_dialogue, null);
                // Inflate and set the layout for the dialog
                // Pass null as the parent view because its going in the dialog layout
                builder.setView(layout)
                        .setPositiveButton("SEND", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                EditText messageText =(EditText)layout.findViewById(R.id.message_to_send);
                                EditText chatroom =(EditText)layout.findViewById(R.id.chatroom);
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

                /*final EditText et = new EditText(this);

                new AlertDialog.Builder(this).setTitle("Send A Message")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setView(et)
                        .setPositiveButton("SEND", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String input = et.getText().toString();
                                Intent sendMessageIntent = new Intent(getApplicationContext(), SendMessageService.class);
                                sendMessageIntent.putExtra("message", input);
                                startService(sendMessageIntent);
                            }
                        })
                        .setNegativeButton("CANCEL", null)
                        .show();*/
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
