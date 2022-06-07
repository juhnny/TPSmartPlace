package com.juhnny.tpsmartplace.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.juhnny.tpsmartplace.R
import com.juhnny.tpsmartplace.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    val b by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)



    }
}