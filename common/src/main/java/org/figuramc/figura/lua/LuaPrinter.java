package org.figuramc.figura.lua;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.EntityType;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.TextUtils;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.VarArgFunction;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static org.figuramc.figura.utils.ColorUtils.Colors.AWESOME_BLUE;
import static org.figuramc.figura.utils.ColorUtils.Colors.LUA_PING;

public class LuaPrinter {
    private final FiguraLuaRuntime runtime;
    private final Avatar owner;

    public LuaPrinter(FiguraLuaRuntime runtime, Avatar avatar) {
        this.runtime = runtime;
        this.owner = avatar;
        LuaValue print = new PrintValue();
        runtime.setGlobal("print", print);
        runtime.setGlobal("log", print);
        LuaValue printTable = new PrintTable();
        runtime.setGlobal("printTable", printTable);
        runtime.setGlobal("logTable", printTable);
        LuaValue printJson = new PrintJson();
        runtime.setGlobal("printJson", printJson);
        runtime.setGlobal("logJson", printJson);
    }

    // prints message with ping data
    public void sendPingMessage(String name, int length, LuaValue[] args) {
        int config = Configs.LOG_PINGS.value;
        
        if (config == 0 || config == 1 && !owner.isHost)
            return;
        
        MutableComponent text = Component.empty()
            .append(Component.literal("[ping] ").withStyle(LUA_PING.style))
            .append(Component.literal(owner.entityName))
            .append(Component.literal(" : ").withStyle(LUA_PING.style))
            .append(name)
            .append(Component.literal(" :: ").withStyle(LUA_PING.style))
            .append(length + " bytes")
            .append(Component.literal(" :: ").withStyle(LUA_PING.style));
        
        for (LuaValue arg : args)
            text.append(getPrintText(runtime.typeManager, arg, true, false, new HashMap<>())).append("\t");
        
        text.append("\n");
        
        if (Configs.LOG_LOCATION.value == 0)
            sendChatMessage(text);
        else
            FiguraMod.LOGGER.info(text.getString());
    }

    public void clear() {
        charsQueued = 0;
        chatQueue.clear();
    }

    // print an error, errors should always show up on chat
    public void sendLuaError(LuaError error) {
        // why was it checked at the last moment?
        if (!(owner.entityType != EntityType.PLAYER || Configs.LOG_OTHERS.value || owner.isHost) || owner.permissions.getCategory() == Permissions.Category.BLOCKED)
            return;

        // Jank as hell
        String message = error.toString().replace("org.luaj.vm2.LuaError: ", "")
            .replace("\n\t[Java]: in ?", "")
            .replace("'<eos>' expected", "Expected end of script");

        if (Configs.EASTER_EGGS.value && Math.random() < 0.0001) {
            message = message
                .replaceFirst("attempt to index ? (a nil value) with key", "attempt to key (a ? value) with index nil")
                .replaceFirst("attempt to call a nil value", "attempt to nil a call value");
        }

        // get script line
        line: {
            if (owner.minify) {
                message += "\nscript:\n\tscript heavily minified! - cannot look for line numbers!";
                break line;
            }

            try {
                String[] split = message.split(":", 2);
                if (split.length <= 1 || owner.luaRuntime == null)
                    break line;

                // name
                String left = "[string \"";
                int sub = split[0].indexOf(left);

                String name = sub == -1 ? split[0] : split[0].substring(sub + left.length(), split[0].indexOf("\"]"));
                String src = owner.luaRuntime.scripts.get(name);
                if (src == null)
                    break line;

                // line
                int line = Integer.parseInt(split[1].split("\\D", 2)[0]);

                String str = src.split("\n")[line - 1].trim();
                if (str.length() > 96)
                    str = str.substring(0, 96) + " [...]";

                message += "\nscript:\n\t" + str;
            } catch (Exception ignored) {}
        }

        MutableComponent component = Component.empty()
            .append(Component.literal("[error] ").withStyle(ColorUtils.Colors.LUA_ERROR.style))
            .append(Component.literal(owner.entityName))
            .append(Component.literal(" : " + message).withStyle(ColorUtils.Colors.LUA_ERROR.style))
            .append(Component.literal("\n"));

        owner.errorText = TextUtils.replaceTabs(Component.literal(message).withStyle(ColorUtils.Colors.LUA_ERROR.style));

        chatQueue.offer(component); // bypass the char limit filter
        FiguraMod.LOGGER.error("", error);
    }

