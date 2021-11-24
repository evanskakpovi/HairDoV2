package com.ekm.hairdo.listener;

import com.ekm.hairdo.things.Stack;

public interface CustomStackDesignAdapterListener {
    // you can define any parameter as per your requirement

    void onEmpty();
    void onNotEmpty();
    void onRewind();

    void onChatButtonClicked(Stack currentStack);

    void addFavorite(boolean isChecked, String hairid);
}