package com.squoshi.sailor.physics;

import org.joml.Vector2d;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GerstnerWave {

    private double amplitude; // A
    private double directionX; // W_x component of direction vector
    private double directionZ; // W_z component of direction vector
    private double waveLength; // W
    private double steepness;  // Q (often derived from amplitude and wavelength)
    private double phaseConstant; // phi (derived from wavelength and gravity)

    // Gravity constant (adjust if needed, e.g., for different scales)
    private static final double GRAVITY = 9.8f;

    public GerstnerWave(double amplitude, Vector2d direction, double waveLength, double steepness) {
        this.amplitude = amplitude;
        // Normalize the direction vector
        double magnitude = (double) Math.sqrt(direction.x * direction.x + direction.y * direction.y);
        this.directionX = direction.x / magnitude;
        this.directionZ = direction.y / magnitude; // Mapping Point2D.y to Z for 3D world
        this.waveLength = waveLength;
        this.steepness = steepness;

        // Calculate wave number (k)
        double k = (2.0f * (double) Math.PI) / this.waveLength;

        // Calculate angular frequency (omega) and phase constant (phi)
        // For deep water waves: omega = sqrt(g * k)
        double omega = (double) Math.sqrt(GRAVITY * k);
        this.phaseConstant = omega;

        // A common way to calculate Q (steepness) if not directly provided,
        // ensuring it's relative to the wave number and amplitude.
        // For deep water, Q is often just a factor, or related to k*A
        // If steepness is given, use it. Otherwise, you might calculate it:
        // this.steepness = 0.5f / (k * amplitude); // Example, adjust as needed
    }

    /**
     * Calculates the position of a point on the wave surface at a given time.
     *
     * @param initialX The initial X coordinate of the point.
     * @param initialZ The initial Z coordinate of the point.
     * @param time The current time.
     * @return A Point3D representing the displaced position (x, y, z).
     */
    public Vector3d getWavePosition(double initialX, double initialZ, double time) {
        // Dot product of direction and initial position
        double dotProduct = (directionX * initialX + directionZ * initialZ);

        // Calculate the phase of the wave at this point and time
        double phase = dotProduct * ((2.0f * (double) Math.PI) / waveLength) + (phaseConstant * time);

        // Calculate displacements
        double displacementX = steepness * amplitude * directionX * (double) Math.cos(phase);
        double displacementY = amplitude * (double) Math.sin(phase);
        double displacementZ = steepness * amplitude * directionZ * (double) Math.cos(phase);

        return new Vector3d(initialX + displacementX, displacementY, initialZ + displacementZ);
    }

    public static Vector3d getWaterDisplacement(float x, float y, float z, float time){
        double totalX = x;
        double totalY = 0; // The base y level of the water
        double totalZ = z;
        Random rand = new Random();
        List<GerstnerWave> waves = new ArrayList<>();

//        waves.add(new GerstnerWave(rand.nextDouble()))

        for (GerstnerWave wave : waves) {
            Vector3d wavePos = wave.getWavePosition(x, z, time);
            // Sum the displacements, not the absolute positions
            totalX += (wavePos.x - x); // Only add the displacement from initial x
            totalY += wavePos.y;       // Add the height
            totalZ += (wavePos.z - z); // Only add the displacement from initial z
        }
        return new Vector3d(totalX-x, totalY-y, totalZ-z);
    }

}