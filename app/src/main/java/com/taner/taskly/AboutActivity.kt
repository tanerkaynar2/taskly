package com.taner.taskly

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.net.Uri
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.taner.taskly.R


class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_about)



        val appName: TextView = findViewById(R.id.appName)
        val version: TextView = findViewById(R.id.version)
        val description: TextView = findViewById(R.id.description)
        val contactUsButton: Button = findViewById(R.id.contactUsButton)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        contactUsButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:tkaynar198@gmail.com"))
            intent.putExtra(Intent.EXTRA_SUBJECT, "Taskly - Geri Bildirim")
            startActivity(Intent.createChooser(intent, "E-posta Uygulaması Seç"))
        }

    }




    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


}