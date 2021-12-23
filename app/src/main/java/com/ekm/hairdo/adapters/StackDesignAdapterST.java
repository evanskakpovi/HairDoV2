package com.ekm.hairdo.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ekm.hairdo.R;
import com.ekm.hairdo.listener.CustomStackDesignAdapterListener;
import com.ekm.hairdo.things.Stack;

import java.util.ArrayList;

public class StackDesignAdapterST extends RecyclerView.Adapter<StackDesignAdapterST.ViewHolder>{
    private ArrayList<Stack> mStacks;
    private CustomStackDesignAdapterListener mListener;
  // private AdapterCallback adapterCallback;
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder   {

        // each data item is just a string in this case..
        ImageView myLogo;

        public ViewHolder(View v) {
            super(v);
            // Declare objects
            myLogo = v.findViewById(R.id.item_image);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public StackDesignAdapterST(Context c, ArrayList<Stack> myDataset, CustomStackDesignAdapterListener ml) {
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
            mListener.onNotEmpty();
        return R.layout.stack_design_stdesign; }
        else {
            mListener.onEmpty();
            return R.layout.stack_design_stdesignempty;
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
            //holder.price.setText(position+" / "+mStacks.size());
            Glide.with(holder.myLogo.getContext())
                    .load(mStacks.get(position).getUrl())
                    .into(holder.myLogo);
        }
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        //Extra login to show empty view at 0
        if(mStacks.size() == 0){
            return 1;
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