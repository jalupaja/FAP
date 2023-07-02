package com.example.fap.ui.home

import com.example.fap.ui.category.CategoryItem
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter

class BarXAxisFormatter(categories: List<CategoryItem>): ValueFormatter() {
    private val passedCategories = categories
    private val newCategories = ArrayList<String>()

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        lateinit var categorySub: String
        for (category in passedCategories) {
            categorySub = if (category.title.length >= 9) {
                category.title.substring(0, 8)
            } else {
                category.title
            }

            newCategories.add(categorySub)
        }

        return newCategories.getOrNull(value.toInt()) ?: value.toString()
    }
}