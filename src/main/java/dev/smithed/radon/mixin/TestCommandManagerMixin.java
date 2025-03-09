package dev.smithed.radon.mixin;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.logging.LogUtils;
import net.minecraft.SharedConstants;
import net.minecraft.command.CommandExecutionContext;
import net.minecraft.command.ReturnValueConsumer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.Profilers;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CommandManager.class)
public class TestCommandManagerMixin {


    @Shadow @Final private static Logger LOGGER = LogUtils.getLogger();

    @Shadow private static ContextChain<ServerCommandSource> checkCommand(ParseResults<ServerCommandSource> parseResults, String command, ServerCommandSource source) { return null; }

    @Overwrite
    public void execute(ParseResults<ServerCommandSource> parseResults, String command) {
        ServerCommandSource serverCommandSource = parseResults.getContext().getSource();
        Profilers.get().push(() -> "/" + command);
        ContextChain<ServerCommandSource> contextChain = checkCommand(parseResults, command, serverCommandSource);

        try {
            if (contextChain != null) {
                CommandManager.callWithContext(serverCommandSource, (context) -> {
                    CommandExecutionContext.enqueueCommand(context, command, contextChain, serverCommandSource, ReturnValueConsumer.EMPTY);
                });
            }
        } catch (Exception var12) {
            System.out.println("FAILED command: " + command);
            System.err.println(var12.getMessage());
            var12.printStackTrace(System.err);

            MutableText mutableText = Text.literal(var12.getMessage() == null ? var12.getClass().getName() : var12.getMessage());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error("Command exception: /{}", command, var12);
                StackTraceElement[] stackTraceElements = var12.getStackTrace();

                for(int i = 0; i < Math.min(stackTraceElements.length, 3); ++i) {
                    mutableText.append("\n\n").append(stackTraceElements[i].getMethodName()).append("\n ").append(stackTraceElements[i].getFileName()).append(":").append(String.valueOf(stackTraceElements[i].getLineNumber()));
                }
            }

            serverCommandSource.sendError(Text.translatable("command.failed").styled((style) -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, mutableText))));
            if (SharedConstants.isDevelopment) {
                serverCommandSource.sendError(Text.literal(Util.getInnermostMessage(var12)));
                LOGGER.error("'/{}' threw an exception", command, var12);
            }
        } finally {
            Profilers.get().pop();
        }

    }
}
