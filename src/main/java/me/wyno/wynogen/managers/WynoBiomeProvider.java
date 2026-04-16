package me.wyno.wynogen.managers;

import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class WynoBiomeProvider extends BiomeProvider {

    private final List<Biome> biomes;

    public WynoBiomeProvider() {
        this.biomes = new ArrayList<>();
        // Add a varied selection of major biomes for the tight rotation
        biomes.add(Biome.PLAINS);
        biomes.add(Biome.DESERT);
        biomes.add(Biome.FOREST);
        biomes.add(Biome.TAIGA);
        biomes.add(Biome.SWAMP);
        biomes.add(Biome.JUNGLE);
        biomes.add(Biome.SNOWY_PLAINS);
        biomes.add(Biome.SAVANNA);
        biomes.add(Biome.BADLANDS);
        biomes.add(Biome.SUNFLOWER_PLAINS);
        biomes.add(Biome.MUSHROOM_FIELDS);
        biomes.add(Biome.ICE_SPIKES);
        biomes.add(Biome.CHERRY_GROVE);
        biomes.add(Biome.DARK_FOREST);
    }

    @Override
    public @NotNull Biome getBiome(@NotNull WorldInfo worldInfo, int x, int y, int z) {
        // Tight rotation logic:
        // Group biomes in 64x64 block patches to avoid pure static noise, but still guarantee all biomes quickly
        int scale = 64;
        int scaledX = Math.abs(x / scale);
        int scaledZ = Math.abs(z / scale);
        
        // Deterministic hash based on grid coordinates to pick a biome
        int index = (scaledX * 73 + scaledZ * 37) % biomes.size();
        return biomes.get(index);
    }

    @Override
    public @NotNull List<Biome> getBiomes(@NotNull WorldInfo worldInfo) {
        return biomes;
    }
}
