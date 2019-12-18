package com.example.network.statistic.domian

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

abstract class UseCase<R> {

    protected val gson = Gson()

    abstract fun execute(): R

    inline fun <reified T> Gson.fromJson(json: String) = this.fromJson<T>(json, object: TypeToken<T>() {}.type)
}