    private static Component tableToText(LuaTypeManager typeManager, LuaValue value, int depth, int indent, boolean hasTooltip, HashMap<LuaValue, Component> seen) {
        // attempt to parse top
        if (value.isuserdata())
            return userdataToText(typeManager, value, depth, indent, hasTooltip, seen);

        // normal print when invalid type or depth limit
        if (!value.istable() || depth <= 0)
            return getPrintText(typeManager, value, hasTooltip, true, seen);

        // format text
        MutableComponent text = Component.empty()
                .append(Component.literal("table:").withStyle(getTypeColor(value)))
                .append(Component.literal(" {\n").withStyle(ChatFormatting.GRAY));

        String spacing = "\t".repeat(indent - 1);

        LuaTable table = value.checktable();
        for (LuaValue key : sortKeys(table.keys()))
            text.append(getTableEntry(typeManager, spacing, key, table.get(key), hasTooltip, depth, indent, seen, table));

        text.append(spacing).append(Component.literal("}").withStyle(ChatFormatting.GRAY));
        return text;
    }

    // needs a special print because we want to also print NIL values
    private static Component userdataToText(LuaTypeManager typeManager, LuaValue value, int depth, int indent, boolean hasTooltip, HashMap<LuaValue, Component> seen) {
        // normal print when failed to parse userdata or depth limit
        if (!value.isuserdata() || depth <= 0)
            return getPrintText(typeManager, value, hasTooltip, true, seen);

        Object data = value.checkuserdata();
        Class<?> clazz = data.getClass();

        // format text
        MutableComponent text = Component.empty()
                .append(Component.literal("userdata (" + typeManager.getTypeName(clazz) + "):").withStyle(getTypeColor(value)))
                .append(Component.literal(" {\n").withStyle(ChatFormatting.GRAY));

        String spacing = "\t".repeat(indent - 1);

        LuaTable table = LuaValue.tableOf();

        if (clazz.isAnnotationPresent(LuaWhitelist.class)) {
            // fields
            Set<String> fields = new HashSet<>();
            for (Field field : clazz.getFields()) {
                String name = field.getName();
                if (!field.isAnnotationPresent(LuaWhitelist.class) || fields.contains(name))
                    continue;

                try {
                    Object obj = field.get(data);
                    table.rawset(name, typeManager.javaToLua(obj).arg(1));
                    fields.add(name);
                } catch (Exception e) {
                    FiguraMod.LOGGER.error("", e);
                }
            }

            // methods
            Set<String> methods = new HashSet<>();
            for (Method method : clazz.getMethods()) {
                String name = method.getName();
                if (!(!method.isAnnotationPresent(LuaWhitelist.class) || name.startsWith("__") || methods.contains(name))) {
                    table.rawset(name, getWrapper(method));
                    methods.add(name);
                }
            }
        }

        for (LuaValue key : sortKeys(table.keys()))
            text.append(getTableEntry(typeManager, spacing, key, table.get(key), hasTooltip, depth, indent, seen, table));

        text.append(spacing).append(Component.literal("}").withStyle(ChatFormatting.GRAY));
        return text;
    }

    private static List<LuaValue> sortKeys(LuaValue[] keys) {
        List<LuaValue> values = Arrays.asList(keys);
        values.sort((k1, k2) -> {
            if (k1 instanceof LuaString s1 && k2 instanceof LuaString s2)
                return new String(s1.m_bytes, s1.m_offset, s1.m_length).compareTo(new String(s2.m_bytes, s2.m_offset, s2.m_length));
            if (k1.isnumber() && !k2.isnumber())
                return -1;
            if (!k1.isnumber() && k2.isnumber())
                return 1;
            return Integer.compare(k1.hashCode(), k2.hashCode());
        });
        return values;
    }

