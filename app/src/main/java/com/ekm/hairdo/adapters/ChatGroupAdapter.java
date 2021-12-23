package com.ekm.hairdo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.ekm.hairdo.R;
import com.ekm.hairdo.listener.UsergroupListener;
import com.ekm.hairdo.things.ChatDetails;
import com.ekm.hairdo.vars;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ChatGroupAdapter extends RecyclerView.Adapter<ChatGroupAdapter.ViewHolder> {
    private final String uid;
    private ArrayList<ChatDetails> mDataset;
    private UsergroupListener mListener;
    private int message_layout;
    private String myUID;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView message_text_view;
        public TextView user_text_view;
        public TextView user_time;
        public ConstraintLayout mView;
        ImageView photoImageView;
        public ViewHolder(View v) {
            super(v);
            message_text_view = v.findViewById(R.id.last_message);
            user_text_view = v.findViewById(R.id.profile_name);
            photoImageView = v.findViewById(R.id.profilepic);
            user_time = v.findViewById(R.id.time);
            mView = v.findViewById(R.id.group);
            //
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ChatGroupAdapter(ArrayList<ChatDetails> myDataset, int messagelayout, String uid, UsergroupListener mListener, String myUID) {
        mDataset = myDataset;
        message_layout = messagelayout;
        this.uid = uid;
        this.mListener = mListener;
        this.myUID = myUID;
    }

    @Override
    public int getItemViewType(int position) {
                return R.layout.user_group;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        View v =  LayoutInflater.from(parent.getContext())
                .inflate(viewType, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        Date mDate = new Date();
        mDate.setTime(mDataset.get(position).getLast_openned_time());
        ;

            holder.message_text_view.setText(mDataset.get(position).getLast_message());
            holder.user_time.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(mDate));

            Glide.with(holder.photoImageView.getContext())
                    .load(mDataset.get(position).getPerson1url())
                    .into(holder.photoImageView);

            // find my iud position in persons
        int me = mDataset.get(position).getPersons().indexOf(myUID);
        System.out.println("My UID Position is "+me+" for IUD="+myUID);
        //set text to personname position 0 if me =1, or 1 is me = 0;
        switch (me) {
            case 0:
                holder.user_text_view.setText(mDataset.get(position).getPersonsNames().get(vars.getHisUIDPosition(uid, mDataset.get(position).getPersons().get(1))));
                break;
            case 1:
                holder.user_text_view.setText(mDataset.get(position).getPersonsNames().get(vars.getHisUIDPosition(uid, mDataset.get(position).getPersons().get(0))));
                break;
        }

        holder.mView.setOnClickListener(view -> {
            mListener.onChatClicked(mDataset.get(position));
        });

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}