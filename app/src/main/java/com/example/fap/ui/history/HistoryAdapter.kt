package com.example.fap.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fap.R

class HistoryAdapter(private val historyList: List<HistoryItem>) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.history_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return historyList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val historyItem = historyList[position]
        holder.bind(historyItem)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textTitle: TextView = itemView.findViewById(R.id.history_title)
        private val textCategory: TextView = itemView.findViewById(R.id.history_category)
        private val textPrice: TextView = itemView.findViewById(R.id.history_price)

        fun bind(historyItem: HistoryItem) {
            textTitle.text = historyItem.title
            textCategory.text = historyItem.category
            textPrice.text = historyItem.price.toString()
        }
    }
}