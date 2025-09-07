package com.squoshi.sailor.mixin;

import com.squoshi.sailor.ship.ForceInducer;
import com.squoshi.sailor.util.NeighbourUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@Mixin(LevelChunk.class)
public abstract class MixinLevelChunk {
    @Shadow public abstract Level getLevel();

    @Inject(method = "setBlockState", at = @At("TAIL"))
    public void postSetBlockState(BlockPos pos, BlockState state, boolean isMoving, CallbackInfoReturnable<BlockState> cir){

        if (!(getLevel() instanceof ServerLevel)){
            return;
        }

        if (VSGameUtilsKt.isBlockInShipyard(getLevel(),pos)) {
            LoadedServerShip ship = VSGameUtilsKt.getShipObjectManagingPos((ServerLevel) getLevel(), pos);
            ForceInducer inducer = ForceInducer.getOrCreate(ship,(ServerLevel) getLevel());
            if (!state.isAir()) {
                if (NeighbourUtils.hasAirNearby(pos, (ServerLevel) getLevel())) {
                    inducer.addEdge(pos);
                } else {
                    inducer.removeEdge(pos);
                }
            } else {
                inducer.removeEdge(pos);
            }
            inducer.bulkSetEdge(NeighbourUtils.checkNearby(pos,(ServerLevel) getLevel()));
        }
    }
}
