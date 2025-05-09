package com.taner.taskly

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import java.io.File
import java.io.FileOutputStream

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val splashVideoView: VideoView = findViewById(R.id.splashVideoView)

        val videoUri = getVideoUriFromAssets(this, "splash_animation.mp4")

        splashVideoView.setVideoURI(videoUri)
        splashVideoView.start()
        splashVideoView.setOnCompletionListener {
            Handler().postDelayed({
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }, 100)
        }




    }

    private fun getVideoUriFromAssets(context: Context, fileName: String): Uri {
        val file = File(context.cacheDir, fileName)
        if (!file.exists()) {
            context.assets.open(fileName).use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
        return Uri.fromFile(file)
    }
}