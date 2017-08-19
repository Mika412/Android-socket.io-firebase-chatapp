package com.lei.mykha.chatapp.activities.recyclerview;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lei.mykha.chatapp.R;
import com.lei.mykha.chatapp.activities.utilities.JsonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mykha on 31/05/2017.
 */

public class MessagesAdapter<U, V extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    String TAG = "ConversationAdapter";

    private String userID;
    private Cursor mCursor;

    List<String> cursorColumns = new ArrayList<>();

    public MessagesAdapter(String UID, List<String> columns) {
        this.userID = UID;
        cursorColumns = columns;
    }

    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
        View view;

        switch (type) {
            case 0:
                return new MessagesAdapter.RightViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_message_right, viewGroup, false));
            case 1:
                return new MessagesAdapter.LeftViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_message_left, viewGroup, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        if(mCursor.getString(1).equals(userID)) {
            MessagesAdapter.RightViewHolder viewHolder = (MessagesAdapter.RightViewHolder) holder;

            viewHolder.timestamp.setText(JsonUtils.getFormattedDate(mCursor.getString(cursorColumns.indexOf("timestamp"))));
            viewHolder.message.setText(mCursor.getString(cursorColumns.indexOf("message")));
        }else{
            MessagesAdapter.LeftViewHolder viewHolder = (MessagesAdapter.LeftViewHolder) holder;

            viewHolder.timestamp.setText(JsonUtils.getFormattedDate(mCursor.getString(cursorColumns.indexOf("timestamp"))));
            viewHolder.message.setText(mCursor.getString(cursorColumns.indexOf("message")));
        }
    }

    @Override
    public int getItemViewType(int position) {
        mCursor.moveToPosition(position);
        if(mCursor.getString(1).equals(userID))
            return 0;
        else
            return 1;
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }
    public class LeftViewHolder extends RecyclerView.ViewHolder{
        Context context;
        TextView timestamp;
        TextView message;

        LeftViewHolder(View view) {
            super(view);
            timestamp = (TextView)itemView.findViewById(R.id.lblMsgFrom);
            message = (TextView)itemView.findViewById(R.id.txtMsg);
            context = itemView.getContext();
        }
    }

    public class RightViewHolder extends RecyclerView.ViewHolder{
        Context context;
        TextView timestamp;
        TextView message;

        RightViewHolder(View view) {
            super(view);
            timestamp = (TextView)itemView.findViewById(R.id.lblMsgFrom);
            message = (TextView)itemView.findViewById(R.id.txtMsg);
            context = itemView.getContext();
        }
    }
}