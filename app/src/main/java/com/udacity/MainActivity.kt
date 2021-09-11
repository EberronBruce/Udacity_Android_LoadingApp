package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.udacity.databinding.ActivityMainBinding
import com.udacity.notification.sendNotification

private const val TAG = "Main_Activity_Tag"

class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        binding.contentMain.customButton.setOnClickListener {
            val loadingButton = it as LoadingButton
            loadingButton.changeButtonState(ButtonState.Loading)
            setupCustomButtonOnClickLister(loadingButton)
            createChannel(getString(R.string.notifyChannelId), getString(R.string.notifyChannelName))
        }
    }

    private fun setupCustomButtonOnClickLister(loadingButton: LoadingButton) {
        val radioGroup = binding.contentMain.radioGroup
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

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            val downloadManager : DownloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            Log.d(TAG, "id : $id")
            if (id == downloadID) {
                createBroadcastReceiver(context, downloadManager)
            }
        }
    }

    private fun createBroadcastReceiver(context: Context?, downloadManager: DownloadManager) {
        Toast.makeText(context, "Download Completed", Toast.LENGTH_SHORT).show()

        val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadID))

        if (cursor.moveToFirst()) {
            var column = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)
            val title = if (column >= 0) cursor.getString(column) else "No Title"
            column = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            cursor.downloadManagerSelection(context, column, title)
        }
    }

    private fun Cursor.downloadManagerSelection(context: Context?, column: Int, title: String) {
        when (getInt(column)) {
            DownloadManager.STATUS_SUCCESSFUL -> {
                Log.d(TAG, "Status Success : ${DownloadManager.STATUS_SUCCESSFUL}")
                //Give some delay to see animation
                Handler(Looper.getMainLooper()).postDelayed({
                    Toast.makeText(context, "Download Success", Toast.LENGTH_SHORT).show()
                    binding.contentMain.customButton.changeButtonState(ButtonState.Completed)
                    sendCompleteNotify(title, DownloadManager.STATUS_SUCCESSFUL)
                }, 1000)

            }
            DownloadManager.STATUS_FAILED -> {
                Log.d(TAG, "Status Failed : ${DownloadManager.STATUS_FAILED}")
                Toast.makeText(context, "Download Failed", Toast.LENGTH_SHORT).show()
                binding.contentMain.customButton.changeButtonState(ButtonState.Completed)
                sendCompleteNotify(title, DownloadManager.STATUS_FAILED)
            }
            DownloadManager.STATUS_PAUSED -> {
                Log.d(TAG, "Status Paused : ${DownloadManager.STATUS_PAUSED}")
            }
            DownloadManager.STATUS_PENDING -> {
                Log.d(TAG, "Status Pending : ${DownloadManager.STATUS_PENDING}")
            }
            DownloadManager.STATUS_RUNNING -> {
                Log.d(TAG, "Status Running : ${DownloadManager.STATUS_RUNNING}")
            }
            else -> {

            }
        }
    }

    private fun download(url : String) {
        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(when (url) {
                    LOAD_URL -> "LoadApp"
                    GLIDE_URL -> "Glide"
                    RETROFIT_URL -> "Retrofit"
                    else -> "Unknown"
                })
                .setDescription(when (url) {
                    LOAD_URL -> getString(R.string.radioLoadApp)
                    GLIDE_URL -> getString(R.string.radioGlide)
                    RETROFIT_URL -> getString(R.string.radioRetrofit)
                    else -> "Unknown"
                })
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    companion object {
        private const val LOAD_URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val GLIDE_URL = "https://github.com/bumptech/glide"
        private const val RETROFIT_URL = "https://github.com/square/retrofit"
    }

    private fun createChannel(id : String, name: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW).apply { setShowBadge(false) }
            channel.enableLights(true)
            channel.lightColor = Color.RED
            channel.enableVibration(true)
            channel.description = getString(R.string.notification_description)

            notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendCompleteNotify(title: String, status: Int) {
        notificationManager = ContextCompat.getSystemService(application, NotificationManager::class.java) as NotificationManager
        notificationManager.sendNotification(application.getString(R.string.notification_description), applicationContext, title, status)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

}
