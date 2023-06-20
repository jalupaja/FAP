package com.example.fap.ui.home

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.fap.R
import com.example.fap.data.FapDatabase
import com.example.fap.data.Wallet
import com.example.fap.databinding.FragmentHomeBinding
import com.example.fap.utils.SharedCurrencyManager
import com.example.fap.utils.SharedPreferencesManager
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferencesManager
    private lateinit var sharedCurrency: SharedCurrencyManager

    // TODO in HomeFragment:
    private var wallets = ArrayList<Wallet>()
    private lateinit var walletAdapter: ViewPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        sharedPreferences = SharedPreferencesManager.getInstance(requireContext())
        sharedCurrency = SharedCurrencyManager.getInstance(requireContext())

        val view = binding.root

        val viewpager= binding.viewpagerHome
        wallets.add(Wallet("alskdjf", ""))
        walletAdapter = ViewPagerAdapter(wallets)
        viewpager.adapter = walletAdapter


        return view
    }

    override fun onResume() {
        super.onResume()
        // TODO in HomeFragment:
        wallets.clear()
        wallets.add(Wallet("", sharedPreferences.getCurUser(requireContext())))
        lifecycleScope.launch {
            val db = FapDatabase.getInstance(requireContext())
            val wallet_list = db.fapDao().getWallets(sharedPreferences.getCurUser(requireContext()))
            for (wallet in wallet_list) {
                Log.d("Wallets", wallet.name)
                wallets.add(wallet)
            }
        }
        // update values
        walletAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
