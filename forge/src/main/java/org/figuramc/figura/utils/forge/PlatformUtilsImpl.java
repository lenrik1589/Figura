package org.figuramc.figura.utils.forge;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.IModInfo;
import org.figuramc.figura.FiguraMod;

import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class PlatformUtilsImpl {
    public static Path getGameDir() {
        return FMLPaths.GAMEDIR.relative();
    }

    public static String getFiguraModVersionString() {
        return ModList.get().getModContainerById(FiguraMod.MOD_ID).get().getModInfo().getVersion().getQualifier();
    }

    public static Path getConfigDir() {
        return FMLPaths.CONFIGDIR.relative();
    }

    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    public static String getModVersion(String modId) {
        return ModList.get().getModContainerById(modId).get().getModInfo().getVersion().getQualifier();
    }
}
