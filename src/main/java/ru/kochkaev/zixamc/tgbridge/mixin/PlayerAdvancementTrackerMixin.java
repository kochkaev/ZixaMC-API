package ru.kochkaev.zixamc.tgbridge.mixin;

import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.kochkaev.zixamc.tgbridge.chatSync.ChatSyncCustomEvents;

import java.lang.reflect.InvocationTargetException;

@Mixin(PlayerAdvancementTracker.class)
abstract class PlayerAdvancementTrackerMixin {
    @Shadow
    private ServerPlayerEntity owner;

    @Shadow private boolean dirty;

    @Inject(
            method = "grantCriterion"/*"method_12878"*/,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/advancement/AdvancementRewards;apply(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void grantCriterion(AdvancementEntry advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
//        final var display = (AdvancementDisplay) Advancement.class.getMethod("display"/*"method_686"*/).invoke(advancement);
//        ZixaMCTGBridge.Companion.getLogger().info(display.toString());
        final var _advancement = advancement.value();
        final var display = _advancement.display().orElse(null);
        if (display == null || !display.shouldAnnounceToChat()) {
            return;
        }
        final var frame = display.getFrame();
        if (frame == null) {
            return;
        }
        final var type = frame.name().toLowerCase();

//        final var toHoverableText = Advancement.class.getMethod("name"/*"method_684"*/);
//        final var name = (Text) toHoverableText.invoke(advancement);
        final var name = _advancement.name().get();
        ChatSyncCustomEvents.Companion.getADVANCEMENT_EARN_EVENT().invoker().onAdvancementEarn(owner, type, name);
    }
}
