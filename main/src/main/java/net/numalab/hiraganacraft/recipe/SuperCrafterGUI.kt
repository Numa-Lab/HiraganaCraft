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
import java.lang.Integer.max


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

    fun reopen() {
        player.closeInventory()
        player.openInventory(gui)
    }

    private fun onClick(e: InventoryClickEvent) {
        if (e.slot == 53) {
            // プレイヤーが出力スロットをクリックした場合
            // 強制的にインベントリに移動させる
            val current = e.currentItem
            if (current != null && current.type == Material.BARRIER) {
                e.isCancelled = true
            }
            if (current != null && current.type != Material.AIR && current.type != Material.BARRIER) {
                val clonedCurrent = current.clone()
                plugin.server.scheduler.runTaskLater(
                    plugin,
                    Runnable {
                        val toRemoveAmount = clonedCurrent.amount
                        (0..52).forEach {
                            val toModified = max((gui.getItem(it)?.amount ?: 0) - toRemoveAmount, 0)
                            if (toModified > 0) {
                                gui.setItem(it, gui.getItem(it)!!.apply { amount = toModified })
                            } else {
                                gui.setItem(it, null)
                            }
                        }

                        update()
                    }, 2
                )
            }
        }
        update()
    }

    private fun onDrag(e: InventoryDragEvent) {
        update()
    }

    private fun onMoveItem(e: InventoryMoveItemEvent) {
        update()
    }

    /**
     * 余ったアイテムを返却する
     */
    private fun onClose(e: InventoryCloseEvent) {
        close()
        val toDrop = e.player.inventory.addItem(*gui.contents[0..52].filterNotNull().toTypedArray()).values
        toDrop.forEach {
            e.player.world.dropItem(e.player.location, it)
        }
        gui.clear()
    }

    private fun update() {
        plugin.server.scheduler.runTaskLater(
            plugin,
            Runnable {
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
            }, 1
        )
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

        @EventHandler
        private fun onCloseInventory(event: InventoryCloseEvent) {
            getInventory(event)?.onClose(event)
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