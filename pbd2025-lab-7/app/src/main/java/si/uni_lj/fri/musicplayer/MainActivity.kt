package si.uni_lj.fri.musicplayer


import android.Manifest
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.snackbar.Snackbar
import si.uni_lj.fri.musicplayer.databinding.ActivityMainBinding
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        const val NOTIF_REQUEST_CODE = 42
    }

    private lateinit var binding: ActivityMainBinding
    private var service: MusicService? = null
    private val handler = android.os.Handler(Looper.getMainLooper())

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.i(TAG, "onServiceConnected()")
            this@MainActivity.service = (service as MusicService.LocalBinder).service
            binding.musicInfoTextView.text = this@MainActivity.service?.song
            handler.post(updateTimeRunnable)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.i(TAG, "onServiceDisconnected()")
            service = null
        }
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            binding.musicInfoTextView.text = intent?.getStringExtra("song")
        }
    }

    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            service?.player?.let {
                if (it.isPlaying) {
                    val elapsed = it.currentPosition / 1000
                    val minutes = elapsed / 60
                    val seconds = elapsed % 60
                    binding.songProgressTextView.text =
                        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
                }
            }
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate()")

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.playButton.setOnClickListener { service?.play() }
        binding.stopButton.setOnClickListener { service?.stop() }
        binding.startServiceButton.setOnClickListener {
            checkPermissions()
            val intent = Intent(this@MainActivity, MusicService::class.java)
            startForegroundService(intent)
            bindService(intent, connection, BIND_AUTO_CREATE)
        }
        binding.stopServiceButton.setOnClickListener {
            service?.let {
                unbindService(connection)
                service = null
                stopService(Intent(this@MainActivity, MusicService::class.java))
                binding.musicInfoTextView.text = ""
                binding.songProgressTextView.text = "00:00"
                handler.removeCallbacks(updateTimeRunnable)
            }
        }
        binding.aboutButton.setOnClickListener {
            startActivity(
                Intent(
                    this@MainActivity,
                    AboutActivity::class.java
                )
            )
        }
    }

    override fun onStop() {
        Log.i(TAG, "onStop()")
        service?.let {
            unbindService(connection)
            service = null
        }

        LocalBroadcastManager
            .getInstance(this)
            .unregisterReceiver(receiver)

        handler.removeCallbacks(updateTimeRunnable)
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "onStart()")
        if (isServiceRunning()) {
            bindService(
                Intent(this@MainActivity, MusicService::class.java),
                connection,
                BIND_AUTO_CREATE
            )
        }

        LocalBroadcastManager
            .getInstance(this)
            .registerReceiver(receiver, IntentFilter("mplayer"))
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val postNotificationPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
        if (postNotificationPermission != PackageManager.PERMISSION_GRANTED) {
            val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)
            if (shouldShowRationale) showPermissionRationale()
            else requestPermissions()
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            NOTIF_REQUEST_CODE
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

    private fun isServiceRunning(): Boolean =
        (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
            .getRunningServices(Int.MAX_VALUE)
            .any { it.service.className == MusicService::class.java.canonicalName }
}
