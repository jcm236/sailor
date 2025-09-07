package com.squoshi.sailor.ship;

import com.squoshi.sailor.util.NeighbourUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3i;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.ServerTickListener;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"deprecation","UnstableApiUsage"})
public final class ForceInducer implements ShipForcesInducer, ServerTickListener {

    private final ConcurrentHashMap<Vector3i,Boolean> edges = new ConcurrentHashMap<>();
    private ServerLevel level;
    private ServerShip ship;

    public void addEdge(Vector3i pos){
        edges.put(pos,false);
    }

    public void addEdge(@NotNull BlockPos pos){
        addEdge(VectorConversionsMCKt.toJOML(pos));
    }

    public void bulkSetEdge(HashMap<Vector3i, Boolean> map){
        edges.putAll(map);
    }

    public void removeEdge(Vector3i pos){
        edges.remove(pos);
    }

    public void removeEdge(BlockPos pos){
        removeEdge(VectorConversionsMCKt.toJOML(pos));
    }

    public void actualApplyForces(@NotNull PhysShipImpl physShip) {
        for (Map.Entry<Vector3i,Boolean> edge : edges.entrySet()){
            if (edge.getValue()){
                final Vector3dc linearVelocity = physShip.getPoseVel().getVel();
                final Vector3dc angularVelocity = physShip.getPoseVel().getOmega();
                Vector3d force = linearVelocity.mul(-physShip.get_inertia().getShipMass(), new Vector3d());
                double maxDrag = 10000;
                if (force.lengthSquared() > maxDrag * maxDrag) {
                    force.normalize(maxDrag);
                }
//            Vector3d force = linearVelocity.mul(linearVelocity, new Vector3d()).mul(500, new Vector3d()).mul(0.8,new Vector3d()).mul(-1,-1,-1);
                physShip.applyInvariantForce(force);

                final Vector3d rotForce = angularVelocity.mul(-physShip.getInertia().getShipMass(), new Vector3d());
//            Vector3d rotForce = angularVelocity.mul(angularVelocity, new Vector3d()).mul(500, new Vector3d()).mul(0.8,new Vector3d()).mul(-1,-1,-1);
                clampVector(rotForce, maxDrag);

                physShip.applyInvariantTorque(rotForce);
            }
        }
    }

    @Override
    public void onServerTick() {
        ConcurrentHashMap<Vector3i, Boolean> tempCopy = new ConcurrentHashMap<>(edges);
        for (Map.Entry<Vector3i,Boolean> edge : edges.entrySet()){
            Vector3i shipPos = edge.getKey();
            Vector3d worldPos = VSGameUtilsKt.toWorldCoordinates(this.ship, shipPos.x, shipPos.y, shipPos.z);
            BlockPos pos = BlockPos.containing((int) worldPos.x, (int) worldPos.y, (int) worldPos.z);
            if (NeighbourUtils.isSubmerged(pos, this.level)){
                tempCopy.put(shipPos,true);
            } else {
                tempCopy.put(shipPos,false);
            }
        }
    }

    public static Vector3d clampVector(Vector3d force, double limit) {
        // Clamp each component of the force vector within the range -limit, +limit
        force.x = Math.max(-limit, Math.min(limit, force.x));
        force.y = Math.max(-limit, Math.min(limit, force.y));
        force.z = Math.max(-limit, Math.min(limit, force.z));
        return force;
    }

    @Override
    public void applyForces(@NotNull PhysShip physShip) {
        this.actualApplyForces((PhysShipImpl) physShip);
    }

    public static @NotNull ForceInducer getOrCreate(@NotNull ServerShip ship, @NotNull ServerLevel level){
        if (ship.getAttachment(ForceInducer.class) == null) {
            ForceInducer attachment = new ForceInducer();
            attachment.ship = ship;
            attachment.level = level;
            return attachment;
        } else {
            return Objects.requireNonNull(ship.getAttachment(ForceInducer.class));
        }

    }

}
