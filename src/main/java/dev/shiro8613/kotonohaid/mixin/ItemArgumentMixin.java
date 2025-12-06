package dev.shiro8613.kotonohaid.mixin;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@Mixin(ItemStackArgumentType.class)
public class ItemArgumentMixin {

    @Inject(method = "listSuggestions", at = @At("RETURN"), cancellable = true)
    public <S> void suggestion(CommandContext<S> context, SuggestionsBuilder builder, CallbackInfoReturnable<CompletableFuture<Suggestions>> cir) {
        CompletableFuture<Suggestions> before = cir.getReturnValue();
        try {
            Suggestions sug = before.get();
            String remaining = builder.getRemainingLowerCase();
            Registries.ITEM.stream()
                    .filter(i -> i.getName().getString().startsWith(remaining))
                    .map(i -> Registries.ITEM.getId(i).toString())
                    .forEach(builder::suggest);
            sug.getList().forEach(g -> builder.suggest(g.getText()));

            cir.setReturnValue(builder.buildFuture());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

    }
}
