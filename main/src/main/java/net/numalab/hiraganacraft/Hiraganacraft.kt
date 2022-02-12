package net.numalab.hiraganacraft

import dev.kotx.flylib.flyLib
import net.numalab.hiraganacraft.command.MainCommand
import org.bukkit.plugin.java.JavaPlugin

class Hiraganacraft : JavaPlugin() {
    private val config = HiraganaConfig(this)

    init {
        config.saveConfigIfAbsent()
        config.loadConfig()
        flyLib {
            command(MainCommand(config))
        }
    }

    override fun onEnable() {
        // Plugin startup logic
    }

    override fun onDisable() {
        // Plugin shutdown logic
        config.saveConfigIfPresent()
    }
}