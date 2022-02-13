package net.numalab.hiraganacraft.recipe

import net.numalab.hiraganacraft.HiraganaConverter
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe

/**
 * Manage All Recipes
 */
class RecipeManager(val converter: HiraganaConverter) {
    private val exceededRecipes = mutableListOf<ExceededRecipe>()
    private val shapedRecipe = mutableListOf<ShapedRecipe>()
    fun addRecipe(recipe: ExceededRecipe) {
        exceededRecipes.add(recipe)
    }

    fun addRecipe(recipe: ShapedRecipe) {
        shapedRecipe.add(recipe)
    }

    fun removeRecipe(recipe: ExceededRecipe) {
        exceededRecipes.remove(recipe)
    }

    fun removeRecipe(recipe: ShapedRecipe) {
        shapedRecipe.remove(recipe)
    }

    fun size() = exceededRecipes.size + shapedRecipe.size

    /**
     * @return all matched Recipes
     */
    fun craftResult(str: String): List<ItemStack> {
        return if (str.length <= 9) {
            // Shaped Recipe
            shapedRecipe.filter { shapedRecipe1 ->
                str == shapedRecipe1.ingredientMap.values.filterNotNull().mapNotNull { converter.fromHiraganaCard(it) }
                    .joinToString("")
            }.map { it.result.clone() }
        } else {
            // Exceeded Recipe
            exceededRecipes.filter { it.input == str }.map { it.result.clone() }
        }
    }
}