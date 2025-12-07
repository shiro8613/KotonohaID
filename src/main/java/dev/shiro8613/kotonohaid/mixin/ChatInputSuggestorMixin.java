package dev.shiro8613.kotonohaid.mixin;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
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
import java.util.regex.Pattern;

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
    private final Pattern idPattern = Pattern.compile("^[a-z0-9._-]+:[a-z0-9/._-]+$");

    @Unique
    private boolean idSuggestion = false;


    @Inject(method = "refresh", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/CommandDispatcher;getCompletionSuggestions(Lcom/mojang/brigadier/ParseResults;I)Ljava/util/concurrent/CompletableFuture;", shift = At.Shift.AFTER), cancellable = true)
    public void kotonoha$refresh(CallbackInfo ci) {
        if (textField.getText().endsWith(" ")) {
            idSuggestion = false;
        }

        try {
            if (pendingSuggestions == null) {
                return;
            }
            Suggestions sug = pendingSuggestions.get();
            if (sug.getList().stream().anyMatch(s -> idPattern.matcher(s.getText()).matches())) {
                idSuggestion = true;
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
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


