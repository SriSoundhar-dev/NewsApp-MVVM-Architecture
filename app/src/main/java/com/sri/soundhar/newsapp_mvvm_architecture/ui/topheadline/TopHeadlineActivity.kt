package com.sri.soundhar.newsapp_mvvm_architecture.ui.topheadline

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.sri.soundhar.newsapp_mvvm_architecture.NewsApplication
import com.sri.soundhar.newsapp_mvvm_architecture.R
import com.sri.soundhar.newsapp_mvvm_architecture.data.model.Article
import com.sri.soundhar.newsapp_mvvm_architecture.databinding.ActivityTopHeadlineBinding
import com.sri.soundhar.newsapp_mvvm_architecture.di.component.DaggerActivityComponent
import com.sri.soundhar.newsapp_mvvm_architecture.di.module.ActivityModule
import com.sri.soundhar.newsapp_mvvm_architecture.ui.base.UiState
import kotlinx.coroutines.launch
import javax.inject.Inject

class TopHeadlineActivity : AppCompatActivity() {

    @Inject
    lateinit var newsListViewModel: TopHeadlineViewModel

    @Inject
    lateinit var adapter: TopHeadlineAdapter

    private lateinit var binding: ActivityTopHeadlineBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies()
        super.onCreate(savedInstanceState)
        binding = ActivityTopHeadlineBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUI()
        setupObserver()

    }

    private fun setupUI() {
        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                recyclerView.context,
                (recyclerView.layoutManager as LinearLayoutManager).orientation
            )
        )
        recyclerView.adapter = adapter
        binding.buttonRetry.setOnClickListener { newsListViewModel.fetchNews() }
    }
    private fun setupObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                newsListViewModel.uiState.collect {
                    when (it) {
                        is UiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            binding.errorGroup.visibility = View.GONE
                            renderList(it.data)
                            binding.recyclerView.visibility = View.VISIBLE
                        }
                        is UiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.errorGroup.visibility = View.GONE
                            binding.recyclerView.visibility = View.GONE
                        }
                        is UiState.Error -> {
                            // The state's message carries the raw exception for logs; the
                            // user gets fixed copy and a way to try again.
                            Log.w(TAG, "Could not load top headlines: ${it.message}")
                            binding.progressBar.visibility = View.GONE
                            binding.recyclerView.visibility = View.GONE
                            binding.errorGroup.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }
    private fun renderList(articleList: List<Article>) {
        adapter.setData(articleList)
        adapter.notifyDataSetChanged()
    }

    private fun injectDependencies() {
        DaggerActivityComponent.builder()
            .applicationComponent((application as NewsApplication).applicationComponent)
            .activityModule(ActivityModule(this)).build().inject(this)
    }

    private companion object {
        const val TAG = "TopHeadlineActivity"
    }
}
