package net.numalab.hiraganacraft.recipe

import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe

/**
 * 9個の限界を突破するレシピ
 */
class ExceededRecipe(val result: ItemStack, vararg val input: ItemStack) {
    /**
     * @return if [actualInput] is matched with [input]
     */
    fun isMatch(vararg actualInput: ItemStack) = craftResult(*actualInput) != null

    /**
     * @return craftedResult
     * @note this method ignores item stack amount
     */
    fun craftResult(vararg actualInput: ItemStack): ItemStack? {
        val resultAmount = input.minOfOrNull {
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

        return result.clone().apply { amount = resultAmount }
    }
}