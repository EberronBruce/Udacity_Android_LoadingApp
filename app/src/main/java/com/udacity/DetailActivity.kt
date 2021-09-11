package com.udacity

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.udacity.databinding.ActivityDetailBinding
import com.udacity.notification.Constants

private const val TAG = "DetailActivity"

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    private val downloadData: DownloadData = DownloadData("Status Check", "File Name Check")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail)
        setSupportActionBar(binding.toolbar)
        Log.d("${TAG}.onCreate", "Constants: ${Constants.KEY_DOWNLOAD_FILE_NAME}, ${Constants.KEY_DOWNLOAD_STATUS}")
        Log.d("${TAG}.onCreate", "title: ${ intent.getStringExtra(Constants.KEY_DOWNLOAD_FILE_NAME).toString()}, status: ${intent.getIntExtra(Constants.KEY_DOWNLOAD_STATUS, -1)}")

        val height = resources.displayMetrics.heightPixels
        Log.d("${TAG}.onCreate", "height : $height")


        binding.contentDetail.downloadData = downloadData
        downloadData.fileName = intent.getStringExtra(Constants.KEY_DOWNLOAD_FILE_NAME) ?: "No FileName"

        when (intent.getIntExtra(Constants.KEY_DOWNLOAD_STATUS, -1)) {
            8 -> {
                binding.contentDetail.status.setTextColor(Color.GREEN)
                downloadData.status = getString(R.string.download_success)
            }
            16 -> {
                binding.contentDetail.status.setTextColor(Color.RED)
                downloadData.status = getString(R.string.download_failure)
            }
            else -> {
                binding.contentDetail.status.setTextColor(Color.BLACK)
                downloadData.status = getString(R.string.download_unknown)
            }
        }

    }



}

data class DownloadData(var status: String = "", var fileName: String = "")
