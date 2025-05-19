package engine;

public class Camera {
    public float orbitYaw = 0f;
    public float orbitPitch = 20f;
    public float distance = 200f;

    public float[] getPosition(float targetX, float targetY, float targetZ) {
        float yawRad = (float)Math.toRadians(orbitYaw);
        float pitchRad = (float)Math.toRadians(orbitPitch);
        float camX = targetX + (float)(Math.cos(pitchRad) * Math.sin(yawRad)) * distance;
        float camY = targetY + (float)(Math.sin(pitchRad)) * distance;
        float camZ = targetZ + (float)(Math.cos(pitchRad) * Math.cos(yawRad)) * distance;
        return new float[]{camX, camY, camZ};
    }

    public float[] getLookAngles(float camX, float camY, float camZ, float targetX, float targetY, float targetZ) {
        float dx = targetX - camX;
        float dy = targetY - camY;
        float dz = targetZ - camZ;
        float yaw = (float)Math.toDegrees(Math.atan2(dx, dz));
        float pitch = (float)-Math.toDegrees(Math.atan2(dy, Math.sqrt(dx*dx + dz*dz)));
        return new float[]{yaw, pitch};
    }
}
