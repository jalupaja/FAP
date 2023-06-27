package com.example.fap.ui.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fap.R
import com.example.fap.data.FapDatabase
import com.example.fap.data.entities.Category
import com.example.fap.utils.SharedPreferencesManager
import com.example.fap.databinding.FragmentCategoryBinding
import kotlinx.coroutines.launch

class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null

    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferencesManager
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var categoryData: List<CategoryItem>
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

        searchView = view.findViewById(R.id.category_searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                categoryAdapter?.getFilter()?.filter(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                categoryAdapter?.getFilter()?.filter(newText)
                return true
            }
        })


    return view
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val dbCategory = FapDatabase.getInstance(requireContext())
            val categoryData = dbCategory.fapDaoPayment().getTotalAmountByCategory(sharedPreferences.getCurUser(requireContext()))

            if (categoryData.isEmpty()) {
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
