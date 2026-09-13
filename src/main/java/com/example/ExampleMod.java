package com.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleMod implements ModInitializer {
	public static final String MOD_ID = "modid";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Block NUCLEAR_BOMB_BLOCK;
	public static Item NUCLEAR_BOMB_ITEM;

	@Override
	public void onInitialize() {
		LOGGER.info("Nuclear Bomb Mod загружен!");
		
		// Регистрируем блок ядерной бомбы
		NUCLEAR_BOMB_BLOCK = new NuclearBombBlock(
			FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)
				.strength(50.0f, 1200.0f)
		);
		
		Registry.register(
			BuiltInRegistries.BLOCK,
			new ResourceLocation(MOD_ID, "nuclear_bomb_block"),
			NUCLEAR_BOMB_BLOCK
		);
		
		// Регистрируем предмет блока
		NUCLEAR_BOMB_ITEM = new BlockItem(NUCLEAR_BOMB_BLOCK, new Item.Properties());
		Registry.register(
			BuiltInRegistries.ITEM,
			new ResourceLocation(MOD_ID, "nuclear_bomb_block"),
			NUCLEAR_BOMB_ITEM
		);
		
		// Добавляем в творческий режим
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS)
			.register(entries -> entries.accept(NUCLEAR_BOMB_ITEM));
	}
}
