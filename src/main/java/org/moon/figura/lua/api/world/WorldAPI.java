package org.moon.figura.lua.api.world;

import com.mojang.brigadier.StringReader;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.ReadOnlyLuaTable;
import org.moon.figura.lua.api.entity.EntityAPI;
import org.moon.figura.lua.api.entity.PlayerAPI;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaMethodOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.EntityUtils;
import org.moon.figura.utils.LuaUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LuaWhitelist
@LuaTypeDoc(
        name = "WorldAPI",
        value = "world"
)
public class WorldAPI {

    public static final WorldAPI INSTANCE = new WorldAPI();

    public static Level getCurrentWorld() {
        return Minecraft.getInstance().level;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            value = "world.get_biome"
    )
    public static BiomeAPI getBiome(Object x, Double y, Double z) {
        FiguraVec3 pos = LuaUtils.parseVec3("getBiome", x, y, z);
        BiomeAPI result = new BiomeAPI(getCurrentWorld().getBiome(pos.asBlockPos()).value(), pos.asBlockPos());
        pos.free();
        return result;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            value = "world.get_blockstate"
    )
    public static BlockStateAPI getBlockState(Object x, Double y, Double z) {
        FiguraVec3 pos = LuaUtils.parseVec3("getBlockState", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        pos.free();
        Level world = getCurrentWorld();
        if (world.getChunkAt(blockPos) == null)
            return new BlockStateAPI(Blocks.AIR.defaultBlockState(), blockPos);
        return new BlockStateAPI(world.getBlockState(blockPos), blockPos);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            value = "world.get_redstone_power"
    )
    public static int getRedstonePower(Object x, Double y, Double z) {
        FiguraVec3 pos = LuaUtils.parseVec3("getRedstonePower", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        pos.free();
        if (getCurrentWorld().getChunkAt(blockPos) == null)
            return 0;
        return getCurrentWorld().getBestNeighborSignal(blockPos);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            value = "world.get_strong_redstone_power"
    )
    public static int getStrongRedstonePower(Object x, Double y, Double z) {
        FiguraVec3 pos = LuaUtils.parseVec3("getStrongRedstonePower", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        pos.free();
        if (getCurrentWorld().getChunkAt(blockPos) == null)
            return 0;
        return getCurrentWorld().getDirectSignalTo(blockPos);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = Double.class,
                            argumentNames = "delta"
                    )
            },
            value = "world.get_time"
    )
    public static double getTime(double delta) {
        return getCurrentWorld().getGameTime() + delta;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = Double.class,
                            argumentNames = "delta"
                    )
            },
            value = "world.get_time_of_day"
    )
    public static double getTimeOfDay(double delta) {
        return getCurrentWorld().getDayTime() + delta;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload,
            value = "world.get_moon_phase"
    )
    public static int getMoonPhase() {
        return getCurrentWorld().getMoonPhase();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = Double.class,
                            argumentNames = "delta"
                    )
            },
            value = "world.get_rain_gradient"
    )
    public static double getRainGradient(Float delta) {
        if (delta == null) delta = 1f;
        return getCurrentWorld().getRainLevel(delta);
    }

    @LuaWhitelist
    @LuaMethodDoc("world.is_thundering")
    public static boolean isThundering() {
        return getCurrentWorld().isThundering();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            value = "world.get_light_level"
    )
    public static Integer getLightLevel(Object x, Double y, Double z) {
        FiguraVec3 pos = LuaUtils.parseVec3("getLightLevel", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        pos.free();
        Level world = getCurrentWorld();
        if (world.getChunkAt(blockPos) == null)
            return null;
        world.updateSkyBrightness();
        return world.getLightEngine().getRawBrightness(blockPos, world.getSkyDarken());
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            value = "world.get_sky_light_level"
    )
    public static Integer getSkyLightLevel(Object x, Double y, Double z) {
        FiguraVec3 pos = LuaUtils.parseVec3("getSkyLightLevel", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        pos.free();
        Level world = getCurrentWorld();
        if (world.getChunkAt(blockPos) == null)
            return null;
        return world.getBrightness(LightLayer.SKY, blockPos);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            value = "world.get_block_light_level"
    )
    public static Integer getBlockLightLevel(Object x, Double y, Double z) {
        FiguraVec3 pos = LuaUtils.parseVec3("getBlockLightLevel", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        pos.free();
        Level world = getCurrentWorld();
        if (world.getChunkAt(blockPos) == null)
            return null;
        return world.getBrightness(LightLayer.BLOCK, blockPos);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            value = "world.is_open_sky"
    )
    public static Boolean isOpenSky(Object x, Double y, Double z) {
        FiguraVec3 pos = LuaUtils.parseVec3("isOpenSky", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        pos.free();
        Level world = getCurrentWorld();
        if (world.getChunkAt(blockPos) == null)
            return null;
        return world.canSeeSky(blockPos);
    }

    @LuaWhitelist
    @LuaMethodDoc("world.get_dimension")
    public static String getDimension() {
        Level world = getCurrentWorld();
        return world.dimension().location().toString();
    }

    @LuaWhitelist
    @LuaMethodDoc("world.get_players")
    public static Map<String, EntityAPI<?>> getPlayers() {
        HashMap<String, EntityAPI<?>> playerList = new HashMap<>();
        for (Player player : getCurrentWorld().players())
            playerList.put(player.getName().getString(), PlayerAPI.wrap(player));
        return playerList;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "UUID"
            ),
            value = "world.get_entity"
    )
    public static EntityAPI<?> getEntity(@LuaNotNil String uuid) {
        try {
            return EntityAPI.wrap(EntityUtils.getEntityByUUID(UUID.fromString(uuid)));
        } catch (Exception ignored) {
            throw new LuaError("Invalid UUID");
        }
    }

    @LuaWhitelist
    public HashMap<String, Object> raycastBlock(boolean fluid, Object x, Object y, Double z, Object w, Double t, Double h) {
        if (true) return null;
        FiguraVec3 start, end;

        Pair<FiguraVec3, FiguraVec3> pair = LuaUtils.parse2Vec3("raycastBlock", x, y, z, w, t, h);
        start = pair.getFirst();
        end = pair.getSecond();

        BlockHitResult result = getCurrentWorld().clip(new ClipContext(start.asVec3(), end.asVec3(), ClipContext.Block.OUTLINE, fluid ? ClipContext.Fluid.NONE : ClipContext.Fluid.ANY, new Marker(EntityType.MARKER, getCurrentWorld())));

        start.free();
        end.free();

        if (result == null || result.getType() == HitResult.Type.MISS)
            return null;

        HashMap<String, Object> map = new HashMap<>();
        BlockPos pos = result.getBlockPos();
        map.put("block", getBlockState(pos.getX(), (double) pos.getY(), (double) pos.getZ()));
        map.put("direction", result.getDirection().getName());
        map.put("pos", FiguraVec3.fromVec3(result.getLocation()));

        return map;
    }

    @LuaWhitelist
    public HashMap<String, Object> raycastEntity(Object x, Object y, Double z, Object w, Double t, Double h) {
        if (true) return null;
        FiguraVec3 start, end;

        Pair<FiguraVec3, FiguraVec3> pair = LuaUtils.parse2Vec3("raycastEntity", x, y, z, w, t, h);
        start = pair.getFirst();
        end = pair.getSecond();

        EntityHitResult result = ProjectileUtil.getEntityHitResult(new Marker(EntityType.MARKER, getCurrentWorld()), start.asVec3(), end.asVec3(), new AABB(start.asVec3(), end.asVec3()), entity -> true, Double.MAX_VALUE);

        if (result == null)
            return null;

        HashMap<String, Object> map = new HashMap<>();
        map.put("entity", EntityAPI.wrap(result.getEntity()));
        map.put("pos", FiguraVec3.fromVec3(result.getLocation()));

        return map;
    }

    @LuaWhitelist
    @LuaMethodDoc("world.avatar_vars")
    public static Map<String, LuaTable> avatarVars() {
        HashMap<String, LuaTable> varList = new HashMap<>();
        for (Avatar avatar : AvatarManager.getLoadedAvatars()) {
            LuaTable tbl = avatar.luaRuntime == null ? new LuaTable() : avatar.luaRuntime.avatar_meta.storedStuff;
            varList.put(avatar.owner.toString(), new ReadOnlyLuaTable(tbl));
        }
        return varList;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "block"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, FiguraVec3.class},
                            argumentNames = {"block", "pos"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, Double.class, Double.class, Double.class},
                            argumentNames = {"block", "x", "y", "z"}
                    )
            },
            value = "world.new_block"
    )
    public static BlockStateAPI newBlock(@LuaNotNil String string, Object x, Double y, Double z) {
        BlockPos pos = LuaUtils.parseVec3("newBlock", x, y, z).asBlockPos();
        try {
            BlockState block = BlockStateArgument.block(new CommandBuildContext(RegistryAccess.BUILTIN.get())).parse(new StringReader(string)).getState();
            return new BlockStateAPI(block, pos);
        } catch (Exception e) {
            throw new LuaError("Could not parse block state from string: " + string);
        }
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "item"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, Integer.class},
                            argumentNames = {"item", "count"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, Integer.class, Integer.class},
                            argumentNames = {"item", "count", "damage"}
                    )
            },
            value = "world.new_item"
    )
    public static ItemStackAPI newItem(@LuaNotNil String string, Integer count, Integer damage) {
        try {
            ItemStack item = ItemArgument.item(new CommandBuildContext(RegistryAccess.BUILTIN.get())).parse(new StringReader(string)).createItemStack(1, false);
            if (count != null)
                item.setCount(count);
            if (damage != null)
                item.setDamageValue(damage);
            return new ItemStackAPI(item);
        } catch (Exception e) {
            throw new LuaError("Could not parse item stack from string: " + string);
        }
    }

    @LuaWhitelist
    @LuaMethodDoc("world.exists")
    public static boolean exists() {
        return getCurrentWorld() != null;
    }

    @Override
    public String toString() {
        return "WorldAPI";
    }
}
