package com.example.fap.ui.slideshow

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fap.R
import com.example.fap.data.FapDatabase
import com.example.fap.utils.SharedPreferencesManager
import com.example.fap.databinding.FragmentCategoryBinding
import kotlinx.coroutines.launch

class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null

    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferencesManager
    private var categoryData = ArrayList<CategoryItem>()
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        val view: View = binding.root

        sharedPreferences = SharedPreferencesManager.getInstance(requireContext())

        val recyclerView = binding.categoryRecyclerview
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        categoryAdapter = CategoryAdapter(categoryData)
        recyclerView.adapter = categoryAdapter

    return view
    }

    override fun onResume() {
        super.onResume()
        categoryData.clear()
        lifecycleScope.launch {
            val db = FapDatabase.getInstance(requireContext())
            val categorys = db.fapDao().getCategories()
            for (category in categorys) {
                val sumCat = db.fapDao().getTotalAmountByCategory(sharedPreferences.getCurUser(requireContext()), category.name)
                categoryData.add(CategoryItem(category.name, sumCat?: 0.0))
            }
            if (categorys.isEmpty()) {
                binding.textCategory.visibility = View.VISIBLE
            } else {
                binding.textCategory.visibility = View.GONE
            }
            categoryAdapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
            _binding = null
        }
    }
