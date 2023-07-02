package com.example.fap.ui.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fap.data.FapDatabase
import com.example.fap.utils.SharedPreferencesManager
import com.example.fap.databinding.FragmentCategoryBinding
import kotlinx.coroutines.launch

class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null

    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferencesManager
    private lateinit var categoryAdapter: CategoryAdapter
    private var categoryData = ArrayList<CategoryItem>()
    private lateinit var searchView: SearchView

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

        searchView = binding.categorySearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                updateSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                updateSearch(newText)
                return true
            }
        })
    return view
    }

    private inline fun updateSearch(search: CharSequence?) {
        categoryAdapter.getFilter().filter(search)
    }

    override fun onResume() {
        super.onResume()
        categoryData.clear()
        lifecycleScope.launch {
            val dbCategory = FapDatabase.getInstance(requireContext())
            val categories = dbCategory.fapDaoCategory().getCategories()
            for (category in categories) {
                val sumCat = dbCategory.fapDaoPayment().getTotalAmountByCategory(sharedPreferences.getCurUser(requireContext()), category.name)
                categoryData.add(CategoryItem(category.name, sumCat?: 0.0))
            }
            if (categories.isEmpty()) {
                binding.textCategory.visibility = View.VISIBLE
            } else {
                binding.textCategory.visibility = View.GONE
            }
            categoryAdapter.notifyDataSetChanged()
            updateSearch(searchView?.query)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
            _binding = null
        }
    }
