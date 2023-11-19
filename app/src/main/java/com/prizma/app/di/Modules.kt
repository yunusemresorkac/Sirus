package com.prizma.app.di

import com.prizma.app.viewmodel.GiftViewModel
import com.prizma.app.viewmodel.InfoViewModel
import com.prizma.app.viewmodel.TestViewModel
import com.prizma.app.viewmodel.TimeViewModel
import com.prizma.app.viewmodel.UserViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    viewModel{
        TestViewModel(get())
    }

    viewModel{
        InfoViewModel(get())
    }

    viewModel{
        GiftViewModel(get())
    }

    viewModel{
        TimeViewModel(get())
    }

    viewModel{
        UserViewModel(get())
    }

}