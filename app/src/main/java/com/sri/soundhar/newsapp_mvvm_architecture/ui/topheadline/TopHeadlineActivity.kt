package com.sri.soundhar.newsapp_mvvm_architecture.ui.topheadline

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sri.soundhar.newsapp_mvvm_architecture.NewsApplication
import com.sri.soundhar.newsapp_mvvm_architecture.R
import com.sri.soundhar.newsapp_mvvm_architecture.di.component.DaggerActivityComponent
import com.sri.soundhar.newsapp_mvvm_architecture.di.module.ActivityModule
import javax.inject.Inject

class TopHeadlineActivity : AppCompatActivity() {

    @Inject
    lateinit var newsListViewModel: TopHeadlineViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
        enableEdgeToEdge()
        setContentView(R.layout.activity_top_headline)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }


    private fun injectDependencies() {
        DaggerActivityComponent.builder()
            .applicationComponent((application as NewsApplication).applicationComponent)
            .activityModule(ActivityModule(this)).build().inject(this)
    }
}