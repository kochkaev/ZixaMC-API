package ru.kochkaev.zixamc.tgbridge.chatSync

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
    }

    fun interface AdvancementEarn {
        fun onAdvancementEarn(player: ServerPlayerEntity, advancementType: String, advancementNameComponent: Text): Unit
    }
}