package com.example.fap.ui.slideshow

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.TextView
import androidx.compose.ui.text.toLowerCase
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.fap.R
import com.example.fap.utils.SharedCurrencyManager
import java.util.Locale
import java.util.*
import kotlin.collections.ArrayList

class CategoryAdapter(
    private var categoryList: ArrayList<CategoryItem>
    ) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null
    private lateinit var sharedCurrency: SharedCurrencyManager
    private var colorRed = 0
    private var colorGreen = 0
    private var initialCategoryList = ArrayList<CategoryItem>().apply {
        categoryList?.let {addAll(it)}
    }

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

        holder.itemView.setOnClickListener{
            val action = CategoryFragmentDirections.actionCategoryToHistory(item.title)
            holder.itemView.findNavController().navigate(action)
        }
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    fun getFilter(): Filter {
        return filter
    }

    private val filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?):
                FilterResults {
                    val filteredList: ArrayList<CategoryItem> = ArrayList()
                    if (constraint.isNullOrEmpty()) {
                        initialCategoryList.let {filteredList.addAll(it)}
                    } else {
                        val query = constraint.toString().trim().lowercase()
                        initialCategoryList.forEach {
                            if (it.title.lowercase(Locale.ROOT).contains(query)) {
                                filteredList.add(it)
                            }
                        }
                    }
                    val results = FilterResults()
                    results.values = filteredList
                    for (result in results.values as ArrayList<CategoryItem>)
                        Log.d("FAP" , "Resultzap ${result.title}")
                    return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if (results?.values is ArrayList<*>) {
                for (category in categoryList)
                    Log.d("FAP", "zap ${category.title}")
                categoryList = results.values as ArrayList<CategoryItem>
                notifyDataSetChanged()
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.category_title)
        private val sumTextView: TextView = itemView.findViewById(R.id.category_sum)

        init {
            itemView.setOnClickListener{
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.onItemClick(position)
                }
            }
        }

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

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}
