package com.udacity.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.udacity.DetailActivity
import com.udacity.R


private const val ID = 0
private const val NOTIFY_BUTTON_TITLE = "Check Download"
object Constants {
    const val KEY_DOWNLOAD_FILE_NAME = "download_file_name"
    const val KEY_DOWNLOAD_STATUS = "download_status"
}


fun NotificationManager.sendNotification(message: String, applicationContext: Context, fileName: String, status: Int) {

    val contentIntent = Intent(applicationContext, DetailActivity::class.java)
    contentIntent.putExtra(Constants.KEY_DOWNLOAD_FILE_NAME , fileName)
    contentIntent.putExtra(Constants.KEY_DOWNLOAD_STATUS, status)
    val contentPendingIntent = PendingIntent.getActivities(
        applicationContext,
        ID,
        arrayOf(contentIntent),
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    val builder = NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.notifyChannelId)
    )
        .setSmallIcon(R.drawable.ic_assistant_black_24dp)
        .setContentTitle(applicationContext.getString(R.string.notification_title))
        .setContentText(message)
        .setContentIntent(contentPendingIntent)
        .addAction(R.drawable.ic_assistant_black_24dp, NOTIFY_BUTTON_TITLE, contentPendingIntent )
        .setAutoCancel(true)

    notify(ID, builder.build())
}