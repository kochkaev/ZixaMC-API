package ru.kochkaev.zixamc.api.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.lang.reflect.Type

object GsonManager {

    private val adapters: HashMap<Type, Any> = hashMapOf()
    val gson: Gson
        get() = get()

    fun get(prettyPrinting: Boolean = true): Gson {
        val gson = GsonBuilder()
        if (prettyPrinting) gson.setPrettyPrinting()
        gson.disableHtmlEscaping()
            .enableComplexMapKeySerialization()
            .serializeNulls()
        adapters.forEach { (model, adapter) ->
            gson.registerTypeAdapter(model, adapter)
        }
        return gson.create()
    }
    fun registerAdapter(model: Type, adapter: Any) {
        adapters[model] = adapter
    }
    fun registerAdapters(vararg args: Pair<Type, Any>) {
        args.forEach { adapters[it.first] = it.second }
    }
}