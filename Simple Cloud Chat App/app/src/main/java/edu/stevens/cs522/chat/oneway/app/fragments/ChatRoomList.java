package edu.stevens.cs522.chat.oneway.app.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import edu.stevens.cs522.chat.oneway.app.provider.ChatProvider;
import edu.stevens.cs522.chat.oneway.client.R;

/**
 * Created by Xiang on 2015/4/18.
 */
public class ChatRoomList extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    OnChatRoomSelectedListener mCallback;
    private ContentResolver contentResolver;
    private LoaderManager lm;
    private CursorAdapter adapter;
    private Cursor cursor;
    private ListView listView;
    private ReceiveBroadCast receiveBroadCast;
    public interface OnChatRoomSelectedListener {
        public void onChatRoomSelected(String chatroom);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        receiveBroadCast = new ReceiveBroadCast();
        IntentFilter filter = new IntentFilter();
        filter.addAction("Chat App");    //只有持有相同的action的接受者才能接收此广播
        getActivity().registerReceiver(receiveBroadCast, filter);
        adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null,new String[] {ChatProvider.CHATROOM}, new int[] {android.R.id.text1});
        setListAdapter(adapter);
        lm = getLoaderManager();
        lm.initLoader(0, null, this);
        contentResolver = getActivity().getContentResolver();
        return inflater.inflate(R.layout.chatroom_list_fragment, container, false);
    }

    public void onAttach(Activity activity) {
        /*receiveBroadCast = new ReceiveBroadCast();
        IntentFilter filter = new IntentFilter();
        filter.addAction("Chat App");    //只有持有相同的action的接受者才能接收此广播
        activity.registerReceiver(receiveBroadCast, filter);*/
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnChatRoomSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Send the event to the host activity
        cursor = contentResolver.query(Uri.withAppendedPath(ChatProvider.MESSAGE_TABLE_URI, String.valueOf(id)), null, ChatProvider._ID + "=?", new String[]{String.valueOf(id)}, null);
        cursor.moveToFirst();
        mCallback.onChatRoomSelected(cursor.getString(cursor.getColumnIndexOrThrow(ChatProvider.CHATROOM)));
    }
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
        return new CursorLoader(getActivity(), ChatProvider.MESSAGE_TABLE_URI, null, null, null, ChatProvider.CHATROOM);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor c){
        this.adapter.swapCursor(c);
    }

    public void onLoaderReset(Loader<Cursor> loader){
        this.adapter.swapCursor(null);
    }

    public void refresh(){
        lm.restartLoader(0, null, this);
    }

    public class ReceiveBroadCast extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (intent.getStringExtra("receive_or_send").equals("receive")){
                lm.restartLoader(0, null, ChatRoomList.this);
            }
            else if (intent.getStringExtra("receive_or_send").equals("register_failed")){
                Toast.makeText(arg0, "Registration failed", Toast.LENGTH_SHORT).show();
            }
            else if (intent.getStringExtra("receive_or_send").equals("register_success")){
                Toast.makeText(arg0, "Register Successfully", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onDestroyView() {
        getActivity().unregisterReceiver(receiveBroadCast);
        super.onDestroyView();
    }
}
