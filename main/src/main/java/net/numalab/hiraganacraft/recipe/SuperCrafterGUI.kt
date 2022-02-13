package net.numalab.hiraganacraft.recipe

import net.kyori.adventure.text.Component
import net.numalab.hiraganacraft.Hiraganacraft
import org.bukkit.Bukkit
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
    private val recipeManager: RecipeManager = plugin.recipeManager
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
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            update()
        }, 1)
        if (e.slot == 53) {
            doCraft(e)
        }
    }

    private fun onDrag(e: InventoryDragEvent) {
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            update()
        }, 1)
    }

    private fun onMoveItem(e: InventoryMoveItemEvent) {
    }

    /**
     * クラフト処理
     */
    private fun doCraft(e: InventoryClickEvent) {
        val input = gui.contents[0..52].filterNotNull()
        val outputList = recipeManager.craftResult(*input.toTypedArray())
        if (outputList.size != 1) {
            println("[SuperCrafter][ERROR]複数のレシピが登録されています")
            return
        }

        val output = outputList.first()

        if (e.currentItem != null && output.type == e.currentItem!!.type) {
            // もともとアイテムがあった、かつ、そのアイテムがレシピにマッチする場合
            val exceededRecipe =
                recipeManager.matchedExceededRecipe(*input.toTypedArray())
                    .filter { it.result.type == e.currentItem!!.type }
            val shapelessRecipe =
                recipeManager.matchedShapelessRecipe(*input.toTypedArray())
                    .filter { it.result.type == e.currentItem!!.type }
            if (exceededRecipe.size +
                shapelessRecipe.size != 1
            ) {
                // レシピが複数マッチしていて判定が不可能
                println("[SuperCrafter][ERROR] レシピが複数マッチしていて判定が不可能")
            } else {
                // 1つのレシピにマッチした場合
                val out = if (exceededRecipe.firstOrNull() != null) {
                    val recipe = exceededRecipe.first()
                    minus(input, recipe.input.map { it.clone().also { s -> s.amount = s.amount * output.amount } })
                } else if (shapelessRecipe.firstOrNull() != null) {
                    val recipe = shapelessRecipe.first()
                    minus(
                        input,
                        recipe.ingredientList.map { it.clone().also { s -> s.amount = s.amount * output.amount } })
                } else {
                    throw IllegalStateException("[SuperCrafter][ERROR] This Branch must be not reachable")
                }
                plugin.server.scheduler.runTaskLater(plugin, Runnable {
                    gui.clear()
                    gui.addItem(*out.toTypedArray())
                    gui.close()
                    player.openInventory(gui)
                }, 2)
            }
        }
    }

    private fun update() {
        val input = gui.contents[0..52].filterNotNull()
        val output = recipeManager.craftResult(*input.toTypedArray())
        if (output.isEmpty()) {
            gui.setItem(53, null)
        } else if (output.size == 1) {
            gui.setItem(53, output.first())
        } else {
            println("[SuperCrafterGUI] Recipe is duplicated for Input:[${input.joinToString(",")}]")
            gui.setItem(53, null)
        }
    }

    /**
     * To minus [minus] from [input],considering the amount and type of each item
     */
    private fun minus(input: List<ItemStack>, minus: List<ItemStack>): List<ItemStack> {
        var result = mutableListOf(*input.toTypedArray())
        minus.forEach { toMinus ->
            result = minus(result, toMinus).toMutableList()
        }
        return result
    }

    private fun minus(input: List<ItemStack>, minus: ItemStack): List<ItemStack> {
        var toMinus = minus.amount
        input.forEach {
            if (it.type == minus.type) {
                if (toMinus > it.amount) {
                    toMinus -= it.amount
                    it.amount = 0
                } else {
                    it.amount -= toMinus
                    toMinus = 0
                }
            }
        }

        if (toMinus > 0) {
            throw IllegalStateException("[SuperCrafter] Not enough item to minus")
        }
        return input.filter { it.amount > 0 }
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