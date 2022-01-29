package com.ekm.hairdo.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.ekm.hairdo.listener.CustomStackAdapterListener;
import com.ekm.hairdo.things.Stack;
import com.ekm.hairdo.R;

import java.util.ArrayList;

public class StackAdapter extends RecyclerView.Adapter<StackAdapter.ViewHolder>{
    private ArrayList<Stack> mStacks;
    private CustomStackAdapterListener mListener;
  // private AdapterCallback adapterCallback;
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder   {

        // each data item is just a string in this case
        ImageView myLogo;
        TextView distance;
        TextView price;
        Button undo, go;
        ToggleButton favorite;

        public ViewHolder(View v) {
            super(v);
            // Declare objects
            myLogo = v.findViewById(R.id.item_image);
            distance = v.findViewById(R.id.item_distance);
            price = v.findViewById(R.id.item_price);
            undo = v.findViewById(R.id.item_undo);
            favorite = v.findViewById(R.id.item_star);
            go = v.findViewById(R.id.item_go);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public StackAdapter(Context c, ArrayList<Stack> myDataset, CustomStackAdapterListener ml) {
        mStacks = myDataset;
        mListener = ml;
//        try {
//            adapterCallback = ((AdapterCallback) c);
//        } catch (ClassCastException e) {
//            throw new ClassCastException("Activity must implement AdapterCallback.");
//        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mStacks.size()>0) {
        return R.layout.stack_design; }
        else { return R.layout.stack_design_empty;
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
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (mStacks.size() > 0) {
            //Hide rewind button on 1st card in the list.
            if (position==0){
                holder.undo.setVisibility(View.INVISIBLE);
            } else {
                //
                if (holder.undo.getVisibility() == View.INVISIBLE)
                holder.undo.setVisibility(View.VISIBLE);
            holder.undo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onRewind();
                }
            });
            }
            holder.price.setText(position+" / "+mStacks.size() + mStacks.get(position).getName());
            Glide.with(holder.myLogo.getContext())
                    .load(mStacks.get(position).getUrl())
                    .into(holder.myLogo);

            holder.go.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Launch new activity from CardActivity using listener
                    mListener.onChatButtonClicked(mStacks.get(position));
                }
            });
            holder.favorite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        // The toggle is enabled
                       // System.out.println("This exists");
                        mListener.addFavorite(isChecked, mStacks.get(position).getHairid());
                    } else {
                        // The toggle is disabled
                    //    System.out.println("This not exists");
                        mListener.addFavorite(isChecked, mStacks.get(position).getHairid());
                    }
                }
            });
            if (mStacks.get(position).getFav() && mStacks.get(position).getFavUpdated()) {
                holder.favorite.setVisibility(View.VISIBLE);
                holder.favorite.setChecked(true);
                System.out.println("visible "+position+" checked");
            } else   if (!mStacks.get(position).getFav() && mStacks.get(position).getFavUpdated()) {
                holder.favorite.setVisibility(View.VISIBLE);
                holder.favorite.setChecked(false);
                System.out.println("visible "+position+" unchecked");
            } else {
                holder.favorite.setChecked(false);
            }
        } else {
            Glide.with(holder.myLogo.getContext())
                    .load(R.drawable.lightbraid)
                    .into(holder.myLogo);
        }
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        //Extra login to show empty view at 0
        if(mStacks.size() == 0){
            return 500;
        }else {
           return mStacks.size();
        }
    }

    public ArrayList<Stack> getmStacks() {
        return mStacks;
    }

    public void setmStacks(ArrayList<Stack> mStacks) {
        this.mStacks = mStacks;
    }
//    private void GoToActivityAndFinish(Context v, Class c, StackGroup StackGroup) {
//        Intent myIntent = new Intent(v, c);
//      //  myIntent.putExtra(keys.styles, StackGroup); //Optional parameters
//        v.startActivity(myIntent);
////        ((Activity)v).finish();
//    }
//
//    public static interface AdapterCallback {
//        void onLoginCallBack();
//        void onCardRewindCallback();
//    }
}