package dev.shiro8613.kotonohaid.mixin;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Mixin(ChatInputSuggestor.class)
public abstract class ChatInputSuggestorMixin {
    @Shadow
    @Nullable
    private CompletableFuture<Suggestions> pendingSuggestions;
    @Shadow
    @Final
    TextFieldWidget textField;

    @Shadow protected abstract void showCommandSuggestions();

    @Unique
    private boolean idSuggestion = false;

    @Unique
    private static final Logger LOGGER = LogUtils.getLogger();

    @Inject(method = "clearWindow", at = @At("HEAD"))
    public void kotonoha$clearWindow(CallbackInfo ci) {
        idSuggestion = false;
    }

    @Inject(method = "refresh", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/CommandDispatcher;getCompletionSuggestions(Lcom/mojang/brigadier/ParseResults;I)Ljava/util/concurrent/CompletableFuture;", shift = At.Shift.AFTER), cancellable = true)
    public void kotonoha$refresh(CallbackInfo ci) {
        if (textField.getText().endsWith(" ")) {
            idSuggestion = false;
        }

        try {
            if (pendingSuggestions == null) {
                return;
            }
            if (!pendingSuggestions.isDone()) {
                return;
            }

            Suggestions sug = pendingSuggestions.get(2, TimeUnit.SECONDS);
            if (sug.getList().stream().anyMatch(s -> {
                Identifier id = Identifier.tryParse(s.getText());
                if (id == null) {
                    return false;
                }
                return Registries.ITEM.containsId(id);
            })) {
                idSuggestion = true;
            }
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.warn("[KotonohaID] except", e);
            return;
        } catch (TimeoutException e) {
            return;
        }

        if (idSuggestion) {
            int lastSpaceIndex = textField.getText().lastIndexOf(' ');
            if (lastSpaceIndex != -1) {
                final String truncatedInput = textField.getText().substring(0, textField.getCursor());
                final String searchText = textField.getText().substring(lastSpaceIndex + 1);
                SuggestionsBuilder builder = new SuggestionsBuilder(truncatedInput, lastSpaceIndex + 1);
                List<String> sug = Registries.ITEM.stream()
                        .filter(t -> t.getName().getString().startsWith(searchText))
                        .map(Registries.ITEM::getId)
                        .map(Identifier::toString)
                        .toList();

                if (!sug.isEmpty()) {
                    sug.forEach(builder::suggest);
                    pendingSuggestions.obtrudeValue(builder.build());
                    showCommandSuggestions();
                    ci.cancel();
                }
            }
        }
    }
}


