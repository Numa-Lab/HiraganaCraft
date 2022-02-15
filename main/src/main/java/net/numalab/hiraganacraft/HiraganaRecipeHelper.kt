package net.numalab.hiraganacraft

import net.numalab.hiraganacraft.recipe.ExceededRecipe
import net.numalab.hiraganacraft.recipe.RecipeManager
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Server
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.ShapedRecipe

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

        var shaped = 0
        var exceeded = 0

        getAllRecipes().also {
            shaped = it.size
        }.forEach {
            plugin.server.addRecipe(it)
            recipeManager.addRecipe(it)
        }

        getAllExceededRecipes().also {
            exceeded = it.size
        }.forEach {
            recipeManager.addRecipe(it)
        }

        registerSuperCrafterRecipe()

        println("Shaped: $shaped")
        println("Exceeded: $exceeded")
        println("RecipeManager:${recipeManager.size()}")
        println("Total: ${shaped + exceeded}")
        println("Not Registered: ${converter.getAllEntries().size - (shaped + exceeded)}")
        plugin.logger.info("Registering recipes...Complete!")
    }

    private fun registerSuperCrafterRecipe() {
        // 作業台*9 -> スーパークラフター
        val shaped =
            ShapedRecipe(NamespacedKey(plugin, "super_crafter"), ItemStack(config.superCrafterMaterial.value()))
        shaped.shape("AAA", "AAA", "AAA")
        shaped.setIngredient('A', Material.CRAFTING_TABLE)
        plugin.server.addRecipe(shaped)

        // ひらがなカード「すーぱーくらふたー」 -> スーパークラフター
        recipeManager.addRecipe(generateRecipe(config.superCrafterMaterial.value(), "すーぱーくらふたー","super_crafter")!!)
    }

    private fun getAllRecipes(): List<ShapedRecipe> {
        return converter.getAllEntries()
            .filter { it.value.length <= 9 }
            .mapNotNull {
                val material = converter[it.key] ?: return@mapNotNull null
                if (config.superCrafterMaterial.value() == material) {
                    return@mapNotNull null  // remove dummy block for super crafter
                }
                return@mapNotNull generateRecipe(material, it.value, it.key)
            }
    }

    private fun generateRecipe(to: Material, from: String, key: String): ShapedRecipe? {
        if (to.isAir) {
            return null
        }

        val nameSpacedKey = NamespacedKey(plugin, key)
        val ingredients = plugin.generateItemStacks(from)
        val recipe = ShapedRecipe(nameSpacedKey, ItemStack(to, 1))
        val shape = generateRecipeShape(ingredients).toTypedArray()
        if (shape.size > 9) {
            println("[ERROR][HiraganaRecipeHelper] ${to.name} is too long")
            return null
        }
        try {
            recipe.shape(*(shape))
        } catch (e: Exception) {
            e
        }
        val const = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        ingredients.forEachIndexed { index, itemStack ->
            recipe.setIngredient(const[index], itemStack)
        }
        return recipe
    }

    /**
     * Generate recipe shape
     * 1. Count the number of ingredients
     * 2. Generate string filled with 'A','B','C'.. for each ingredient
     * 3. Concat that strings into one string
     * 4. Split that string into array of strings at size with 3
     */
    private fun generateRecipeShape(ingredients: List<ItemStack>): List<String> {
        val size = ingredients.size
        val shape = StringBuilder()
        val const = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        for (i in 0 until size) {
            shape.append(const[i])
        }
        for (i in size until 9) {
            shape.append(" ")
        }
        val shapeString = shape.toString()
        return shapeString.split(Regex("(?<=\\G.{3})")).filter { it.isNotEmpty() }
    }

    private fun getAllExceededRecipes(): List<ExceededRecipe> {
        return converter.getAllEntries()
            .filter { it.value.length in 10..53 }
            .mapNotNull {
                val material = converter[it.key] ?: return@mapNotNull null
                if (config.superCrafterMaterial.value() == material) {
                    return@mapNotNull null  // remove dummy block for super crafter
                }
                return@mapNotNull generateExceededRecipe(material, converter.mapString(it.value))   // 小文字などを変換
            }
    }

    private fun generateExceededRecipe(to: Material, from: String): ExceededRecipe? {
        if (to.isAir) {
            return null
        }
        return ExceededRecipe(ItemStack(to), from)
    }
}

private fun Server.removeRecipe(it: Recipe) {
    if (it is Keyed) {
        removeRecipe(it.key)
    } else {
        println("[WARN] ${it.javaClass.simpleName} is not Keyed")
    }
}
