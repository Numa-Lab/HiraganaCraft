package net.numalab.hiraganacraft

import dev.kotx.flylib.FlyLibBuilder
import dev.kotx.flylib.flyLib
import net.kyori.adventure.text.Component
import net.numalab.hiraganacraft.command.MainCommand
import org.bukkit.Material
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.awt.TextComponent
import kotlin.streams.toList

class Hiraganacraft : JavaPlugin() {
    private val config = HiraganaConfig(this).also {
        it.saveConfigIfAbsent()
        it.loadConfig()
    }

    private val converter = HiraganaConverter(this.getTextResource("ja_jp.json")!!)

    init {
        flyLib {
            command(MainCommand(config))
            listens(this)
        }
    }

    private fun listens(flyLibBuilder: FlyLibBuilder) {
        // ブロック破壊時にひらがなカードが落ちるように
        flyLibBuilder.listen(BlockBreakEvent::class.java, action = {
            if (config.isEnabled.value() && it.isDropItems) {
                it.isDropItems = false
                val toDrop = converter[it.block.type]?.let { str -> generateItemStacks(str) }
                toDrop?.forEach { stack ->
                    it.block.location.world.dropItem(it.block.location, stack)
                }
            }
        })
    }

    private fun generateItemStacks(str: String): List<ItemStack> =
        converter.toChars(str).mapNotNull { generateItemStack(it) }

    private fun generateItemStack(char: Char): ItemStack? {
        val customDataInt = converter.toCustomModelDataInt(char) ?: return null
        val item = ItemStack(Material.PAPER)
        item.editMeta {
            it.displayName(Component.text(char))
            it.setCustomModelData(customDataInt)
        }
        return item
    }

    override fun onEnable() {
        // Plugin startup logic
    }

    override fun onDisable() {
        // Plugin shutdown logic
        config.saveConfigIfPresent()
    }
}