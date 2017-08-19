package com.lei.mykha.chatapp.activities.recyclerview;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lei.mykha.chatapp.R;
import com.lei.mykha.chatapp.activities.data.DatabaseProvider;

import java.util.ArrayList;
import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder>{
    String TAG = "ConversationAdapter";

    final private ListItemClickListener mOnClickListener;


    private Cursor mCursor;
    List<String> cursorColumns = new ArrayList<>();
    public ConversationAdapter(List<String> columns, ListItemClickListener clickListener) {
        this.mOnClickListener = clickListener;
        cursorColumns = columns;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public ConversationViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view;
        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.conversation_item, viewGroup, false);

        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ConversationViewHolder personViewHolder, int position) {
        mCursor.moveToPosition(position);
        personViewHolder.conversationTitle.setText(mCursor.getString(cursorColumns.indexOf("name")));

    }
    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    public interface ListItemClickListener{
        void onClick(String clickedConversation, String userId);
    }


    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }
    public class ConversationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{


        Context context;
        TextView conversationTitle;

        ConversationViewHolder(View view) {
            super(view);
            conversationTitle = (TextView)itemView.findViewById(R.id.tx_conversation_title);
            context = itemView.getContext();
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            mOnClickListener.onClick(mCursor.getString(cursorColumns.indexOf("uid")), mCursor.getString(cursorColumns.indexOf("user")));
        }
    }
}