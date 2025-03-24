package ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync

import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

class ChatSyncCustomEvents {
    companion object {
        val ADVANCEMENT_EARN_EVENT = EventFactory.createArrayBacked(AdvancementEarn::class.java) { handlers ->
            AdvancementEarn { player, advancementType, advancementNameComponent ->
                for (handler in handlers) {
                    handler.onAdvancementEarn(player, advancementType, advancementNameComponent)
                }
            }
        }
        val PLAYER_DIE_EVENT = EventFactory.createArrayBacked(PlayerDie::class.java) { handlers ->
            PlayerDie { player, deathMessage ->
                for (handler in handlers) {
                    handler.onPlayerDie(player, deathMessage)
                }
            }
        }
    }

    fun interface AdvancementEarn {
        fun onAdvancementEarn(player: ServerPlayerEntity, advancementType: String, advancementNameComponent: Text): Unit
    }
    fun interface PlayerDie {
        fun onPlayerDie(player: ServerPlayerEntity, deathMessage: Text)
    }
}