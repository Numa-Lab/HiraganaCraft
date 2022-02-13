package net.numalab.hiraganacraft.recipe

import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe

/**
 * 9個の限界を突破するレシピ
 */
data class ExceededRecipe(val result: ItemStack, val input: String)