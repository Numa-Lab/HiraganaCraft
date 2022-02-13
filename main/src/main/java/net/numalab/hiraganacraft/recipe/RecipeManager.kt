package net.numalab.hiraganacraft.recipe

import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapelessRecipe
import java.lang.Integer.max

/**
 * Manage All Recipes
 */
class RecipeManager {
    private val exceededRecipes = mutableListOf<ExceededRecipe>()
    private val shapelessRecipes = mutableListOf<ShapelessRecipe>()
    fun addRecipe(recipe: ExceededRecipe) {
        exceededRecipes.add(recipe)
    }

    fun addRecipe(recipe: ShapelessRecipe) {
        shapelessRecipes.add(recipe)
    }

    fun removeRecipe(recipe: ExceededRecipe) {
        exceededRecipes.remove(recipe)
    }

    fun removeRecipe(recipe: ShapelessRecipe) {
        shapelessRecipes.remove(recipe)
    }

    fun size() = exceededRecipes.size + shapelessRecipes.size

    /**
     * @return all matched Recipes
     */
    fun craftResult(vararg actualInput: ItemStack): List<ItemStack> {
        val ex = exceededRecipes
            .mapNotNull {
                val result = it.craftResult(*actualInput) ?: return@mapNotNull null
                Pair(result, it.input.size)
            }
        val sh = shapelessRecipes
            .mapNotNull {
                val result = it.craftResult(*actualInput) ?: return@mapNotNull null
                Pair(result, it.ingredientList.size)
            }
        if (ex.isEmpty()) {
            if (sh.isEmpty()) return emptyList()
            else {
                val shM = sh.maxOf { it.second }
                return sh.filter { it.second == shM }.map { it.first }
            }
        } else {
            val shM = sh.maxOf { it.second }
            val exM = ex.maxOf { it.second }
            val max = max(shM, exM)
            return ex.filter { it.second == max }.map { it.first } +
                    sh.filter { it.second == max }.map { it.first }
        }
    }

    fun matchedExceededRecipe(vararg actualInput: ItemStack): List<ExceededRecipe> =
        exceededRecipes.filter { it.craftResult(*actualInput) != null }

    fun matchedShapelessRecipe(vararg actualInput: ItemStack): List<ShapelessRecipe> =
        shapelessRecipes.filter { it.craftResult(*actualInput) != null }

    private fun ShapelessRecipe.craftResult(vararg actualInput: ItemStack): ItemStack? {
        val resultAmount = ingredientList.minOfOrNull {
            val acAmount = actualInput.sumOf { ac ->
                if (it.isSimilar(ac)) {
                    ac.amount
                } else {
                    0
                }
            }
            acAmount / it.amount
        } ?: 0

        if (resultAmount == 0) return null

        return result.clone().apply { amount = resultAmount * result.amount }
    }
}