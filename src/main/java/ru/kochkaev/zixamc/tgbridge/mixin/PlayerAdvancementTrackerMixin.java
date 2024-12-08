package ru.kochkaev.zixamc.tgbridge.mixin;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.kochkaev.zixamc.tgbridge.chatSync.CustomEvents;

import java.lang.reflect.InvocationTargetException;

@Mixin(PlayerAdvancementTracker.class)
abstract class PlayerAdvancementTrackerMixin {
    @Shadow
    private ServerPlayerEntity owner;

    @Dynamic
    @Inject(
            method = "grantCriterion",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/advancement/AdvancementRewards;apply(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void grantCriterion(Advancement advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        final var display = (AdvancementDisplay) Advancement.class.getMethod("display").invoke(advancement);
        if (display == null || !display.shouldAnnounceToChat()) {
            return;
        }
        final var frame = display.getFrame();
        if (frame == null) {
            return;
        }
        final var type = frame.name().toLowerCase();

        final var toHoverableText = Advancement.class.getMethod("name");
        final var name = (Text) toHoverableText.invoke(advancement);
        CustomEvents.Companion.getADVANCEMENT_EARN_EVENT().invoker().onAdvancementEarn(owner, type, name);
    }
}
