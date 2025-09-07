package com.squoshi.sailor.mixin;

import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Block.class)
public class MixinBlock {
//    @Inject(method = "fallOn", at = @At("HEAD"))
//    private void sailor$fallOn(Level pLevel, BlockState pState, BlockPos pPos, Entity pEntity, float pFallDistance, CallbackInfo ci) {
//        if (pLevel.isClientSide()) return;
//        ServerEvents.onFall(pLevel, pPos, pEntity, pFallDistance);
//    }
//    @Inject(method = "stepOn", at = @At("HEAD"))
//    private void sailor$stepOn(Level pLevel, BlockPos pPos, BlockState pState, Entity pEntity, CallbackInfo ci) {
//        if (pLevel.isClientSide()) return;
//        ServerEvents.stepOn(pPos, pEntity);
//    }
}
