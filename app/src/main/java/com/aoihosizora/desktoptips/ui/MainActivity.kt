package com.aoihosizora.desktoptips.ui

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.aoihosizora.desktoptips.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initUI()
    }

    private fun initUI() {
        supportActionBar?.let {
            tab_layout.elevation = it.elevation
            supportActionBar?.elevation = 0F
        }
        initTabLayout()
    }

    private fun initTabLayout() {
        view_pager.adapter = TabPageAdapter(this, supportFragmentManager)
        tab_layout.setupWithViewPager(view_pager)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.menu_add -> {
                Toast.makeText(this, item.title, Toast.LENGTH_SHORT).show()
            }
            R.id.menu_delete -> {
                Toast.makeText(this, item.title, Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
