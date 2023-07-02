package com.example.fap.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fap.data.FapDatabase
import com.example.fap.databinding.FragmentHistoryBinding
import com.example.fap.utils.SharedPreferencesManager
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.

    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferencesManager
    private var historyData = ArrayList<HistoryItem>()
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var categoryHistory: String
    private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val view: View = binding.root
        categoryHistory = arguments?.getString("categoryNameHistory", "showAll").toString()
        sharedPreferences = SharedPreferencesManager.getInstance(requireContext())

        val recyclerView = binding.historyRecyclerview
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        historyAdapter = HistoryAdapter(historyData)
        recyclerView.adapter = historyAdapter
        searchView = binding.historySearchView

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
        historyAdapter.getFilter().filter(search)
    }

    override fun onResume() {
        super.onResume()
        historyData.clear()
        lifecycleScope.launch {
            val dbPayment = FapDatabase.getInstance(requireContext()).fapDaoPayment()
            val payments = if (categoryHistory == "showAll") {
                dbPayment.getPayments(sharedPreferences.getCurUser(requireContext())).reversed()
            } else {
                dbPayment.getPaymentsByCategory(sharedPreferences.getCurUser(requireContext()), categoryHistory).reversed()
            }
            for (payment in payments) {
                historyData.add(HistoryItem(payment.id, payment.title, payment.category ?: "", payment.price, payment.isPayment, payment.date))
            }
            if (payments.isEmpty()) {
                binding.lblHistoryEmpty.visibility = View.VISIBLE
            } else {
                binding.lblHistoryEmpty.visibility = View.GONE
            }
            historyAdapter.notifyDataSetChanged()
            updateSearch(searchView?.query)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
