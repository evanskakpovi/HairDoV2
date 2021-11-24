package com.ekm.hairdo.listener;

import com.ekm.hairdo.things.Stack;

public interface CustomStackAdapterListener {
    // you can define any parameter as per your requirement
    void onRewind();

    void onChatButtonClicked(Stack currentStack);

    void addFavorite(boolean isChecked, String hairid);
}