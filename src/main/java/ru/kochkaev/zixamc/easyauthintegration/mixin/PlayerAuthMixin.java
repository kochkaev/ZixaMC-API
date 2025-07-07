package ru.kochkaev.zixamc.easyauthintegration.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.kochkaev.zixamc.easyauthintegration.EasyAuthCustomEvents;

@Mixin({ServerPlayerEntity.class})
public class PlayerAuthMixin {

    @Dynamic
    @Inject(
            method = "easyAuth$setAuthenticated(Z)V",
            at = {@At("HEAD")}
    )
    public void invokeOnUpdatePlayerAuthenticated(boolean authenticated, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        EasyAuthCustomEvents.Companion.getUPDATE_PLAYER_AUTHENTICATED_EVENT().invoker().onUpdatePlayerAuthenticated(authenticated, player);
    }
}
