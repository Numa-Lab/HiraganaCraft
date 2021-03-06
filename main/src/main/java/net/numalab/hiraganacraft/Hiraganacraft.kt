package net.numalab.hiraganacraft

import dev.kotx.flylib.FlyLibBuilder
import dev.kotx.flylib.flyLib
import net.kunmc.lab.configlib.ConfigCommandBuilder
import net.kyori.adventure.text.Component
import net.numalab.hiraganacraft.command.MainCommand
import net.numalab.hiraganacraft.command.SuperCrafterCommand
import net.numalab.hiraganacraft.recipe.RecipeManager
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityEvent
import org.bukkit.event.entity.ExplosionPrimeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import java.awt.TextComponent
import kotlin.random.Random
import kotlin.streams.toList

class Hiraganacraft : JavaPlugin() {
    val config = HiraganaConfig(this).also {
        it.saveConfigIfAbsent()
        it.loadConfig()
    }

    val converter = HiraganaConverter(this.getTextResource("ja_jp.json")!!)
    val recipeManager = RecipeManager(converter)
    private val recipeHelper = HiraganaRecipeHelper(this, converter, config, recipeManager)

    init {
        flyLib {
            command(MainCommand(config, ConfigCommandBuilder(config).build(), SuperCrafterCommand()))
            listens(this)
        }
    }

    private fun listens(flyLibBuilder: FlyLibBuilder) {
        // ブロック破壊時にひらがなカードが落ちるように
        flyLibBuilder.listen(BlockBreakEvent::class.java, action = {
            if (config.isEnabled.value() && it.isDropItems) {
                it.isDropItems = false
                val toDrop =
                    getNameReplaced(it.block.type)?.let { str -> generateItemStacks(str).reducedBy(config.dropRate.value()) }
                toDrop?.forEach { stack ->
                    it.block.location.world.dropItem(it.block.location, stack)
                }
            }
        })

        flyLibBuilder.listen(ExplosionPrimeEvent::class.java, action = {
            val items = it.entity.location.getNearbyEntitiesByType(Item::class.java, it.radius.toDouble())
            val itemStacks = items.map { i -> i.itemStack }.filter { s ->
                !isGeneratedItemStack(s)
            }
            val toDrop = itemStacks.map { s -> intoHiraganaCards(s) }.flatten().reducedBy(config.dropRate.value())
            val location = it.entity.location.clone()
            server.scheduler.runTaskLater(this@Hiraganacraft, Runnable {
                toDrop.forEach { stack ->
                    location.world.dropItem(location, stack)
                }
            }, 1)
        })
    }

    /**
     * スーパークラフターの時は、「すーぱーくらふたー」を返す
     */
    private fun getNameReplaced(material: Material): String? {
        return if (config.superCrafterMaterial.value() == material) {
            "すーぱーくらふたー"
        } else {
            converter[material]
        }
    }

    /**
     * @return converted item stack into hiragana cards
     */
    private fun intoHiraganaCards(itemStack: ItemStack): List<ItemStack> {
        val converted = getNameReplaced(itemStack.type) ?: return listOf()
        return generateItemStacks(converted).onEach { it.amount = itemStack.amount }
    }

    fun generateItemStacks(str: String): List<ItemStack> =
        converter.toChars(str).mapNotNull { generateItemStack(it) }

    private fun generateItemStack(char: Char): ItemStack? {
        val customDataInt = converter.toCustomModelDataInt(char) ?: return null
        val item = ItemStack(Material.PAPER)
        item.editMeta {
            it.displayName(Component.text(char))
            it.setCustomModelData(customDataInt)
        }
        markItemStack(item)
        return item
    }

    /**
     * @return true if the item is generated by Hiraganacraft
     */
    private fun isGeneratedItemStack(itemStack: ItemStack): Boolean {
        return itemStack.type == Material.PAPER && isMarkedItemStack(itemStack)
    }

    // このアイテムは自動生成されたものであることを示す
    private val nameKey = NamespacedKey(this, "itemmarker")
    private fun markItemStack(itemStack: ItemStack) {
        itemStack.editMeta {
            it.persistentDataContainer.set(nameKey, PersistentDataType.INTEGER, 1)
        }
    }

    private fun isMarkedItemStack(itemStack: ItemStack): Boolean {
        val meta = itemStack.itemMeta
        return meta.persistentDataContainer.has(
            nameKey,
            PersistentDataType.INTEGER
        ) && meta.persistentDataContainer.get(nameKey, PersistentDataType.INTEGER) == 1
    }

    override fun onEnable() {
        // Plugin startup logic
        recipeHelper.recipeAll()
        SuperCrafterOpener(this)
        HiraganaCraftResolver(this, converter, recipeManager)
    }

    override fun onDisable() {
        // Plugin shutdown logic
        config.saveConfigIfPresent()
    }
}

fun <T> List<T>.reducedBy(percentage: Double): List<T> {
    return this.toMutableList().filter { Random.nextDouble() * 100.0 < percentage }
}