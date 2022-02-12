package net.numalab.hiraganacraft

import net.kunmc.lab.configlib.BaseConfig
import net.kunmc.lab.configlib.value.BooleanValue
import net.kunmc.lab.configlib.value.DoubleValue
import org.bukkit.plugin.Plugin

class HiraganaConfig(plugin: Plugin) : BaseConfig(plugin) {
    val isEnabled = BooleanValue(false)
    // ドロップする確率(%)
    val dropRate = DoubleValue(50.0)
}