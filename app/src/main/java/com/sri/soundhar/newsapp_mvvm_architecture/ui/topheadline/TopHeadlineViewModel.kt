package com.sri.soundhar.newsapp_mvvm_architecture.ui.topheadline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sri.soundhar.newsapp_mvvm_architecture.data.model.Article
import com.sri.soundhar.newsapp_mvvm_architecture.data.repository.TopHeadlineRepository
import com.sri.soundhar.newsapp_mvvm_architecture.ui.base.UiState
import com.sri.soundhar.newsapp_mvvm_architecture.uitils.AppConstant.COUNTRY
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class TopHeadlineViewModel(private val topHeadlineRepository: TopHeadlineRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<Article>>>(UiState.Loading)

    val uiState: StateFlow<UiState<List<Article>>> = _uiState
    init {
        fetchNews()
    }

    /** Public so the error state's Retry button can ask for another attempt. */
    fun fetchNews() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            topHeadlineRepository.getTopHeadlines(COUNTRY)
                .catch { e ->
                    _uiState.value = UiState.Error(e.toString())
                }.collect {
                    _uiState.value = UiState.Success(it)
                }
        }
    }
}