package com.example.exercise.ui.viewModel

import androidx.lifecycle.ViewModel

import androidx.lifecycle.ViewModelProvider
import com.example.exercise.data.network.RandomJokeRepository


class JokeViewModelFactory(repository: RandomJokeRepository, firstName:String,lastName:String,pageIndex :Int) :
        ViewModelProvider.Factory {
    private val repository: RandomJokeRepository = repository
    val firstName : String = firstName
    val lastName : String = lastName
    var pageIndex :Int = pageIndex

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return JokeViewModel(repository,firstName,lastName,pageIndex) as T
    }
}