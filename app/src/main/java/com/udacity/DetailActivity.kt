package com.udacity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.udacity.databinding.ActivityDetailBinding
import com.udacity.notification.Constants

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    private val downloadData: DownloadData = DownloadData("Status Check", "File Name Check")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail)
        setSupportActionBar(binding.toolbar)

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

        val intent = Intent(this, MainActivity::class.java)
        binding.fabBackButton.setOnClickListener {
            startActivity(intent)
        }
    }
}

data class DownloadData(var status: String = "", var fileName: String = "")
