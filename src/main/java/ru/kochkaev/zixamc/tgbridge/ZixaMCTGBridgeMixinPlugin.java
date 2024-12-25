package ru.kochkaev.zixamc.tgbridge;

import com.google.common.collect.ImmutableMap;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ZixaMCTGBridgeMixinPlugin implements IMixinConfigPlugin {

    private static final Boolean isEasyAuthLoaded;
    static {
        isEasyAuthLoaded = FabricLoader.getInstance().isModLoaded("easyauth");
    }

    private static final Map<String, Boolean> CONDITIONS = ImmutableMap.of(
            "PlayerAuthMixin", isEasyAuthLoaded
    );

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        var className = mixinClassName.replace("ru.kochkaev.zixamc.tgbridge.mixin.", "");
        return CONDITIONS.getOrDefault(className, true);
    }

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
