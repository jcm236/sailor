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

    // Define ranges for wave parameters
    private static float minAmplitude = 0.2f;
    private static float maxAmplitude = 2.5f; // wave height

    private static float minWavelength = 5.0f;
    private static float maxWavelength = 50.0f; // wave size

    private static float minSteepness = 0.3f;
    private static float maxSteepness = 0.8f; // closer to 1 makes sharper waves



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

    public Vector3d getWaveNormal(float initialX, float initialZ, float time) {
        // Small value for calculating derivatives numerically
        float epsilon = 0.001f;

        // Get the position at the original point
        Vector3d p0 = getWavePosition(initialX, initialZ, time);

        // Get the position slightly offset in X
        Vector3d px = getWavePosition(initialX + epsilon, initialZ, time);

        // Get the position slightly offset in Z
        Vector3d pz = getWavePosition(initialX, initialZ + epsilon, time);

        // Create tangent vectors
        Vector3d tangentX = new Vector3d(px.x - p0.x, px.y - p0.y, px.z - p0.z);
        Vector3d tangentZ = new Vector3d(pz.x - p0.x, pz.y - p0.y, pz.z - p0.z);

        // The normal is the cross product of the tangents
        // You'll need to implement a cross product function in your Vector3d class
        Vector3d normal = tangentZ.cross(tangentX);
        normal.normalize(); // Ensure it's a unit vector

        return normal;
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
        double displacementY = amplitude * Math.sin(phase);
        double displacementZ = steepness * amplitude * directionZ * (double) Math.cos(phase);

        return new Vector3d(initialX + displacementX, displacementY, initialZ + displacementZ);
    }

//    public static Vector3d getWaterDisplacement(float x, float y, float z, float time, List<GerstnerWave> waves){
//        double totalX = x;
//        double totalY = 0; // The base y level of the water
//        double totalZ = z;
//
//        for (GerstnerWave wave : waves) {
//            Vector3d wavePos = wave.getWavePosition(x, z, time);
//            // Sum the displacements, not the absolute positions
//            totalX += (wavePos.x - x); // Only add the displacement from initial x
//            totalY += wavePos.y;       // Add the height
//            totalZ += (wavePos.z - z); // Only add the displacement from initial z
//        }
//        return new Vector3d(totalX, totalY, totalZ);
//    }

    /**
     * Generates a single GerstnerWave with random parameters within defined ranges.
     * @return A new GerstnerWave instance.
     */
    public GerstnerWave generateRandomWave() {
        Random random = new Random();
        float amplitude = minAmplitude + random.nextFloat() * (maxAmplitude - minAmplitude);
        float wavelength = minWavelength + random.nextFloat() * (maxWavelength - minWavelength);
        float steepness = minSteepness + random.nextFloat() * (maxSteepness - minSteepness);

        // Random direction: use an angle from 0 to 2*PI radians
        float angle = random.nextFloat() * 2 * (float) Math.PI;
        Vector2d direction = new Vector2d((float) Math.cos(angle), (float) Math.sin(angle));

        return new GerstnerWave(amplitude, direction, wavelength, steepness);
    }

    /**
     * Generates a list of random Gerstner waves.
     * @param numberOfWaves The number of waves to generate.
     * @return A list of GerstnerWave instances.
     */
    public List<GerstnerWave> generateWaveField(int numberOfWaves) {
        List<GerstnerWave> waves = new ArrayList<>();
        for (int i = 0; i < numberOfWaves; i++) {
            waves.add(generateRandomWave());
        }
        return waves;
    }

    public static Vector3d getWaterSurfaceNormal(float initialX, float initialZ, float time, List<GerstnerWave> waves) {
        Vector3d totalNormal = new Vector3d();
        for (GerstnerWave wave : waves) {
            totalNormal.add(wave.getWaveNormal(initialX, initialZ, time));
        }
        totalNormal.normalize();
        return totalNormal;
    }
}