package com.mahdisabra.currencyexchange


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class TabsPagerAdapter(fm: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fm, lifecycle) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                ExchangeFragment()
            }
            1 -> {
                TransactionsFragment()
            }
            2 -> {
                PendingFragment()
            }
            else -> ExchangeFragment()
        }
    }
    override fun getItemCount(): Int {
        return 3
    }
}
