package net.numalab.hiraganacraft

import net.numalab.hiraganacraft.recipe.ExceededRecipe
import net.numalab.hiraganacraft.recipe.RecipeManager
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Server
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.ShapelessRecipe

class HiraganaRecipeHelper(
    private val plugin: Hiraganacraft,
    private val converter: HiraganaConverter,
    private val config: HiraganaConfig,
    private val recipeManager: RecipeManager
) {

    fun recipeAll() {
        deleteConfiguredRecipe()
        registerAllRecipes()
    }

    private fun deleteConfiguredRecipe() {
        val toRemove = config.getDeleteRecipes()
        val toRemoveRecipe = mutableListOf<Recipe>()
        plugin.server.recipeIterator().forEach {
            if (toRemove.contains(it.result.type)) {
                toRemoveRecipe.add(it)
            }
        }

        toRemoveRecipe.forEach {
            plugin.server.removeRecipe(it)
        }
    }

    private fun registerAllRecipes() {
        plugin.logger.info("Registering recipes...")

        var shapeless = 0
        var exceeded = 0

        getAllRecipes().also {
            shapeless = it.size
        }.forEach {
            plugin.server.addRecipe(it)
            recipeManager.addRecipe(it)
        }

        getAllExceededRecipes().also {
            exceeded = it.size
        }.forEach {
            recipeManager.addRecipe(it)
        }

        println("Shapeless: $shapeless")
        println("Exceeded: $exceeded")
        println("RecipeManager:${recipeManager.size()}")
        println("Total: ${shapeless + exceeded}")
        println("Not Registered: ${converter.getAllEntries().size - (shapeless + exceeded)}")
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
        if (to.isAir) {
            return null
        }
        val recipe = ShapelessRecipe(NamespacedKey(plugin, "recipe-${key}"), ItemStack(to))
        val ingredients = plugin.generateItemStacks(from)
        ingredients.forEach {
            recipe.addIngredient(it)
        }
        return recipe
    }

    private fun getAllExceededRecipes(): List<ExceededRecipe> {
        return converter.getAllEntries()
            .filter { it.value.length in 10..53 }
            .mapNotNull {
                val material = findMaterial(it.key) ?: return@mapNotNull null
                return@mapNotNull generateExceededRecipe(material, it.value)
            }
    }

    private fun generateExceededRecipe(to: Material, from: String): ExceededRecipe? {
        if (to.isAir) {
            return null
        }
        val ingredients = plugin.generateItemStacks(from)
        return ExceededRecipe(ItemStack(to), *ingredients.toTypedArray())
    }

    private fun findMaterial(translationKey: String): Material? {
        return Material.values().find { it.translationKey == translationKey }
    }
}

private fun Server.removeRecipe(it: Recipe) {
    if (it is Keyed) {
        removeRecipe(it.key)
    } else {
        println("[WARN] ${it.javaClass.simpleName} is not Keyed")
    }
}
