package net.numalab.hiraganacraft.command

import dev.kotx.flylib.command.Command
import net.numalab.hiraganacraft.HiraganaConfig


class MainCommand(private val config: HiraganaConfig, vararg child: Command) : Command("hg") {
    init {
        children(*child)
        description("This is main command of HiraganaCraft")
        usage {
            selectionArgument("ON/OFF", listOf("ON", "OFF"))
            executes {
                val bool = (this.typedArgs[0] as String).equals("ON", true)

                this@MainCommand.config.isEnabled.value(bool)
                success(
                    "${
                        if (bool) {
                            "ON"
                        } else {
                            "OFF"
                        }
                    }に変更しました"
                )
            }
        }
    }
}