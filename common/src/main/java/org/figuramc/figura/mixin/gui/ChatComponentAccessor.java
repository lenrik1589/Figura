package org.figuramc.figura.mixin.gui;

import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(ChatComponent.class)
public interface ChatComponentAccessor {

    @Intrinsic
    @Accessor("allMessages")
    List<GuiMessage> getAllMessages();

    @Intrinsic
    @Accessor("trimmedMessages")
    List<GuiMessage.Line> getTrimmedMessages();

    @Intrinsic
    @Accessor("chatScrollbarPos")
    int getScrollbarPos();
    
    @Intrinsic
    @Invoker
    void invokeLogChatMessage(Component message, @Nullable GuiMessageTag tag);
}
