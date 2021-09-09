package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.databinding.DataBindingUtil
import com.udacity.databinding.ActivityMainBinding

private const val TAG = "Main_Activity_Tag"

class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        binding.contentMain.customButton.setOnClickListener {
            //download(UDACITY_URL)
            val radioGroup = binding.contentMain.radioGroup
            val loadingButton = it as LoadingButton
            loadingButton.changeButtonState(ButtonState.Loading)

            when (radioGroup.checkedRadioButtonId) {
                R.id.radioGlide -> {
                    download(GLIDE_URL)
                }
                R.id.radioLoad -> {
                    download(LOAD_URL)
                }
                R.id.radioRetroFit -> {
                    download(RETROFIT_URL)
                }
                else -> {
                    Toast.makeText(applicationContext, R.string.noRadioSelected, Toast.LENGTH_SHORT).show()
                    loadingButton.changeButtonState(ButtonState.Completed)
                }
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            Log.d(TAG, "id : $id")
            if (id == downloadID) {
                Log.d(TAG, "Finish Download")
                //Give some delay to see animation
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.contentMain.customButton.changeButtonState(ButtonState.Completed)
                }, 1000)



            }
        }
    }

    private fun download(url : String) {
        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
        Log.d(TAG, "downloadID : $downloadID")
    }

    companion object {
        private const val LOAD_URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val GLIDE_URL = "https://github.com/bumptech/glide"
        private const val RETROFIT_URL = "https://github.com/square/retrofit"
        private const val CHANNEL_ID = "channelId"
    }

}
