package si.uni_lj.fri.pbd.classproject2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import com.google.android.material.navigation.NavigationView
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.snackbar.Snackbar
import si.uni_lj.fri.pbd.classproject2.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object {
        private const val ACTIVITY_RECOGNITION_CODE = 1001
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)

        drawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.dashboardFragment, R.id.historyFragment, R.id.settingsFragment
            ), drawerLayout
        )
        
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        checkPermissions()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ACTIVITY_RECOGNITION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startSensingService()
        } else {
            Snackbar.make(binding.root, "Permission denied. Cannot track activity.", Snackbar.LENGTH_LONG).show()
        }
    }


    private fun startSensingService() {
        val intent = Intent(this, SensingService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }


    private fun checkPermissions() {
        val activityRecognitionPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
        if (activityRecognitionPermission != PackageManager.PERMISSION_GRANTED) {
            val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACTIVITY_RECOGNITION)
            if (shouldShowRationale) showPermissionRationale()
            else requestPermissions()
        } else {
            startSensingService()
        }
    }


    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
            ACTIVITY_RECOGNITION_CODE
        )
    }

    private fun showPermissionRationale() {
        val snackbar = Snackbar.make(
            binding.root,
            R.string.permission_notifications_rationale,
            Snackbar.LENGTH_INDEFINITE
        )

        snackbar.setAction(R.string.ok) { requestPermissions() }
        snackbar.show()
    }

}