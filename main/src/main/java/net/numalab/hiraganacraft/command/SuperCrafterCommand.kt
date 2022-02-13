package net.numalab.hiraganacraft.command

import dev.kotx.flylib.command.Command
import net.numalab.hiraganacraft.Hiraganacraft
import net.numalab.hiraganacraft.recipe.SuperCrafterGUI

class SuperCrafterCommand : Command("supercrafter") {
    init {
        description("SuperCrafterを起動します")
        usage {
            stringArgument("Player", suggestion = {
                suggestAll(plugin.server.onlinePlayers.map { it.name })
            })

            executes {
                val playerName = typedArgs[0] as String
                val player = plugin.server.getPlayer(playerName)
                if (player != null) {
                    val plugin = plugin as? Hiraganacraft
                    if (plugin != null) {
                        SuperCrafterGUI(player, plugin).open()
                    } else {
                        fail("pluginの取得に失敗しました")
                    }
                } else {
                    sender.sendMessage("プレイヤーが見つかりません")
                }
            }
        }
    }
}