package com.example.bookapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.bookapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //menangani klik, Login
        binding.loginBtn.setOnClickListener {
            //akan dilakukan nanti
            startActivity(Intent(this, LoginActivity::class.java))
        }

        //hand click, lewati dan lanjutkan ke layar utama
        binding.skipBtn.setOnClickListener {
            //will do later
            startActivity(Intent(this, DashboardUserActivity::class.java))
        }
    }
}