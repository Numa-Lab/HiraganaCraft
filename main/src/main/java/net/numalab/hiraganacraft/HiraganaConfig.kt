package net.numalab.hiraganacraft

import net.kunmc.lab.configlib.BaseConfig
import net.kunmc.lab.configlib.value.*
import org.bukkit.Material
import org.bukkit.plugin.Plugin

class HiraganaConfig(plugin: Plugin) : BaseConfig(plugin) {
    val isEnabled = BooleanValue(false)

    // ドロップする確率(%)
    val dropRate = DoubleValue(50.0)

    // スーパークラフターの代替ブロック
    val superCrafterMaterial = MaterialValue(Material.FLETCHING_TABLE)

    // 最終アイテムの名前
    val finalname = StringValue("五十音表")
    // 最終アイテムのlore
    val finalLore = StringListValue()

    // レシピを削除するアイテム名一覧
    private val deleteRecipes = StringListValue()
    fun getDeleteRecipes(): List<Material> {
        return deleteRecipes.value().mapNotNull { Material.getMaterial(it) }
    }

    fun setDeleteRecipes(materials: List<Material>) {
        deleteRecipes.value(materials.map { it.name })
    }
}