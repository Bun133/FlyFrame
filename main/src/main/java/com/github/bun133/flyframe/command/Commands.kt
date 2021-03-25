package com.github.bun133.flyframe.command

import com.github.bun133.flyframe.FlyFrame
import com.github.bun133.flyframe.flylib.SmartTabCompleter
import com.github.bun133.flyframe.flylib.TabChain
import com.github.bun133.flyframe.flylib.TabObject
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class Commands(val plugin: FlyFrame) {
    fun onEnable() {
        plugin.getCommand("fm")!!.setExecutor(FMCommand(plugin))
        plugin.getCommand("fm")!!.tabCompleter = FMCommand.gen()
    }
}

class FMCommand(val plugin: FlyFrame) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            if (sender.isOp) {
                return run(sender, command, label, args)
            } else {
                sender.sendMessage("You don't have enough permission!")
                return false
            }
        } else return run(sender, command, label, args)
    }

    fun run(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        when (args.size) {
            1 -> {
                when (args[0]) {
                    "list" -> {
                        val desc =
                            plugin.modules.map { module -> Pair(Pair(module.moduleName, module.version),module.authorName) }.toTypedArray()
                        desc.forEach {
                            sender.sendMessage("Name:${it.first.first} Version:${it.first.second} Author:${it.second}")
                        }
                        sender.sendMessage("${desc.size} Modules are loaded!")
                        return true
                    }
                    "help" -> {
                        var authorString = ""
                        plugin.description.authors.forEach { authorString += "$it " }
                        sender.sendMessage("FlyFrame Version:${plugin.description.version} Authors:${authorString}")
                        return true
                    }
                    else -> return false
                }
            }
            else -> return false
        }
    }

    companion object{
        fun gen(): SmartTabCompleter {
            return SmartTabCompleter(
                TabChain(
                    TabObject(
                        "list"
                    )
                ),
                TabChain(
                    TabObject(
                        "help"
                    )
                )
            )
        }
    }
}