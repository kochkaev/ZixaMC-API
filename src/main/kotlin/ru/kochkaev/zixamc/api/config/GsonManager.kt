package ru.kochkaev.zixamc.api.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapterFactory
import java.lang.reflect.Type

object GsonManager {

    private val adapters: HashMap<Type, Any> = hashMapOf()
    private val hierarchyAdapters: HashMap<Class<*>, Any> = hashMapOf()
    private val adapterFactories: ArrayList<TypeAdapterFactory> = arrayListOf()
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
        hierarchyAdapters.forEach { (model, adapter) ->
            gson.registerTypeHierarchyAdapter(model, adapter)
        }
        adapterFactories.forEach { factory ->
            gson.registerTypeAdapterFactory(factory)
        }
        return gson.create()
    }
    fun registerAdapter(model: Type, adapter: Any) {
        adapters[model] = adapter
    }
    fun registerAdapters(vararg args: Pair<Type, Any>) {
        args.forEach { adapters[it.first] = it.second }
    }
    fun registerHierarchyAdapter(model: Class<*>, adapter: Any) {
        hierarchyAdapters[model] = adapter
    }
    fun registerHierarchyAdapters(vararg args: Pair<Class<*>, Any>) {
        args.forEach { hierarchyAdapters[it.first] = it.second }
    }
    fun registerAdapterFactory(factory: TypeAdapterFactory) {
        adapterFactories.add(factory)
    }
    fun registerAdapterFactories(vararg args: TypeAdapterFactory) {
        adapterFactories.addAll(args)
    }
}