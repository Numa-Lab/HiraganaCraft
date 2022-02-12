package net.numalab.hiraganacraft

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapelessRecipe

class HiraganaRecipeHelper(private val plugin: Hiraganacraft, private val converter: HiraganaConverter) {
    fun registerAllRecipes() {
        plugin.logger.info("Registering recipes...")
        getAllRecipes().forEach {
            plugin.server.addRecipe(it)
        }
        plugin.logger.info("Registering recipes...Complete!")
    }

    private fun getAllRecipes(): List<ShapelessRecipe> {
        return converter.getAllEntries()
            .filter { it.value.length <= 9 }
            .mapNotNull {
                val material = findMaterial(it.key) ?: return@mapNotNull null
                return@mapNotNull generateRecipe(material, it.value, it.key)
            }
    }

    private fun generateRecipe(to: Material, from: String, key: String): ShapelessRecipe? {
        if (to.isAir){
            return null
        }
        val recipe = ShapelessRecipe(NamespacedKey(plugin, "recipe-${key}"), ItemStack(to))
        val ingredients = plugin.generateItemStacks(from)
        ingredients.forEach {
            recipe.addIngredient(it)
        }
        return recipe
    }

    private fun findMaterial(translationKey: String): Material? {
        return Material.values().find { it.translationKey == translationKey }
    }
}