    private static LuaValue getWrapper(Method method) {
        return new VarArgFunction() {{
            name = method.getName();
        }};
    }

    private static MutableComponent getTableEntry(LuaTypeManager typeManager, String spacing, LuaValue key, LuaValue value, boolean hasTooltip, int depth, int indent, HashMap<LuaValue, Component> seen, LuaValue parent) {
        MutableComponent text = Component.empty()
                .append(spacing).append("\t");

        seen = new HashMap<>(seen);

        // key
        Component keyText;
        if (key instanceof LuaString s && (Object) new String(s.m_bytes, s.m_offset, s.m_length) instanceof String string && string.matches("[a-zA-Z_][a-zA-Z_0-9]*")) {
            keyText = Component.literal(string).withStyle(ChatFormatting.GRAY);
        } else
            keyText = Component.translatable("chat.square_brackets", getPrintText(typeManager, key, hasTooltip, true, new HashMap<>())).withStyle(ChatFormatting.GRAY);
        text.append(keyText).append(" = ");

        // FiguraMod.LOGGER.info("{} / {} ({})", getPrintText(typeManager, key, false, true, new HashMap<>()).getString(), getPrintText(typeManager, value, false, true, new HashMap<>()).getString(), seen.containsKey(value) ? seen.get(value).getString() : "null" );
        // value
        if (value.istable() || value.isuserdata()) {
            if (seen.containsKey(value))
                text.append(seen.get(value));
            else {
                if (!seen.containsKey(parent))
                    seen.put(parent, Component.literal("table").withStyle(AWESOME_BLUE.style));
                if (keyText.getString().startsWith("["))
                    seen.put(value, seen.get(parent).copy().append(keyText));
                else
                    seen.put(value, seen.get(parent).copy().append(".").append(keyText));

                text.append(tableToText(typeManager, value, depth - 1, indent + 1, hasTooltip, seen));
            }
        } else {
            text.append(getPrintText(typeManager, value, hasTooltip, true, seen));
        }

        text.append("\n");
        return text;
    }

