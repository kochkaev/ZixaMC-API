package ru.kochkaev.zixamc.tgbridge.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.kochkaev.zixamc.chatsync.ChatSyncCustomEvents;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Inject(method = "onDeath(Lnet/minecraft/entity/damage/DamageSource;)V", at = @At("HEAD"))
    private void onPlayerDeath(DamageSource damageSource, CallbackInfo ci) {
        ChatSyncCustomEvents.Companion.getPLAYER_DIE_EVENT().invoker().onPlayerDie((ServerPlayerEntity)(Object)this, damageSource.getDeathMessage((ServerPlayerEntity)(Object)this));
    }
}
