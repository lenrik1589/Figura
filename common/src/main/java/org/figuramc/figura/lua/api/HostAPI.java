package org.figuramc.figura.lua.api;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.ducks.GuiMessageAccessor;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.api.entity.EntityAPI;
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.figuramc.figura.lua.docs.LuaFieldDoc;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.mixin.LivingEntityAccessor;
import org.figuramc.figura.mixin.gui.ChatComponentAccessor;
import org.figuramc.figura.mixin.gui.ChatScreenAccessor;
import org.figuramc.figura.model.rendering.texture.FiguraTexture;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.LuaUtils;
import org.figuramc.figura.utils.TextUtils;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;

import java.util.*;

@LuaWhitelist
@LuaTypeDoc(
        name = "HostAPI",
        value = "host"
)
public class HostAPI {

    private final Avatar owner;
    private final boolean isHost;
    private final Minecraft minecraft;

    @LuaWhitelist
    @LuaFieldDoc("host.unlock_cursor")
    public boolean unlockCursor = false;
    public Integer chatColor;

    public HostAPI(Avatar owner) {
        this.owner = owner;
        this.minecraft = Minecraft.getInstance();
        this.isHost = owner.isHost;
    }

    @LuaWhitelist
    @LuaMethodDoc("host.is_host")
    public boolean isHost() {
        return isHost;
    }

