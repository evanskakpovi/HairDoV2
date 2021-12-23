package com.ekm.hairdo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ekm.hairdo.R;
import com.ekm.hairdo.things.Message;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private final String uid;
    private ArrayList<Message> mDataset;
    private int message_layout;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView message_text_view;
        public TextView user_text_view;
        ImageView photoImageView;
        public ViewHolder(View v) {
            super(v);
            message_text_view = v.findViewById(R.id.msg_view);
            user_text_view = v.findViewById(R.id.user_view);
            photoImageView = v.findViewById(R.id.imageView);
            //
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ChatAdapter(ArrayList<Message> myDataset, int messagelayout, String uid) {
        mDataset = myDataset;
        message_layout = messagelayout;
        this.uid = uid;
    }

    @Override
    public int getItemViewType(int position) {
        boolean textItem = (mDataset.get(position).getUrl_message().equals("none"));
     //   System.out.println(mDataset.get(position).getUrl_message()+" --- " +textItem + " --- "+uid);
        if (mDataset.get(position).getKey().equals(uid)){
            if (textItem){
                return R.layout.msg_item_me;}
            else {
                //photo layout
                return R.layout.msg_item_photo_me;
            }
        } else {
            if (textItem){
                return R.layout.msg_item;}
            else {
                //photo layout
                return R.layout.msg_item_photo;
            }
        }
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
        boolean textItem = (mDataset.get(position).getUrl_message().equals("none"));
        if (textItem) {
            holder.message_text_view.setText(mDataset.get(position).getText());
        } else {
            Glide.with(holder.photoImageView.getContext())
                    .load(mDataset.get(position).getUrl_message())
                    .into(holder.photoImageView);
        }
        holder.user_text_view.setText(mDataset.get(position).getName());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}