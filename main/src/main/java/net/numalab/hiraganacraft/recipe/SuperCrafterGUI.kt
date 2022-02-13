package net.numalab.hiraganacraft.recipe

import net.kyori.adventure.text.Component
import net.numalab.hiraganacraft.HiraganaConverter
import net.numalab.hiraganacraft.Hiraganacraft
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.*
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack


/**
 * スーパークラフター
 * @note 一回の表示ごとに再生成するクラス
 */
class SuperCrafterGUI(
    private val player: Player,
    private val plugin: Hiraganacraft,
    private val recipeManager: RecipeManager = plugin.recipeManager,
    private val converter: HiraganaConverter = plugin.converter
) {
    private val gui = Bukkit.getServer().createInventory(player, 54, Component.text("スーパークラフター"))
    fun open() {
        SuperCrafterEventListener.getInstance(plugin).registerGUI(this)
        player.openInventory(gui)
    }

    fun close() {
        gui.close()
        SuperCrafterEventListener.getInstance(plugin).unregisterGUI(this)
    }

    private fun onClick(e: InventoryClickEvent) {
        update()
    }

    private fun onDrag(e: InventoryDragEvent) {
        update()
    }

    private fun onMoveItem(e: InventoryMoveItemEvent) {
        update()
    }

    private fun update() {
        val inputStr = buildString()
        val outputStacks = recipeManager.craftResult(inputStr)
        if (outputStacks.isEmpty()) {
            // None Recipe Matched
            gui.setItem(53, ItemStack(Material.BARRIER))
        } else if (outputStacks.size == 1) {
            // Single Result
            gui.setItem(53, outputStacks[0])
        } else {
            // Multiple Result
            // This Branch should not be reached
            gui.setItem(53, ItemStack(Material.BARRIER))
        }
    }

    private fun buildString(): String {
        var str = ""
        gui.contents.takeWhile { it != null }.forEach {
            converter.fromHiraganaCard(it)?.let { c ->
                str += c
            }
        }
        return str
    }


    private class SuperCrafterEventListener(plugin: Hiraganacraft) : org.bukkit.event.Listener {
        init {
            plugin.server.pluginManager.registerEvents(this, plugin)
        }

        companion object {
            private var instance: SuperCrafterEventListener? = null
            fun getInstance(plugin: Hiraganacraft): SuperCrafterEventListener {
                if (instance == null) {
                    instance = SuperCrafterEventListener(plugin)
                }
                return instance!!
            }
        }

        private val guis = mutableListOf<SuperCrafterGUI>()
        fun registerGUI(gui: SuperCrafterGUI) {
            guis.add(gui)
        }

        fun unregisterGUI(gui: SuperCrafterGUI) {
            guis.remove(gui)
        }

        @EventHandler
        private fun onInventoryClick(event: InventoryClickEvent) {
            getInventory(event)?.onClick(event)
        }

        @EventHandler
        private fun onInventoryMoveItemEvent(event: InventoryMoveItemEvent) {
            getInventory(event.source)?.onMoveItem(event)
        }

        @EventHandler
        private fun onInventoryDrag(event: InventoryDragEvent) {
            getInventory(event)?.onDrag(event)
        }

        private fun getInventory(e: InventoryEvent): SuperCrafterGUI? {
            return guis.find { it.gui == e.view.topInventory }
        }

        private fun getInventory(inventory: Inventory): SuperCrafterGUI? {
            return guis.find { it.gui == inventory }
        }
    }
}

private operator fun <T> Array<T>.get(intRange: IntRange): List<T> {
    return intRange.map { this[it] }
}