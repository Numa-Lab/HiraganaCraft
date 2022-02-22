package net.numalab.hiraganacraft

import net.numalab.hiraganacraft.recipe.RecipeManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareItemCraftEvent

/**
 * Bukkitが反転不可のレシピを登録させてくれないので強引に解決
 */
class HiraganaCraftResolver(
    plugin: Hiraganacraft,
    private val converter: HiraganaConverter,
    val recipeManager: RecipeManager
) : Listener {
    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler
    fun onPrepare(e: PrepareItemCraftEvent) {
        if (e.recipe == null) return
        if (e.inventory.matrix[0] != null && converter.fromHiraganaCard(e.inventory.matrix[0]) != null) {
            // 1つめがひらがなカード
            val str = e.inventory.matrix.map {
                if (it == null) return@map " "
                converter.fromHiraganaCard(it) ?: " "
            }.joinToString("").trimEnd()

            if (str.contains(" ")) {
                // 異物混入
                e.inventory.result = null
            } else {
                val result = recipeManager.craftResult(str)
                e.inventory.result = result.getOrNull(0)
            }
        }
    }
}