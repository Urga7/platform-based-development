package si.uni_lj.fri.pbd.pbd2025_lab_5

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class TabPagerAdapter(fa: FragmentActivity?, private val tabCounter: Int) : FragmentStateAdapter(fa!!) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            1 -> Tab1Fragment()
            2 -> Tab2Fragment()
            else -> Tab3Fragment()
        }
    }

    override fun getItemCount(): Int {
        return tabCounter
    }

}