    @LuaWhitelist
    @LuaMethodDoc(value = "host.is_cursor_unlocked")
    public boolean isCursorUnlocked() {
        return unlockCursor;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "boolean"
            ),
            value = "host.set_unlock_cursor")
    public HostAPI setUnlockCursor(boolean bool) {
        unlockCursor = bool;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "timesData"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Integer.class, Integer.class, Integer.class},
                            argumentNames = {"fadeInTime", "stayTime", "fadeOutTime"}
                    )
            },
            aliases = "titleTimes",
            value = "host.set_title_times"
    )
    public HostAPI setTitleTimes(Object x, Double y, Double z) {
        if (!isHost()) return this;
        FiguraVec3 times = LuaUtils.parseVec3("setTitleTimes", x, y, z);
        this.minecraft.gui.setTimes((int) times.x, (int) times.y, (int) times.z);
        return this;
    }

    @LuaWhitelist
    public HostAPI titleTimes(Object x, Double y, Double z) {
        return setTitleTimes(x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc("host.clear_title")
    public HostAPI clearTitle() {
        if (isHost())
            this.minecraft.gui.clear();
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "text"
            ),
            aliases = "title",
            value = "host.set_title"
    )
    public HostAPI setTitle(@LuaNotNil String text) {
        if (isHost())
            this.minecraft.gui.setTitle(TextUtils.tryParseJson(text));
        return this;
    }

    @LuaWhitelist
    public HostAPI title(@LuaNotNil String text) {
        return setTitle(text);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "text"
            ),
            aliases = "subtitle",
            value = "host.set_subtitle"
    )
    public HostAPI setSubtitle(@LuaNotNil String text) {
        if (isHost())
            this.minecraft.gui.setSubtitle(TextUtils.tryParseJson(text));
        return this;
    }

    @LuaWhitelist
    public HostAPI subtitle(@LuaNotNil String text) {
        return setSubtitle(text);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "text"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, boolean.class},
                            argumentNames = {"text", "animated"}
                    )
            },
            aliases = "actionbar",
            value = "host.set_actionbar"
    )
    public HostAPI setActionbar(@LuaNotNil String text, boolean animated) {
        if (isHost())
            this.minecraft.gui.setOverlayMessage(TextUtils.tryParseJson(text), animated);
        return this;
    }

    @LuaWhitelist
    public HostAPI actionbar(@LuaNotNil String text, boolean animated) {
        return setActionbar(text, animated);
    }

    private static final HashMap<Integer, String> boardNames = new HashMap<>() {{
       put(0, "list");
       put(1, "sidebar");
       put(2, "below_name");
        for (int i = 0; i < 16; i++)
            put(3 + i, "sidebar_team_" + ChatFormatting.getById(i).getSerializedName());
    }};

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = Boolean.class,
                            argumentNames = "jsonNames"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Boolean.class, Integer.class},
                            argumentNames = {"jsonNames", "limit"}
                    )
            },
            value = "host.get_scoreboards"
    )
    public HashMap<String, Map<String, Integer>> getScoreboards(boolean jsonNames, Integer limit) {
        if (!isHost() || this.minecraft.level == null)
            return null;
        HashMap<String, Map<String, Integer>> map = new HashMap<>();
        Scoreboard scoreboard = this.minecraft.level.getScoreboard();
        for (int i = 0; i < 19; i ++) {
            Objective objective = scoreboard.getDisplayObjective(i);
            Collection<Score> scores = scoreboard.getPlayerScores(objective);
            if (scores.isEmpty())
                continue;
            HashMap<String, Integer> objectiveMap = new HashMap<>();
            int toSkip = limit == null ? 15 : limit;
            toSkip = Math.min(toSkip, scores.size());
            toSkip = limit == null || limit > 0 ? scores.size() - toSkip : 0;
            for (Score score : Iterables.skip(scores, toSkip)) {
                String scoreOwner = score.getOwner();
                if (scoreOwner.startsWith("#"))
                    objectiveMap.put(scoreOwner, score.getScore());
                else {
                    Component key = PlayerTeam.formatNameForTeam(scoreboard.getPlayersTeam(scoreOwner), Component.literal(scoreOwner));
                    objectiveMap.put(jsonNames ? Component.Serializer.toJson(key) : key.getString(), score.getScore());
                }
            }
            if (objectiveMap.isEmpty())
                continue;
            map.put(boardNames.getOrDefault(i, String.valueOf(i)), objectiveMap);
        }
        return map;
    }
    
    
    
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = Boolean.class,
                            argumentNames = "jsonNames"
                    )
            },
            value = "host.get_objectives"
    )
    public Object getObjectives(boolean jsonNames) {
        if (!isHost() || this.minecraft.level == null)
            return null;
        HashMap<String, Map<String, String>> map = new HashMap<>();
        Scoreboard scoreboard = this.minecraft.level.getScoreboard();
        for (int i = 0; i < 19; i ++) {
            Objective objective = scoreboard.getDisplayObjective(i);
            if(objective == null)
                continue;
            map.put(boardNames.getOrDefault(i, String.valueOf(i)), new HashMap<>(){{
                put("name", objective.getName());
                put("displayName", jsonNames? Component.Serializer.toJson(objective.getDisplayName()) : objective.getDisplayName().getString());
                put("criteria", objective.getCriteria().getName());
                put("render_type", objective.getRenderType().getId());
            }});
        }
        return map;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "message"
            ),
            value = "host.send_chat_message"
    )
    public HostAPI sendChatMessage(@LuaNotNil String message) {
        if (!isHost() || !Configs.CHAT_MESSAGES.value) return this;
        ClientPacketListener connection = this.minecraft.getConnection();
        if (connection != null) connection.sendChat(message);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "command"
            ),
            value = "host.send_chat_command"
    )
    public HostAPI sendChatCommand(@LuaNotNil String command) {
        if (!isHost() || !Configs.CHAT_MESSAGES.value)
            return this;
        ClientPacketListener connection = this.minecraft.getConnection();
        if (connection != null) connection.sendCommand(command.startsWith("/") ? command.substring(1) : command);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
        overloads = @LuaMethodOverload(
            argumentTypes = Integer.class,
            argumentNames = "lines"
        ),
        value = "host.scroll_chat"
    )
    public HostAPI scrollChat(int lines) {
        if(isHost())
            this.minecraft.gui.getChat().scrollChat(lines);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("host.get_chat_scroll")
    public int getChatScroll() {
        if(!isHost())
            return 0;
        return ((ChatComponentAccessor)this.minecraft.gui.getChat()).getScrollbarPos();
    }

    @LuaWhitelist
    @LuaMethodDoc(
        overloads = @LuaMethodOverload(
            argumentTypes = int.class,
            argumentNames = "lines"
        ),
        value = "host.set_chat_scroll"
    )
    public HostAPI setChatScroll(int lines) {
        if(isHost())
            this.minecraft.gui.getChat().scrollChat(lines - getChatScroll());
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "message"
            ),
            value = "host.append_chat_history"
    )
    public HostAPI appendChatHistory(@LuaNotNil String message) {
        if (isHost())
            this.minecraft.gui.getChat().addRecentChat(message);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(value = "host.get_chat_history")
    public List<String> getChatHistory() {
        if (!isHost())
            return List.of();
        return this.minecraft.gui.getChat().getRecentChat();
    }

    @LuaWhitelist
    @LuaMethodDoc(
        value = "host.set_chat_history"
    )
    public HostAPI setChatHistory(String[] history) {
        if (!isHost())
            return this;
        List<String> chat = this.minecraft.gui.getChat().getRecentChat();
        chat.clear();
        chat.addAll(Arrays.asList(history));
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
        overloads = {
            @LuaMethodOverload(
                argumentTypes = ChatMessage.class,
                argumentNames = "message"
            ),
            @LuaMethodOverload(
                argumentTypes = {ChatMessage.class, Integer.class},
                argumentNames = {"message", "index"}
            )
        },
        value = "host.append_chat_message"
    )
    public HostAPI appendChatMessage(ChatMessage message, Integer index) {
        if (!isHost())
            return this;

        index = index == null? 0: index - 1;
        List<GuiMessage> messages = ((ChatComponentAccessor) this.minecraft.gui.getChat()).getAllMessages();
        if (index < 0 || index >= messages.size())
            return this;

        messages.add(index, message.message());

        this.minecraft.gui.getChat().rescaleChat();
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Integer.class,
                    argumentNames = "index"
            ),
            value = "host.get_chat_message"
    )
    public ChatMessage getChatMessage(int index) {
        if (!isHost())
            return null;

        index--;
        List<GuiMessage> messages = ((ChatComponentAccessor) this.minecraft.gui.getChat()).getAllMessages();
        if (index < 0 || index >= messages.size())
            return null;

        GuiMessage message = messages.get(index);
        return new ChatMessage(owner, message);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = Integer.class,
                            argumentNames = "index"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Integer.class, ChatMessage.class},
                            argumentNames = {"index", "message"}
                    )
            },
            value = "host.set_chat_message"
    )
    public HostAPI setChatMessage(int index, ChatMessage message) {
        if (!isHost())
            return this;

        index--;
        List<GuiMessage> messages = ((ChatComponentAccessor) this.minecraft.gui.getChat()).getAllMessages();
        if (index < 0 || index >= messages.size())
            return this;

        if (message == null)
            messages.remove(index);
        else
            messages.set(index, ("null".equals(message.getTagJson())? message.withTagText(GuiMessageTag.chatModified(messages.get(index).content().getString()).text()) : message).message());

        this.minecraft.gui.getChat().rescaleChat();
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(value = "host.get_chat_messages")
    public List<ChatMessage> getChatMessages() {
        if (!isHost())
            return null;

        List<GuiMessage> messages = ((ChatComponentAccessor)this.minecraft.gui.getChat()).getAllMessages();
        List<ChatMessage> convert = new ArrayList<>();
        for (GuiMessage message : Lists.reverse(messages))
            convert.add(new ChatMessage(owner, message));

        return convert;
    }

    @LuaWhitelist
    @LuaMethodDoc(
        overloads = {
            @LuaMethodOverload,
            @LuaMethodOverload(
                argumentTypes = LuaTable.class,
                argumentNames = "messages"
            ),
            @LuaMethodOverload(
                argumentTypes = {LuaTable.class, Boolean.class},
                argumentNames = {"messages", "trim"}
            ),
            @LuaMethodOverload(
                argumentTypes = {LuaTable.class, Boolean.class, Boolean.class},
                argumentNames = {"messages", "trim", "rerun"}
            )
        },
        value = "host.set_chat_messages"
    )
    public HostAPI setChatMessages(ChatMessage[] messages, Boolean trim, boolean rerun) {
        if (!isHost() || !Configs.CHAT_MESSAGES.value)
            return this;

        ChatComponent    chat = this.minecraft.gui.getChat();
        List<GuiMessage> full = ((ChatComponentAccessor) chat).getAllMessages();
        full.clear();
        if(rerun)
            FiguraMod.LOGGER.info("replacing all messages and rerunning events!");
        for (ChatMessage message : Lists.reverse(Arrays.asList(messages))) {
            if (rerun) {
                ((ChatComponentAccessor) chat).invokeLogChatMessage(message.message().content(), message.message().tag());
                String                json = message.getContentJson();
                Pair<String, Integer> res  = owner.chatReceivedMessageEvent(message.getContentText(), json);
                if (res != null) {
                    String newMessage = res.getFirst();
                    if (newMessage == null)
                        continue;
                    if (!json.equals(newMessage)) {
                        TextUtils.allowScriptEvents = true;
                        message = message.withContent(newMessage);
                        TextUtils.allowScriptEvents = false;
                    }
                    Integer color = res.getSecond();
                    message.setBackgroundColor(color == null? 0 : color);
                }
            }
            full.add(message.message());
        }

        List<GuiMessage.Line> cut = ((ChatComponentAccessor) chat).getTrimmedMessages();
        cut.clear();
        int i = Mth.floor((double)chat.getWidth() / chat.getScale());
        for (GuiMessage message : Lists.reverse(full)) {
            List<FormattedCharSequence> list = ComponentRenderUtils.wrapComponents(
                message.content(),
                message.tag() != null && message.tag().icon() != null? i - message.tag().icon().width - 6 : i,
                this.minecraft.font
            );

            for(int k = 0; k < list.size(); ++k) {
                FormattedCharSequence formattedCharSequence = list.get(k);

                boolean bl2 = k == list.size() - 1;
                GuiMessage.Line line = new GuiMessage.Line(message.addedTime(), formattedCharSequence, message.tag(), bl2);
                ((GuiMessageAccessor)(Object)line).figura$setColor(((GuiMessageAccessor)(Object)message).figura$getColor());
                cut.add(0, line);
            }
        }

        if (trim != Boolean.FALSE) {
            int max = trim == null? 100 : 10000;
            while(cut.size() > max)
                cut.remove(cut.size() - 1);

            while(full.size() > max)
                full.remove(full.size() - 1);
        }

        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = Boolean.class,
                            argumentNames = "offhand"
                    )
            },
            value = "host.swing_arm"
    )
    public HostAPI swingArm(boolean offhand) {
        if (isHost() && this.minecraft.player != null)
            this.minecraft.player.swing(offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "slot"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = Integer.class,
                            argumentNames = "slot"
                    )
            },
            value = "host.get_slot"
    )
    public ItemStackAPI getSlot(@LuaNotNil Object slot) {
        if (!isHost()) return null;
        Entity e = this.owner.luaRuntime.getUser();
        return ItemStackAPI.verify(e.getSlot(LuaUtils.parseSlot(slot, null)).get());
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(argumentTypes = String.class, argumentNames = "slot"),
                    @LuaMethodOverload(argumentTypes = Integer.class, argumentNames = "slot"),
                    @LuaMethodOverload(argumentTypes = {String.class, String.class}, argumentNames = {"slot", "item"}),
                    @LuaMethodOverload(argumentTypes = {Integer.class, ItemStackAPI.class}, argumentNames = {"slot", "item"})
            },
            value = "host.set_slot"
    )
    public HostAPI setSlot(@LuaNotNil Object slot, Object item) {
        if (!isHost() || (slot == null && item == null) || this.minecraft.gameMode == null || this.minecraft.player == null || !this.minecraft.gameMode.getPlayerMode().isCreative())
            return this;

        Inventory inventory = this.minecraft.player.getInventory();

        int index = LuaUtils.parseSlot(slot, inventory);
        ItemStack stack = LuaUtils.parseItemStack("setSlot", item);

        inventory.setItem(index, stack);
        this.minecraft.gameMode.handleCreativeModeItemAdd(stack, index + 36);

        return this;
    }

    @LuaWhitelist
    public HostAPI setBadge(int index, boolean value, boolean pride) {
        if (!isHost()) return this;
        if (!FiguraMod.debugModeEnabled())
            throw new LuaError("Congrats, you found this debug easter egg!");

        Pair<BitSet, BitSet> badges = AvatarManager.getBadges(owner.owner);
        if (badges == null)
            return this;

        BitSet set = pride ? badges.getFirst() : badges.getSecond();
        set.set(index, value);
        return this;
    }

    @LuaWhitelist
    public HostAPI badge(int index, boolean value, boolean pride) {
        return setBadge(index, value, pride);
    }

    @LuaWhitelist
    @LuaMethodDoc("host.get_chat_color")
    public Integer getChatColor() {
        return isHost() ? this.chatColor : null;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "color"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"r", "g", "b"}
                    )
            },
            aliases = "chatColor",
            value = "host.set_chat_color"
    )
    public HostAPI setChatColor(Object x, Double y, Double z) {
        if (isHost()) this.chatColor = x == null ? null : ColorUtils.rgbToInt(LuaUtils.parseVec3("setChatColor", x, y, z));
        return this;
    }

    @LuaWhitelist
    public HostAPI chatColor(Object x, Double y, Double z) {
        return setChatColor(x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc("host.get_chat_text")
    public String getChatText() {
        if (isHost() && this.minecraft.screen instanceof ChatScreen chat)
            return ((ChatScreenAccessor) chat).getInput().getValue();

        return null;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "text"
            ),
            aliases = "chatText",
            value = "host.set_chat_text"
    )
    public HostAPI setChatText(@LuaNotNil String text) {
        if (isHost() && Configs.CHAT_MESSAGES.value && this.minecraft.screen instanceof ChatScreen chat)
            ((ChatScreenAccessor) chat).getInput().setValue(text);
        return this;
    }

    @LuaWhitelist
    public HostAPI chatText(@LuaNotNil String text) {
        return setChatText(text);
    }

    @LuaWhitelist
    @LuaMethodDoc("host.get_screen")
    public String getScreen() {
        if (!isHost() || this.minecraft.screen == null)
            return null;
        return this.minecraft.screen.getClass().getName();
    }

    @LuaWhitelist
    @LuaMethodDoc("host.get_screen_slot_count")
    public Integer getScreenSlotCount() {
        if (isHost() && this.minecraft.screen instanceof AbstractContainerScreen<?> screen)
            return screen.getMenu().slots.size();
        return null;
    }

    @LuaWhitelist
    @LuaMethodDoc(overloads = {
            @LuaMethodOverload(argumentTypes = String.class, argumentNames = "slot"),
            @LuaMethodOverload(argumentTypes = Integer.class, argumentNames = "slot")
    }, value = "host.get_screen_slot")
    public ItemStackAPI getScreenSlot(@LuaNotNil Object slot) {
        if (!isHost() || !(this.minecraft.screen instanceof AbstractContainerScreen<?> screen))
            return null;

        NonNullList<Slot> slots = screen.getMenu().slots;
        int index = LuaUtils.parseSlot(slot, null);
        if (index < 0 || index >= slots.size())
            return null;
        return ItemStackAPI.verify(slots.get(index).getItem());
    }

    @LuaWhitelist
    @LuaMethodDoc("host.is_chat_open")
    public boolean isChatOpen() {
        return isHost() && this.minecraft.screen instanceof ChatScreen;
    }

    @LuaWhitelist
    @LuaMethodDoc("host.is_container_open")
    public boolean isContainerOpen() {
        return isHost() && this.minecraft.screen instanceof AbstractContainerScreen;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "name"
            ),
            value = "host.screenshot")
    public FiguraTexture screenshot(@LuaNotNil String name) {
        if (!isHost())
            return null;

        NativeImage img = Screenshot.takeScreenshot(this.minecraft.getMainRenderTarget());
        return owner.luaRuntime.texture.register(name, img, true);
    }

    @LuaWhitelist
    @LuaMethodDoc("host.is_avatar_uploaded")
    public boolean isAvatarUploaded() {
        return isHost() && AvatarManager.localUploaded;
    }

    @LuaWhitelist
    @LuaMethodDoc("host.get_status_effects")
    public List<Map<String, Object>> getStatusEffects() {
        List<Map<String, Object>> list = new ArrayList<>();

        LocalPlayer player = this.minecraft.player;
        if (!isHost() || player == null)
            return list;

        for (MobEffectInstance effect : player.getActiveEffects()) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", effect.getEffect().getDescriptionId());
            map.put("amplifier", effect.getAmplifier());
            map.put("duration", effect.getDuration());
            map.put("visible", effect.isVisible());

            list.add(map);
        }

        return list;
    }

    @LuaWhitelist
    @LuaMethodDoc("host.get_clipboard")
    public String getClipboard() {
        return isHost() ? this.minecraft.keyboardHandler.getClipboard() : null;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "text"
            ),
            aliases = "clipboard",
            value = "host.set_clipboard")
    public HostAPI setClipboard(@LuaNotNil String text) {
        if (isHost()) this.minecraft.keyboardHandler.setClipboard(text);
        return this;
    }

    @LuaWhitelist
    public HostAPI clipboard(@LuaNotNil String text) {
        return setClipboard(text);
    }

    @LuaWhitelist
    @LuaMethodDoc("host.get_attack_charge")
    public float getAttackCharge() {
        LocalPlayer player = this.minecraft.player;
        if (isHost() && player != null)
            return player.getAttackStrengthScale(0f);
        return 0f;
    }

    @LuaWhitelist
    @LuaMethodDoc("host.is_jumping")
    public boolean isJumping() {
        LocalPlayer player = this.minecraft.player;
        if (isHost() && player != null)
            return ((LivingEntityAccessor) player).isJumping();
        return false;
    }

    @LuaWhitelist
    @LuaMethodDoc("host.is_flying")
    public boolean isFlying() {
        LocalPlayer player = this.minecraft.player;
        if (isHost() && player != null)
            return player.getAbilities().flying;
        return false;
    }

    @LuaWhitelist
    @LuaMethodDoc("host.get_reach_distance")
    public double getReachDistance() {
        return this.minecraft.gameMode == null ? 0 : this.minecraft.gameMode.getPickRange();
    }

    @LuaWhitelist
    @LuaMethodDoc("host.get_air")
    public int getAir() {
        LocalPlayer player = this.minecraft.player;
        if (isHost() && player != null)
            return player.getAirSupply();
        return 0;
    }

    @LuaWhitelist
    @LuaMethodDoc("host.get_pick_block")
    public Object[] getPickBlock() {
        return isHost() ? LuaUtils.parseBlockHitResult(minecraft.hitResult) : null;
    }

    @LuaWhitelist
    @LuaMethodDoc("host.get_pick_entity")
    public EntityAPI<?> getPickEntity() {
        return isHost() && minecraft.crosshairPickEntity != null ? EntityAPI.wrap(minecraft.crosshairPickEntity) : null;
    }

    @LuaWhitelist
    @LuaMethodDoc("host.is_chat_verified")
    public boolean isChatVerified() {
        if (!isHost()) return false;
        ClientPacketListener connection = this.minecraft.getConnection();
        PlayerInfo playerInfo = connection != null ? connection.getPlayerInfo(owner.owner) : null;
        return playerInfo != null && playerInfo.hasVerifiableChat();
    }

    public Object __index(String arg) {
        if ("unlockCursor".equals(arg))
            return unlockCursor;
        return null;
    }

    @LuaWhitelist
    public void __newindex(@LuaNotNil String key, Object value) {
        if ("unlockCursor".equals(key))
            unlockCursor = (Boolean) value;
        else throw new LuaError("Cannot assign value on key \"" + key + "\"");
    }

    @Override
    public String toString() {
        return "HostAPI";
    }
}
