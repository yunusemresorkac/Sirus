package com.yeslab.sirus.di

import com.yeslab.sirus.viewmodel.GiftViewModel
import com.yeslab.sirus.viewmodel.InfoViewModel
import com.yeslab.sirus.viewmodel.TestViewModel
import com.yeslab.sirus.viewmodel.TimeViewModel
import com.yeslab.sirus.viewmodel.UserViewModel
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