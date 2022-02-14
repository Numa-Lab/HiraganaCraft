package net.numalab.hiraganacraft

import net.numalab.hiraganacraft.recipe.SuperCrafterGUI
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class SuperCrafterOpener(private val plugin: Hiraganacraft, private val config: HiraganaConfig = plugin.config) :
    Listener {
    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler
    fun onClickBlock(event: PlayerInteractEvent) {
        if (event.action == Action.RIGHT_CLICK_BLOCK) {
            val block = event.clickedBlock
            if (block != null) {
                val blockType = block.type
                if (config.superCrafterMaterial.value() == blockType) {
                    val player = event.player
                    SuperCrafterGUI(player, plugin).open()
                }
            }
        }
    }
}