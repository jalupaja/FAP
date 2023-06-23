package com.example.fap.ui.slideshow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.fap.R
import com.example.fap.ui.history.HistoryAdapter
import com.example.fap.utils.SharedCurrencyManager

class CategoryAdapter(
    private val categoryList: List<CategoryItem>/*,
    private val onItemClick: (CategoryItem) -> Unit*/
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {


    private lateinit var sharedCurrency: SharedCurrencyManager
    private var colorRed = 0
    private var colorGreen = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryAdapter.ViewHolder {
        val context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.category_item, parent, false)
        sharedCurrency = SharedCurrencyManager.getInstance(context)
        colorRed = ContextCompat.getColor(context, R.color.dark_red)
        colorGreen = ContextCompat.getColor(context, R.color.dark_green)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = categoryList[position]
        holder.bind(item)

        /*holder.itemView.setOnClickListener {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val item = categoryList[position]
                //onItemClick(item)
            }
        }*/
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.category_title)
        private val sumTextView: TextView = itemView.findViewById(R.id.category_sum)

        fun bind(item: CategoryItem) {
            titleTextView.text = item.title
            sumTextView.text = item.sum.toString()
            val sum = sumTextView.text.toString()

            if (sum.toDouble() > 0.0) {
                sumTextView.setTextColor(colorGreen)
            } else if (sum.toDouble() < 0.0) {
                sumTextView.setTextColor(colorRed)
            }
        }

    }
}
