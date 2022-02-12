package net.numalab.hiraganacraft

import net.kunmc.lab.configlib.BaseConfig
import net.kunmc.lab.configlib.value.BooleanValue
import org.bukkit.plugin.Plugin

class HiraganaConfig(plugin: Plugin) : BaseConfig(plugin) {
    val isEnabled = BooleanValue(false)
}