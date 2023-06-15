package com.example.fap.ui.history

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.fap.R
import com.example.fap.ui.dialogs.AddPayment
import com.example.fap.utils.SharedCurrencyManager

class HistoryAdapter(private val historyList: List<HistoryItem>) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private lateinit var sharedCurrency: SharedCurrencyManager
    private var colorRed = 0
    private var colorGreen = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.history_item, parent, false)

        sharedCurrency = SharedCurrencyManager.getInstance(context)
        colorRed = ContextCompat.getColor(context, R.color.dark_red)
        colorGreen = ContextCompat.getColor(context, R.color.dark_green)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return historyList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val historyItem = historyList[position]
        holder.bind(historyItem)
        val context = holder.itemView.context

        holder.itemView.setOnClickListener {
            val intent = Intent(context, AddPayment::class.java)
            intent.putExtra("paymentId", historyItem.id)
            context.startActivity(intent)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textTitle: TextView = itemView.findViewById(R.id.history_title)
        private val textCategory: TextView = itemView.findViewById(R.id.history_category)
        private val textPrice: TextView = itemView.findViewById(R.id.history_price)

        fun bind(historyItem: HistoryItem) {
            textTitle.text = historyItem.title
            textCategory.text = historyItem.category
            textPrice.text = sharedCurrency.num2Money(historyItem.price)

            if (historyItem.isPayment) {
                textPrice.setTextColor(colorRed)
            } else {
                textPrice.setTextColor(colorGreen)
            }
        }
    }
}