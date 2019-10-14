package com.aoihosizora.desktoptips.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.aoihosizora.desktoptips.R
import com.aoihosizora.desktoptips.model.TipItem
import kotlinx.android.synthetic.main.view_tips_adapter.view.*

class TipItemAdapter(
    private val tipItems: List<TipItem>,
    private val onItemClick: (View, TipItem) -> Unit,
    private val onItemLongClick: (View, TipItem) -> Boolean
) : RecyclerView.Adapter<TipItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.view_tips_adapter, parent, false
            ),
            onItemClick,
            onItemLongClick
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.view.tag = tipItems[position]
        holder.view.txt_content.text = tipItems[position].content
    }

    override fun getItemCount(): Int = tipItems.size

    class ViewHolder(
        val view: View,
        private val onItemClick: (View, TipItem) -> Unit,
        private val onLongItemClick: (View, TipItem) -> Boolean
    ) : RecyclerView.ViewHolder(view) {
        init {
            itemView.setOnClickListener { onItemClick(view, itemView.tag as TipItem) }
            itemView.setOnLongClickListener { onLongItemClick(view, itemView.tag as TipItem) }
        }
    }
}