package com.github.bun133.flyframe

import com.github.bun133.flyframe.command.Commands
import com.github.bun133.flyframe.util.MessageSendable
import com.github.bun133.flyframe.util.SeverSendable
import org.bukkit.plugin.InvalidPluginException
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.UnknownDependencyException
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

/**
 * Main Point
 */
class FlyFrame : JavaPlugin() {
    lateinit var modulesFolder: File
    val modules = mutableListOf<Module>()
    lateinit var eventHandler: EventHandler


    /**
     * OnEnable
     */
    override fun onEnable() {
        // Plugin startup logic
        eventHandler = EventHandler(this)
        Commands(this).onEnable()

        modulesFolder = File(this.dataFolder, "modules")

        if (!modulesFolder.exists()) {
            modulesFolder.mkdirs()
            // Skip Module Loading
        } else reloadModules(SeverSendable())
    }

    override fun onDisable() {
        // Plugin shutdown logic
        modules.forEach { it.onModuleEnable() }
    }

    fun reloadModules(se: MessageSendable) {
        val files = modulesFolder.listFiles()
        println("[FlyFrame] ${files.size} Files")
        var loadedPluginCount = 0
        files.forEach {
            println("FileName:${it.name}")
            var p: Plugin? = null
            var shouldSkipLoad = false
            try {
                p = pluginLoader.loadPlugin(it)
            } catch (e: InvalidPluginException) {
                se.send("${it.name} is not valid plugin file")
                shouldSkipLoad = true
            } catch (e: UnknownDependencyException) {
                se.send("${it.name} requires unknown plugin")
                shouldSkipLoad = true
            } catch (e: Exception) {
                se.send("Unknown Error Occurred!")
                se.send(e.localizedMessage)
                shouldSkipLoad = true
            }

            if (!shouldSkipLoad) {
                if (p is FlyModulePlugin) {
                    val module = p.getModule()
                    if (!registerModule(module)) {
                        se.send("Error Occurred While registering ${module.moduleName} Module")
                    } else {
                        loadedPluginCount++
                    }
                } else {
                    se.send("${p!!.name} is not valid fly-module-plugin!")
                }
            }
        }

        eventHandler.execute(ModuleEvent.LOADED_ALL_MODULE)
        se.send("[FlyFrame] $loadedPluginCount Modules Loaded!")
    }

    fun registerModule(module: Module): Boolean {
        try {
            if (modules.any { it.moduleName === module.moduleName }) {
                println("[FlyFrame] ModuleName:${module.moduleName} is Already Exits!")
                return false
            }
            modules.add(module)
            module.onModuleEnable()
        } catch (e: Exception) {
            return false
        }
        return true
    }

    fun removeModule(module: Module): Boolean {
        if (!modules.contains(module)) {
            println("FlyFrame#removeModule not registered such module!")
            return false
        }
        try {
            modules.remove(module)
            module.onModuleDisable()
        } catch (e: Exception) {
            return false
        }
        return true
    }

    fun requireModule(name: String): Module? {
        val matched = modules.filter { it.moduleName === name }
        if (matched.isEmpty()) return null
        if (matched.size > 1) {
            println("[FlyFrame][WARN] Duplicated Module Name found!")
            println("[FlyFrame][WARN] Duplicated Module Name:${name}")
        }
        matched[0].onEvent(ModuleEvent.MODULE_REQUIRED)
        return matched[0]
    }

    fun isModuleExits(name: String): Boolean {
        return requireModule(name) != null
    }
}

abstract class FlyModulePlugin : JavaPlugin() {
    abstract fun getModule(): Module
    abstract override fun onEnable()
    abstract override fun onDisable()
}

interface Module {
    var moduleName: String
    var version: String
    var authorName:String

    fun onModuleEnable()
    fun onModuleDisable()
    fun onEvent(e: ModuleEvent)
}

enum class ModuleEvent {
    LOADED_ALL_MODULE,
    MODULE_REQUIRED
}

class EventHandler(val plugin: FlyFrame) {
    fun execute(e: ModuleEvent) {
        plugin.modules.forEach { it.onEvent(e) }
    }
}