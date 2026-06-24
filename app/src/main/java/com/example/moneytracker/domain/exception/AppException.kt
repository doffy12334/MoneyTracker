package com.example.moneytracker.domain.exception

import androidx.annotation.StringRes

class AppException(@StringRes val messageResId: Int) : Exception()
