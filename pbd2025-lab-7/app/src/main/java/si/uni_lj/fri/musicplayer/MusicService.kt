package si.uni_lj.fri.musicplayer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import si.uni_lj.fri.musicplayer.MainActivity.Companion
import java.io.IOException
import java.util.Locale

class MusicService : Service() {

    companion object {
        val TAG: String = MusicService::class.java.simpleName
    }

    private val binder = LocalBinder(this)
    val player = MediaPlayer()
    var song = ""

    internal class LocalBinder(val service: MusicService) : Binder()

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY


    /**
     * Starts the music player playback
     */
    fun play() {
        player.let {
            if (it.isPlaying) {
                return
            }
            getFiles().random().apply {
                try {
                    val descriptor = assets.openFd(this)
                    it.setDataSource(
                        descriptor.fileDescriptor,
                        descriptor.startOffset,
                        descriptor.length
                    )
                    descriptor.close()
                    it.prepare()
                } catch (e: IOException) {
                    Log.w(TAG, "Could not open file", e)
                    return
                }
                it.isLooping = true
                it.start()
                song = this

                broadcastSongName()
                Log.i(TAG, "Playing song $this")
                startForegroundService()
            }
        }
    }

    /**
     * Stops the music player playback
     */
    fun stop() {
        player.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.reset()
            song = ""
        }

        broadcastSongName()
    }

    private fun broadcastSongName() {
        val intent = Intent("mplayer") // mplayer is the name of the broadcast
        intent.putExtra("song", song) // song name is added as the parameter
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent) // the broadcast is sent
    }

    /**
     * Returns the list of mp3 files in the assets folder
     */
    private fun getFiles(): List<String> =
        assets.list("")?.filter { it.toLowerCase(Locale.getDefault()).endsWith("mp3") }
            ?: emptyList()

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "music_channel",
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun startForegroundService() {
        createNotificationChannel()
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(this,
            "music_channel")
            .setContentTitle("Music Player")
            .setContentText("Playing: $song")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)
    }
}