    // fancyString just means to add quotation marks around strings.
    private static MutableComponent getPrintText(LuaTypeManager typeManager, LuaValue value, boolean hasTooltip, boolean quoteStrings, HashMap<LuaValue, Component> seen) {
        String ret;

        // format value
        if (!(value instanceof LuaString) && value.isnumber()) {
            Double d = value.checkdouble();
            ret = d == Math.rint(d) ? value.tojstring() : Configs.decimalFormat.format(d);
        } else {
            ret = value.tojstring();
            if (value.isstring() && quoteStrings)
                ret = "\"" + ret + "\"";
        }

        MutableComponent text = Component.literal(ret).withStyle(getTypeColor(value));

        // table tooltip
        if (hasTooltip && (value.istable() || value.isuserdata())) {
            Component table = TextUtils.replaceTabs(tableToText(typeManager, value, 1, 1, false, seen));
            text.withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, table)));
        }

        return text;
    }

    private static Style getTypeColor(LuaValue value) {
        if (value.istable())
            return ColorUtils.Colors.AWESOME_BLUE.style;
        else if (!(value instanceof LuaString) && value.isnumber())
            return ColorUtils.Colors.BLUE.style;
        else if (value.isnil())
            return ColorUtils.Colors.LUA_ERROR.style;
        else if (value.isboolean())
            return LUA_PING.style;
        else if (value.isfunction())
            return Style.EMPTY.withColor(ChatFormatting.GREEN);
        else if (value.isuserdata())
            return Style.EMPTY.withColor(ChatFormatting.YELLOW);
        else if (value.isthread())
            return Style.EMPTY.withColor(ChatFormatting.GOLD);
        else
            return Style.EMPTY.withColor(ChatFormatting.WHITE);
    }
    
    private void sendLuaMessage(Component message) throws LuaError {
        MutableComponent component = Component.empty()
                .append(Component.literal("[lua] ").withStyle(ColorUtils.Colors.LUA_LOG.style))
                .append(Component.literal(owner.entityName))
                .append(Component.literal(" : ").withStyle(ColorUtils.Colors.LUA_LOG.style))
                .append(message)
                .append(Component.literal("\n"));

        if (Configs.LOG_LOCATION.value == 0)
            sendChatMessage(component);
        else
            FiguraMod.LOGGER.info(component.getString());
    }

    // -- SLOW PRINTING OF LOG --//

    // Log safety
    private final LinkedList<Component> chatQueue = new LinkedList<>();
    private int charsQueued = 0;

    /**
     * Sends a message making use of the queue
     * @param message to send
     * @throws org.luaj.vm2.LuaError if the message could not fit in the queue
     */
    private void sendChatMessage(Component message) throws LuaError {
        charsQueued += message.getString().length();
        if (charsQueued > owner.permissions.get(Permissions.MAX_PRINT_QUEUE)) {
            clear();
            sendLuaError(new LuaError("Chat overflow: printing too much!"));
            return;
        }
        chatQueue.offer(message);
    }

    public void tick() {
        if (chatQueue.isEmpty())
            return;

        MutableComponent toPrint = Component.empty();
        int i = owner.permissions.get(Permissions.MAX_PRINT_TICK);

        while (i > 0) {
            Component text = chatQueue.poll();
            if (text == null)
                break;

            int len = text.getString().length();
            if (len <= i) {
                i -= len;
                toPrint.append(text);
            } else {
                toPrint.append(TextUtils.substring(text, 0, i));
                chatQueue.offerFirst(TextUtils.substring(text, i, len));
                i = 0;
            }
        }

        String print = toPrint.getString();
        if (!print.isEmpty()) {
            charsQueued -= print.length();
            FiguraMod.sendChatMessage(print.endsWith("\n") ? TextUtils.substring(toPrint, 0, print.length() - 1) : toPrint);
        }
    }

    //-- PRINT FUNCTIONS --//



    abstract class PrintFunction extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            if (owner.isHost || Configs.LOG_OTHERS.value && owner.permissions.get(Permissions.PRINTING) != 0)
                return print(args);
            else
                return LuaValue.NONE;
        }

        abstract Varargs print(Varargs args);
    }

    class PrintValue extends PrintFunction {
        @Override
        Varargs print(Varargs args) {

            MutableComponent text = Component.empty();
            for (int i = 0; i < args.narg(); i++)
                text.append(getPrintText(runtime.typeManager, args.arg(i + 1), true, false, new HashMap<>())).append("\t");

            // prints the value, either on chat or console
            sendLuaMessage(text);

            return LuaValue.valueOf(text.getString());
        }

        @Override
        public String tojstring() {
            return "function: print";
        }
    }

    class PrintTable extends PrintFunction {
        @Override
        Varargs print(Varargs args) {
            boolean silent = false;
            MutableComponent text = Component.empty();

            if (args.narg() > 0) {
                int depth = args.arg(2).isnumber() ? args.arg(2).checkint() : 1;
                text.append(tableToText(runtime.typeManager, args.arg(1), depth, 1, true, new HashMap<>(Map.of(args.arg(1), Component.empty().append(TextUtils.substring(getPrintText(runtime.typeManager, args.arg(1), true, true, new HashMap<>()), 0, 5))))));
                silent = args.arg(3).isboolean() && args.arg(3).checkboolean();
            }

            if (!silent)
                sendLuaMessage(text);

            return LuaValue.valueOf(text.getString());
        }

        @Override
        public String tojstring() {
            return "function: printTable";
        }
    }

    class PrintJson extends PrintFunction {
        @Override
        Varargs print(Varargs args) {

            TextUtils.allowScriptEvents = true;

            MutableComponent text = Component.empty();
            for (int i = 0; i < args.narg(); i++)
                text.append(TextUtils.tryParseJson(args.arg(i + 1).tojstring()));

            TextUtils.allowScriptEvents = false;

            if (owner.isHost)
                sendChatMessage(text);
            else
                sendChatMessage(TextUtils.removeClickableObjects(text));

            return LuaValue.valueOf(text.getString());
        }

        @Override
        public String tojstring() {
            return "function: printJson";
        }
    }
}
