package crazypants.enderzoo.spawn.impl;

import java.util.ArrayList;
import java.util.List;

import crazypants.enderzoo.config.Config;
import crazypants.enderzoo.spawn.IBiomeDescriptor;
import crazypants.enderzoo.spawn.IBiomeFilter;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

public abstract class AbstractBiomeFilter implements IBiomeFilter {

  protected final List<BiomeDictionary.Type> types = new ArrayList<BiomeDictionary.Type>();
  protected final List<BiomeDictionary.Type> typeExcludes = new ArrayList<BiomeDictionary.Type>();

  protected final List<ResourceLocation> names = new ArrayList<ResourceLocation>();
  protected final List<ResourceLocation> nameExcludes = new ArrayList<ResourceLocation>();

  @Override
  public void addBiomeDescriptor(IBiomeDescriptor biome) {
    if (biome.getType() != null) {
      if (biome.isExclude()) {
        typeExcludes.add(biome.getType());
      } else {
        types.add(biome.getType());
      }
    } else if (biome.getName() != null) {
      if (biome.isExclude()) {
        nameExcludes.add(biome.getName());
      } else {
        names.add(biome.getName());
      }
    }
  }

  protected boolean isExcluded(Biome candidate) {
    for (BiomeDictionary.Type exType : typeExcludes) {
      if (BiomeDictionary.hasType(candidate, exType)) {
        if (Config.spawnConfigPrintDetailedOutput) {
          System.out.print("Excluded " + candidate.getBiomeName() + ", ");
        }
        return true;

      }
    }
    for (ResourceLocation exName : nameExcludes) {
      if (exName != null && exName.equals(candidate.getRegistryName())) {
        System.out.print("Excluded " + candidate.getRegistryName() + ", ");
        return false;
      }
    }
    return false;
  }

}
