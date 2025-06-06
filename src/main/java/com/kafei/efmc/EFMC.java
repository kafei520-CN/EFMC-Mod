package com.kafei.efmc;

import com.kafei.efmc.common.config.MultiSlotConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("efmc")
public class EFMC {
    public EFMC() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

    private void setup(final net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent event) {
        MultiSlotConfig.load();
    }
}