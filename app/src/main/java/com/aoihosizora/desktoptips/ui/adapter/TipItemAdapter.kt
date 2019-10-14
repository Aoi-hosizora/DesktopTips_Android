package com.aoihosizora.desktoptips.ui.adapter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aoihosizora.desktoptips.R
import com.aoihosizora.desktoptips.model.TipItem
import kotlinx.android.synthetic.main.view_tips_adapter.view.*

class TipItemAdapter(

    private val context: Context,
    private val tipItems: List<TipItem>,

    private val onItemClick: (View, TipItem) -> Unit,
    private val onItemLongClick: (View, TipItem) -> Boolean

) : RecyclerView.Adapter<TipItemAdapter.ViewHolder>(), View.OnClickListener, View.OnLongClickListener {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_tips_adapter, parent, false)

        view.setOnClickListener(this)
        view.setOnLongClickListener(this)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tip = tipItems[position]
        holder.view.tag = tip

        holder.view.txt_content.text = tip.content
        if (tip.highLight)
            holder.view.txt_content.setTextColor(ContextCompat.getColor(context, R.color.highlight_color))
        else
            holder.view.txt_content.setTextColor(ContextCompat.getColor(context, R.color.text_color))
    }

    override fun getItemCount(): Int = tipItems.size

    override fun onClick(view: View) {
        onItemClick(view, view.tag as TipItem)
    }

    override fun onLongClick(view: View): Boolean {
        return onItemLongClick(view, view.tag as TipItem)
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}