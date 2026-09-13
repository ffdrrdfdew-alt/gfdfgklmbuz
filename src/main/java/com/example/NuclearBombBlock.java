package com.example;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class NuclearBombBlock extends Block {

	public NuclearBombBlock(Properties settings) {
		super(settings);
	}

	@Override
	protected InteractionResult useWithoutItem(
		BlockState state,
		Level world,
		BlockPos pos,
		Player player,
		BlockHitResult hit
	) {
		if (!world.isClientSide) {
			// Создаём ядерный взрыв
			world.explode(
				player,
				pos.getX() + 0.5,
				pos.getY() + 0.5,
				pos.getZ() + 0.5,
				20.0f,
				net.minecraft.world.level.Explosion.BlockInteraction.DESTROY
			);
			
			// Ищем всех жителей в радиусе 30 блоков
			var entities = world.getEntitiesOfClass(
				Villager.class,
				new net.minecraft.world.phys.AABB(
					pos.getX() - 30,
					pos.getY() - 30,
					pos.getZ() - 30,
					pos.getX() + 30,
					pos.getY() + 30,
					pos.getZ() + 30
			)
		);
		
			// Для каждого жителя спавним кишки
			for (Villager villager : entities) {
				Vec3 villagerPos = villager.position();
				
				// Спавним арморстендов как "кишок" вокруг жителя
				for (int i = 0; i < 5; i++) {
					double angleX = Math.random() * Math.PI * 2;
					double angleY = Math.random() * Math.PI * 2;
					double distance = 0.5;
					
					double x = villagerPos.x + Math.sin(angleX) * distance;
					double y = villagerPos.y + Math.random();
					double z = villagerPos.z + Math.cos(angleY) * distance;
					
					// Создаём визуальный эффект кишок (красные частицы)
					world.addParticle(
						net.minecraft.core.particles.ParticleTypes.REDSTONE,
						x, y, z,
						(Math.random() - 0.5) * 2,
						(Math.random() - 0.5) * 2,
						(Math.random() - 0.5) * 2
					);
					
					world.addParticle(
						net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE,
						x, y, z,
						(Math.random() - 0.5) * 0.5,
						(Math.random() - 0.5) * 0.5,
						(Math.random() - 0.5) * 0.5
					);
				}
				
				// Убиваем жителя
				villager.kill();
			}
			
			// Удаляем сам блок
			world.destroyBlock(pos, false);
		}
		
		return InteractionResult.SUCCESS;
	}
}
