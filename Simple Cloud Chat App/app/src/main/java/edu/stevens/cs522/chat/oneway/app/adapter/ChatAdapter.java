package edu.stevens.cs522.chat.oneway.app.adapter;

/**
 * Created by Xiang on 2015/3/5.
 */
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import edu.stevens.cs522.chat.oneway.app.provider.ChatProvider;

public class ChatAdapter extends ResourceCursorAdapter {
    protected final static int ROW_LAYOUT = android.R.layout.simple_list_item_1;

    public ChatAdapter(Context context, Cursor cursor) {
        super(context, ROW_LAYOUT, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cur, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(ROW_LAYOUT, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //view = newView(context, cursor, null);
        TextView messageLine = (TextView) view.findViewById(android.R.id.text1);
        String sender = cursor.getString(cursor.getColumnIndexOrThrow(ChatProvider.SENDER));
        String message = cursor.getString(cursor.getColumnIndexOrThrow(ChatProvider.MESSAGE));
        String date = cursor.getString(cursor.getColumnIndexOrThrow(ChatProvider.DATE));
        String latitude = cursor.getString(cursor.getColumnIndexOrThrow(ChatProvider.LATITUDE));
        String longitude = cursor.getString(cursor.getColumnIndexOrThrow(ChatProvider.LONGITUDE));
        String address = cursor.getString(cursor.getColumnIndexOrThrow(ChatProvider.ADDRESS));
        messageLine.setText(sender + ": " + message + "\n" + date + "\n" + latitude + ", " + longitude + "\n" + address);
    }
}