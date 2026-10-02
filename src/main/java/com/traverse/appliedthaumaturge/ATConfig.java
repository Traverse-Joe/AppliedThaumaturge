package com.traverse.appliedthaumaturge;

import com.leclowndu93150.thaumaturge.content.wands.WandEconomy;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ATConfig {
    public static final int MAX_KNOWLEDGE_CORE_RECIPES = 1024;
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.IntValue KNOWLEDGE_CORE_RECIPES;
    private static final ModConfigSpec.IntValue ARCANE_ASSEMBLER_BUFFER_VIS;
    private static final ModConfigSpec.IntValue ARCANE_CHARGING_RADIUS;
    private static final ModConfigSpec.DoubleValue ARCANE_ASSEMBLER_IDLE_POWER;
    private static final ModConfigSpec.DoubleValue VIS_RELAY_IDLE_POWER;
    private static final ModConfigSpec.DoubleValue VIS_RELAY_POWER_PER_REQUEST;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        KNOWLEDGE_CORE_RECIPES = builder.worldRestart()
                .defineInRange("knowledgeCoreMaxRecipes", 9, 1, MAX_KNOWLEDGE_CORE_RECIPES);
        ARCANE_ASSEMBLER_BUFFER_VIS = builder.worldRestart()
                .defineInRange("arcaneAssemblerMaxBufferVis", 150, 0, 1_000_000);
        ARCANE_CHARGING_RADIUS = builder.worldRestart()
                .defineInRange("arcaneChargingCardRadiusChunks", 1, 0, 16);
        ARCANE_ASSEMBLER_IDLE_POWER = builder.worldRestart()
                .defineInRange("arcaneAssemblerIdlePowerAE", 1.0, 0.0, 1_000_000.0);
        VIS_RELAY_IDLE_POWER = builder.worldRestart()
                .defineInRange("visRelayInterfaceIdlePowerAE", 0.0, 0.0, 1_000_000.0);
        VIS_RELAY_POWER_PER_REQUEST = builder.worldRestart()
                .defineInRange("visRelayInterfacePowerPerRequestAE", 4.0, 0.0, 1_000_000.0);
        SPEC = builder.build();
    }

    private ATConfig() {
    }

    public static int knowledgeCoreMaxRecipes() {
        return SPEC.isLoaded() ? KNOWLEDGE_CORE_RECIPES.get() : KNOWLEDGE_CORE_RECIPES.getDefault();
    }

    public static int arcaneAssemblerMaxBufferCentivis() {
        int vis = SPEC.isLoaded() ? ARCANE_ASSEMBLER_BUFFER_VIS.get() : ARCANE_ASSEMBLER_BUFFER_VIS.getDefault();
        return vis * WandEconomy.CENTIVIS_PER_VIS;
    }

    public static int arcaneChargingCardRadiusChunks() {
        return SPEC.isLoaded() ? ARCANE_CHARGING_RADIUS.get() : ARCANE_CHARGING_RADIUS.getDefault();
    }

    public static double arcaneAssemblerIdlePowerAE() {
        return SPEC.isLoaded() ? ARCANE_ASSEMBLER_IDLE_POWER.get() : ARCANE_ASSEMBLER_IDLE_POWER.getDefault();
    }

    public static double visRelayInterfaceIdlePowerAE() {
        return SPEC.isLoaded() ? VIS_RELAY_IDLE_POWER.get() : VIS_RELAY_IDLE_POWER.getDefault();
    }

    public static double visRelayInterfacePowerPerRequestAE() {
        return SPEC.isLoaded() ? VIS_RELAY_POWER_PER_REQUEST.get() : VIS_RELAY_POWER_PER_REQUEST.getDefault();
    }
}
