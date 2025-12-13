package dev.shiro8613.kotonohaid.mixin;

import dev.shiro8613.kotonohaid.Kotonohaid;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatInputSuggestor.SuggestionWindow.class)
public class SuggestionWindowMixin {

    @Inject(method = "complete", at = @At("RETURN"))
    public void kotonoha$complete(CallbackInfo ci) {
        Kotonohaid.idSuggestion = false;
    }
}
