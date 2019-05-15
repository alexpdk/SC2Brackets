package com.apx.sc2brackets.network

data class NetworkResponse<T>(val isSuccessful: Boolean, val code: Int, val body: T){

    var handler: Handler? = null
        private set

    fun setHander(handler: Handler): NetworkResponse<T>{
        this.handler = handler
        return this
    }

    /**Which data flow will handle response for network request*/
    enum class Handler{
        USER_ACTION, AUTO_UPDATE
    }
}