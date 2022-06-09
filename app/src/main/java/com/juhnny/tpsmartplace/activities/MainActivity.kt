package com.juhnny.tpsmartplace.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuInflater
import android.widget.Toast
import com.juhnny.tpsmartplace.R
import com.juhnny.tpsmartplace.databinding.ActivityMainBinding
import com.juhnny.tpsmartplace.databinding.QuickListBinding

class MainActivity : AppCompatActivity() {

    val b by lazy { ActivityMainBinding.inflate(layoutInflater) }
    val quick by lazy { QuickListBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)

        setSupportActionBar(b.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        b.layoutQuick.quickToilet

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val m = menuInflater.inflate(R.menu.actionbar_main, menu)
        return super.onCreateOptionsMenu(menu)
    }
}