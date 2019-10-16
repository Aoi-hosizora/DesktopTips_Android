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
    private val onItemLongClick: (View, TipItem) -> Unit

) : RecyclerView.Adapter<TipItemAdapter.ViewHolder>(), View.OnClickListener, View.OnLongClickListener {

    var checkMode = false
        set(value) {
            field = value
            if (!value) checkItems.clear()
            notifyDataSetChanged()
        }

    private val checkItems: MutableList<TipItem> = mutableListOf()

    fun setItemChecked(tipItem: TipItem, isCheck: Boolean) {
        if (!isCheck)
            checkItems.remove(tipItem)
        else
            checkItems.add(tipItem)
    }

    fun getAllChecked(): MutableList<TipItem> = checkItems

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_tips_adapter, parent, false)

        view.setOnClickListener(this)
        view.setOnLongClickListener(this)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // holder.setIsRecyclable(false)

        val tipItem = tipItems[position]
        holder.view.tag = tipItem

        // Content
        holder.view.txt_content.text = tipItem.content
        if (tipItem.highLight)
            holder.view.txt_content.setTextColor(ContextCompat.getColor(context, R.color.highlight_color))
        else
            holder.view.txt_content.setTextColor(ContextCompat.getColor(context, R.color.text_color))

        // Check
        holder.view.check_box.setOnCheckedChangeListener(null)
        holder.view.check_box.isChecked = checkItems.indexOf(tipItem) != -1
        holder.view.check_box.visibility = if (checkMode) View.VISIBLE else View.GONE
        holder.view.check_box.setOnCheckedChangeListener { _ , checked -> run {
            if (checked)
                checkItems.add(tipItem)
            else
                checkItems.remove(tipItem)
        }}
    }

    override fun getItemCount(): Int = tipItems.size

    override fun onClick(view: View?) {
        if (!checkMode)
            view?.let { onItemClick(it, it.tag as TipItem) }
        else
            view?.check_box?.let {
                it.isChecked = !it.isChecked
            }
    }

    override fun onLongClick(view: View?): Boolean {
        if (!checkMode)
            view?.let { onItemLongClick(it, it.tag as TipItem) }
        return true
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}