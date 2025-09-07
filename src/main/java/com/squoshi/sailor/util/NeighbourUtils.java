package com.squoshi.sailor.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import java.util.HashMap;



public class NeighbourUtils {
    public static boolean hasAirNearby(BlockPos pos, ServerLevel level){
        for (Direction dir : Direction.values()){
            BlockPos steppedPos = pos.offset(dir.getStepX(), dir.getStepY(), dir.getStepZ());
            if (level.getBlockState(steppedPos).isAir()){
                return true;
            }
        }
        return false;
    }

    public static boolean isSubmerged(BlockPos pos, ServerLevel level){
        if (VSGameUtilsKt.getShipObjectManagingPos(level, pos) != null){
            Vector3d world = VSGameUtilsKt.toWorldCoordinates(level, pos.getX(),pos.getY(),pos.getZ());
            BlockPos worldPos = BlockPos.containing((int) world.x, (int) world.y, (int) world.z);
            return level.getBlockState(worldPos).is(Blocks.WATER);
        }
        return false;
    }

    public static HashMap<Vector3i, Boolean> checkNearby(BlockPos pos, ServerLevel level){
        HashMap<Vector3i, Boolean> neighbours = new HashMap<>();
        for (Direction dir : Direction.values()){
            BlockPos steppedPos = pos.offset(dir.getStepX(), dir.getStepY(), dir.getStepZ());
            if (steppedPos.equals(pos)) {
                continue;
            }
            if (hasAirNearby(steppedPos,level)){
               neighbours.put(VectorConversionsMCKt.toJOML(steppedPos), true);
            } else {
                neighbours.put(VectorConversionsMCKt.toJOML(steppedPos),false);
            }
        }
        return neighbours;
    }

}
