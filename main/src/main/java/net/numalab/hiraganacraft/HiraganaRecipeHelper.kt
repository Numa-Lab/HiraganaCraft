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

        var shaped2By2 = 0
        var shaped3By3 = 0
        var exceeded = 0

        get2By2Recipes().also {
            shaped2By2 = it.size
        }.forEach {
            plugin.server.addRecipe(it)
            recipeManager.addRecipe(it)
        }

        get3By3Recipes().also {
            shaped3By3 = it.size
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

        println("Shaped2By2: $shaped2By2")
        println("Shaped3By3: $shaped3By3")
        println("Shaped: ${shaped3By3 + shaped2By2}")
        println("Exceeded: $exceeded")
        println("RecipeManager:${recipeManager.size()}")
        println("Total: ${shaped2By2 + shaped3By3 + exceeded}")
        println("Not Registered: ${converter.getAllEntries().size - (shaped3By3 + exceeded)}")
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
        recipeManager.addRecipe(generate3By3Recipe(config.superCrafterMaterial.value(), "すーぱーくらふたー", "super_crafter")!!)
    }

    private fun get2By2Recipes(): List<ShapedRecipe> {
        return converter.getAllEntries()
            .filter { it.value.length <= 4 }
            .mapNotNull {
                val material = converter[it.key] ?: return@mapNotNull null
                if (config.superCrafterMaterial.value() == material) {
                    return@mapNotNull null  // remove dummy block for super crafter
                }
                return@mapNotNull generate2By2Recipe(material, it.value, "${it.key}_2by2")  // 3*3のレシピIDとかぶらないように「_2by2」を付ける
            }
    }

    private fun generate2By2Recipe(to: Material, from: String, key: String): ShapedRecipe? {
        if (to == Material.AIR) {
            return null
        }

        val nameSpacedKey = NamespacedKey(plugin, key)
        val ingredients = plugin.generateItemStacks(from)
        val recipe = ShapedRecipe(nameSpacedKey, ItemStack(to, 1))
        val shape = generate2By2RecipeShape(ingredients).toTypedArray()
        if (shape.size > 9) {
            println("[ERROR][HiraganaRecipeHelper] ${to.name} is too long")
            return null
        }
        recipe.shape(*(shape))
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
     * 4. Split that string into array of strings at size with 2
     */
    private fun generate2By2RecipeShape(ingredients: List<ItemStack>): List<String> {
        val size = ingredients.size
        val shape = StringBuilder()
        val const = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        for (i in 0 until size) {
            shape.append(const[i])
        }
        for (i in size until 4) {
            shape.append(" ")
        }
        val shapeString = shape.toString()
        return shapeString.split(Regex("(?<=\\G.{2})")).filter { it.isNotEmpty() }
    }

    /**
     * 3 * 3のレシピを取得する
     */
    private fun get3By3Recipes(): List<ShapedRecipe> {
        return converter.getAllEntries()
            .filter { it.value.length <= 9 }
            .mapNotNull {
                val material = converter[it.key] ?: return@mapNotNull null
                if (config.superCrafterMaterial.value() == material) {
                    return@mapNotNull null  // remove dummy block for super crafter
                }
                return@mapNotNull generate3By3Recipe(material, it.value, it.key)
            }
    }

    private fun generate3By3Recipe(to: Material, from: String, key: String): ShapedRecipe? {
        if (to.isAir) {
            return null
        }

        val nameSpacedKey = NamespacedKey(plugin, key)
        val ingredients = plugin.generateItemStacks(from)
        val recipe = ShapedRecipe(nameSpacedKey, ItemStack(to, 1))
        val shape = generate3By3RecipeShape(ingredients).toTypedArray()
        if (shape.size > 9) {
            println("[ERROR][HiraganaRecipeHelper] ${to.name} is too long")
            return null
        }
        recipe.shape(*(shape))
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
    private fun generate3By3RecipeShape(ingredients: List<ItemStack>): List<String> {
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
