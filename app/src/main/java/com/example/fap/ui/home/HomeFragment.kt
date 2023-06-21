package com.example.fap.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.fap.data.FapDatabase
import com.example.fap.databinding.FragmentHomeBinding
import com.example.fap.utils.SharedCurrencyManager
import com.example.fap.utils.SharedPreferencesManager
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferencesManager
    private lateinit var sharedCurrency: SharedCurrencyManager

    // TODO in HomeFragment:
    private var wallets = ArrayList<WalletInfo>()
    private lateinit var walletAdapter: HomeAdapter

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
        walletAdapter = HomeAdapter(wallets)
        viewpager.adapter = walletAdapter

        return view
    }

    override fun onResume() {
        super.onResume()

        var totalSpent = 0.0
        var totalIncome = 0.0
        var totalSpentMonth = 0.0
        var totalIncomeMonth = 0.0
        val currency = sharedCurrency.getCurrency()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)

        lifecycleScope.launch {
            val db = FapDatabase.getInstance(requireContext())
            val list_paymentsByWallets = db.fapDao().getPaymentsByWallets(sharedPreferences.getCurUser(requireContext()))
            wallets.clear()
            for (paymentsByWallet in list_paymentsByWallets) {
                var totalSpentWallet = 0.0
                var totalIncomeWallet = 0.0
                var totalSpentMonthWallet = 0.0
                var totalIncomeMonthWallet = 0.0
                val walletName = paymentsByWallet.wallet.name
                for (payment in paymentsByWallet.payments) {
                    val paymentYear = Calendar.getInstance().apply { time = payment.date }.get(Calendar.YEAR)
                    val paymentMonth = Calendar.getInstance().apply { time = payment.date }.get(Calendar.MONTH)

                    if (paymentYear == currentYear && paymentMonth == currentMonth) {
                        if (payment.isPayment) {
                            totalSpentMonthWallet += payment.price
                        } else {
                            totalIncomeMonthWallet += payment.price
                        }
                    }
                    if (payment.isPayment) {
                        totalSpentWallet += payment.price
                    } else {
                        totalIncomeWallet += payment.price
                    }
                }
                totalIncomeMonth += totalIncomeMonthWallet
                totalSpentMonth += totalSpentMonthWallet
                totalIncome += totalIncomeWallet
                totalSpent += totalSpentWallet

                wallets.add(WalletInfo(walletName, totalIncomeWallet, totalSpentWallet, totalIncomeMonthWallet, totalSpentMonthWallet, currency))
            }
            wallets.add(0, WalletInfo("", totalIncome, totalSpent, totalIncomeMonth, totalSpentMonth, currency))

            walletAdapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
