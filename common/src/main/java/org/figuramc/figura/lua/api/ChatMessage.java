package org.figuramc.figura.lua.api;

import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.ducks.GuiMessageAccessor;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@LuaWhitelist
@LuaTypeDoc(
		value = "chat_message",
		name = "ChatMessage"
)
public record ChatMessage(Avatar owner, GuiMessage message) {
	@LuaWhitelist
	@LuaMethodDoc(value = "chat_message.get_added_time")
	public int getAddedTime() {
		return message.addedTime();
	}

	@LuaWhitelist
	@LuaMethodDoc(
			value = "chat_message.with_added_time",
			overloads = @LuaMethodOverload(
					argumentNames = "time",
					argumentTypes = String.class
			)
	)
	public ChatMessage withAddedTime(int addedTime) {
		return new ChatMessage(owner, new GuiMessage(addedTime, this.message.content(), null, this.message.tag()));
	}

	@LuaWhitelist
	@LuaMethodDoc(value = "chat_message.get_content_text")
	public String getContentText() {
		return message.content().getString();
	}

	@LuaWhitelist
	@LuaMethodDoc(value = "chat_message.get_content_json")
	public String getContentJson() {
		return Component.Serializer.toJson(message.content());
	}

	@LuaWhitelist
	@LuaMethodDoc(
			value = "chat_message.with_content",
			overloads = @LuaMethodOverload(
					argumentNames = "json",
					argumentTypes = String.class
			)
	)
	public ChatMessage withContent(String json) {
		return new ChatMessage(owner, new GuiMessage(this.message.addedTime(), TextUtils.tryParseJson(json), null, GuiMessageTag.chatModified(message.content().getString())));
	}
	
	@LuaWhitelist
	@LuaMethodDoc("chat_message.get_background_color")
	public Integer getBackgroundColor() {
		return ((GuiMessageAccessor) (Object) this.message).figura$getColor();
	}
	
	@LuaWhitelist
	public ChatMessage setBackgroundColor(int color) {
		((GuiMessageAccessor) (Object) this.message).figura$setColor(color);
		return this;
	}

	@LuaWhitelist
	@LuaMethodDoc("chat_message.get_indicator_color")
	public Integer getIndicatorColor() {
		return (Object) this.message.tag() instanceof GuiMessageTag tag ? tag.indicatorColor() : null;
	}

	@LuaWhitelist
	@LuaMethodDoc(
			value = "chat_message.with_indicator_color",
			overloads = @LuaMethodOverload(
					argumentNames = "color",
					argumentTypes = int.class
			)
	)
	public ChatMessage withIndicatorColor(int color) {
		GuiMessageTag tag = this.message.tag();
		if (tag == null) {
			tag = new GuiMessageTag(color, null, getName(owner), "Figura");
		} else {
			tag = new GuiMessageTag(color, tag.icon(), tag.text(), tag.logTag());
		}
		return new ChatMessage(owner, new GuiMessage(this.message.addedTime(), this.message.content(), this.message.signature(), tag));
	}

	@LuaWhitelist
	@LuaMethodDoc("chat_message.get_tag_text")
	public String getTagText() {
		return (Object) this.message.tag() instanceof GuiMessageTag tag? (Object) tag.text() instanceof Component c? c.getString() : null : null;
	}

	@LuaWhitelist
	@LuaMethodDoc("chat_message.get_tag_json")
	public String getTagJson() {
		return (Object) this.message.tag() instanceof GuiMessageTag tag? Component.Serializer.toJson(tag.text()) : null;
	}

	public ChatMessage withTagText(Component text) {
		GuiMessageTag tag  = this.message.tag();
		if (tag == null) {
			tag = new GuiMessageTag(getColor(owner), null, text, "Figura");
		} else {
			tag = new GuiMessageTag(tag.indicatorColor(), tag.icon(), text, tag.logTag());
		}
		return new ChatMessage(owner, new GuiMessage(this.message.addedTime(), this.message.content(), this.message.signature(), tag));
	}

	@LuaWhitelist
	@LuaMethodDoc(
			value = "chat_message.with_tag_text",
			overloads = @LuaMethodOverload(
					argumentNames = "json",
					argumentTypes = String.class
			)
	)
	public ChatMessage withTagText(String json) {
		return withTagText(TextUtils.tryParseJson(json));
	}

	@LuaWhitelist
	@LuaMethodDoc("chat_message.get_log_tag")
	public String getLogTag() {
		return (Object) this.message.tag() instanceof GuiMessageTag tag ? tag.logTag() : null;
	}

	@LuaWhitelist
	@LuaMethodDoc(
			value = "chat_message.with_log_tag",
			overloads = @LuaMethodOverload(
					argumentNames = "logTag",
					argumentTypes = String.class
			)
	)
	public ChatMessage withLogTag(String logTag) {
		GuiMessageTag tag = this.message.tag();
		if (tag == null) {
			tag = new GuiMessageTag(getColor(owner), null, getName(owner), logTag);
		} else {
			tag = new GuiMessageTag(tag.indicatorColor(), tag.icon(), tag.text(), logTag);
		}
		return new ChatMessage(owner, new GuiMessage(this.message.addedTime(), this.message.content(), this.message.signature(), tag));
	}

	@LuaWhitelist
	@LuaMethodDoc("chat_message.copy")
	public ChatMessage copy() {
		GuiMessageTag tag = this.message.tag();
		if(tag != null) tag = new GuiMessageTag(tag.indicatorColor(), tag.icon(), (Object) tag.text() instanceof Component c? c.copy() : null, tag.logTag());
		ChatMessage message = new ChatMessage(owner, new GuiMessage(this.message.addedTime(), (Object) this.message.content() instanceof Component c? c.copy() : null, null, tag));
		message.setBackgroundColor(getBackgroundColor());
		return message;
	}

	private static int getColor(Avatar owner) {
		return ColorUtils.rgbToInt(ColorUtils.userInputHex(owner.color, ColorUtils.Colors.AWESOME_BLUE.vec));
	}

	@NotNull private static Component getName(Avatar owner) {
		return Objects.requireNonNullElse(owner.luaRuntime.nameplate.CHAT.getJson(), Component.literal(owner.entityName));
	}

	@LuaWhitelist
	@LuaTypeDoc(value = "globals.messages", name = "messages")
	public static class Messages {
		Avatar owner;

		public Messages(Avatar avatar) {
			owner = avatar;
		}

		@LuaWhitelist
		@LuaMethodDoc(value = "messages.of")
		public ChatMessage of(Object object) {
			if (object instanceof ChatMessage m)
				return m.copy();
			ChatMessage message = new ChatMessage(owner, new GuiMessage(ClientAPI.getGameTime(), Component.empty(), null, null));
			if (object instanceof String s)
				message = message.withContent(s);
			message = message.withIndicatorColor(getColor(owner));
			message = message.withTagText(getName(owner));
			message = message.withLogTag("Figura");
			return message;
		}
	}
}
