package edu.stevens.cs522.chat.oneway.app.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import edu.stevens.cs522.chat.oneway.app.adapter.ChatAdapter;
import edu.stevens.cs522.chat.oneway.app.provider.ChatProvider;
import edu.stevens.cs522.chat.oneway.client.R;

/**
 * Created by Xiang on 2015/4/18.
 */
public class PeerDetail extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static String ARG_POSITION = "position";
    private LoaderManager lm;
    private CursorAdapter adapter;
    public static String senderName = "";
    private ReceiveBroadCast receiveBroadCast;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        adapter = new ChatAdapter(getActivity(), null);
        setListAdapter(adapter);
        lm = getLoaderManager();
        lm.initLoader(0, null, this);
        return inflater.inflate(R.layout.peer_detail_fragment, container, false);
    }
    public void updatePeerView(String sender){
        senderName = sender;
        lm.restartLoader(0, null, this);
    }
    public void onAttach(Activity activity) {
        receiveBroadCast = new ReceiveBroadCast();
        IntentFilter filter = new IntentFilter();
        filter.addAction("Chat App");    //只有持有相同的action的接受者才能接收此广播
        activity.registerReceiver(receiveBroadCast, filter);
        super.onAttach(activity);
    }
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
        if (getArguments() == null) return new CursorLoader(getActivity(), ChatProvider.MESSAGE_TABLE_URI, null, ChatProvider.SENDER + "=? and " + ChatProvider.MESSAGE + "!=?", new String[]{senderName, " "}, null);
        else return new CursorLoader(getActivity(), ChatProvider.MESSAGE_TABLE_URI, null, ChatProvider.SENDER + "=? and " + ChatProvider.MESSAGE + "!=?", new String[]{getArguments().getString(ARG_POSITION), " "}, null);
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
                lm.restartLoader(0, null, PeerDetail.this);
            }
            else if (intent.getStringExtra("receive_or_send").equals("register_failed")){
                Toast.makeText(arg0, "Registration failed", Toast.LENGTH_SHORT).show();
            }
            else if (intent.getStringExtra("receive_or_send").equals("register_success")){
                Toast.makeText(arg0, "Register Successfully", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onDestroyView() {
        getActivity().unregisterReceiver(receiveBroadCast);
        super.onDestroyView();
    }
}
