package si.uni_lj.fri.pbd.pbd2025_lab_5

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.tabs.TabLayoutMediator
import si.uni_lj.fri.pbd.pbd2025_lab_5.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    companion object {
        private const val NUM_OF_TABS = 3
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(binding.toolbar)
        configureTabLayout()
    }

    private fun configureTabLayout() {
        val tabLayout = binding.tabLayout
        val viewPager = binding.viewPager
        val adapter = TabPagerAdapter(this, NUM_OF_TABS)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = getString(when (position) {
                1 -> R.string.tab1_title
                2 -> R.string.tab2_title
                else -> R.string.tab3_title
            })
        }.attach()
    }
}