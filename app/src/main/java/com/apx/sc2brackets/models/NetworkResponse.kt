package com.apx.sc2brackets.models

data class NetworkResponse<T>(val isSuccessful: Boolean, val code: Int, val body: T)