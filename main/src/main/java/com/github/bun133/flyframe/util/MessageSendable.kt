@file:Suppress("SpellCheckingInspection")

package com.github.bun133.flyframe.util

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

abstract class MessageSendable {
    abstract fun send(mes:String)
}

class CommandSendable(private val se:CommandSender): MessageSendable() {
    override fun send(mes: String) {
        se.sendMessage(mes)
    }
}

class SeverSendable():MessageSendable() {
    override fun send(mes: String) {
        println(mes)
    }
}

class BroadcastSendable():MessageSendable(){
    override fun send(mes: String) {
        Bukkit.broadcastMessage(mes)
    }
}