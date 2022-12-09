package com.branders.spawnermod.mixin;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.branders.spawnermod.config.ConfigValues;
import com.branders.spawnermod.registry.ModRegistry;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

/**
 * 	"Event" for mob drops. We inject into the dropLoot method and add a spawn
 * 	egg item if lucky enough.
 * 
 * 	@author Anders <Branders> Blomqvist
 */
@Mixin(MobEntity.class)
public class MobEntityDropsMixin {
	
	@Inject(
			at = @At("HEAD"),
			method = "dropLoot(Lnet/minecraft/entity/damage/DamageSource;Z)V",
			cancellable = true
	)
	private void dropLoot(DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
		
		// Leave if eggs should only drop when killed by a player
	    if(ConfigValues.get("monster_egg_only_drop_when_killed_by_player") == 1 && !causedByPlayer)
			return;
		
		Random random = new Random();
		
		if(random.nextFloat() > ConfigValues.get("monster_egg_drop_chance") / 100f)
			return;
		
		MobEntity entity = (MobEntity) (Object) this;
		ServerWorld world = (ServerWorld) entity.getEntityWorld();
		
		ItemStack egg;
		EntityType<?> entityType = entity.getType();
		
		if(ConfigValues.isEggDisabled(EntityType.getId(entityType).toString()))
			return;
		
		if(entityType.equals(EntityType.ENDER_DRAGON) || entityType.equals(EntityType.WITHER))
			return;
		else if(entityType.equals(EntityType.IRON_GOLEM))
			egg = new ItemStack(ModRegistry.IRON_GOLEM_SPAWN_EGG);
		else
			egg = new ItemStack(Registries.ITEM.get(new Identifier(EntityType.getId(entityType).toString() + "_spawn_egg")));
		
		world.spawnEntity(new ItemEntity(world, entity.prevX, entity.prevY, entity.prevZ, egg));
	}
}

