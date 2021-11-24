package com.ekm.hairdo.things

import androidx.recyclerview.widget.DiffUtil


class StackDiffCallback(
        private val old: List<Stack>,
        private val new: List<Stack>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return old.size
    }

    override fun getNewListSize(): Int {
        return new.size
    }

    override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
        return old[oldPosition].hairid == new[newPosition].hairid
    }

    override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
        return old[oldPosition] == new[newPosition]
    }

}