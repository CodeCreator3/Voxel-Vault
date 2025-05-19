package engine;

import java.awt.Cursor;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Window;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import graphics.Renderer;
import input.InputHandler;
import objects.Player;
import objects.GameObject;

public class Engine {
    private boolean running;
    private final Renderer renderer;
    private final InputHandler inputHandler;
    private final World world;
    private final Player player;
    private final Camera camera;

    // Add these fields to Engine:
    private long lastSpawnTime = System.currentTimeMillis();
    private final long spawnInterval = 2500; // ms
    private final java.util.Random random = new java.util.Random();
    private int spawnCount = 0;
    private int points = 0; // <-- Add points field

    private enum GameState { TITLE, PLAYING, PAUSED, GAME_OVER }
    private GameState gameState = GameState.TITLE;

    private boolean dragging = false;
    private int lastMouseX, lastMouseY;

    private boolean recentering = false;

    public Engine() {
        renderer = new Renderer();
        world = new World();
        player = new Player(
            0,
            world.platform.getY() + world.platform.height / 2f + Player.SIZE / 2f,
            0
        );
        camera = new Camera();

        try {
            Robot robot = new Robot();

            // Hide cursor for gameplay, show for title screen
            renderer.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                @Override
                public void mouseMoved(java.awt.event.MouseEvent e) {
                    if (gameState == GameState.PLAYING) {
                        if (recentering) {
                            // Ignore this event, it was caused by robot.mouseMove
                            recentering = false;
                            lastMouseX = e.getX();
                            lastMouseY = e.getY();
                            return;
                        }
                        if (lastMouseX != 0 || lastMouseY != 0) {
                            int dx = e.getX() - lastMouseX;
                            int dy = e.getY() - lastMouseY;
                            camera.orbitYaw += dx * 0.5f;
                            camera.orbitPitch -= dy * 0.5f;
                            if (camera.orbitPitch < -89) camera.orbitPitch = -89;
                            if (camera.orbitPitch > 89) camera.orbitPitch = 89;
                        }
                        // Recenter mouse to the middle of the window
                        Window window = javax.swing.SwingUtilities.getWindowAncestor(renderer);
                        if (window != null) {
                            Point loc = window.getLocationOnScreen();
                            int centerX = loc.x + renderer.getWidth() / 2;
                            int centerY = loc.y + renderer.getHeight() / 2;
                            recentering = true; // Set flag before recentering
                            robot.mouseMove(centerX, centerY);
                            lastMouseX = renderer.getWidth() / 2;
                            lastMouseY = renderer.getHeight() / 2;
                        }
                    }
                }
            });

            renderer.setPauseActions(
                () -> {
                    gameState = GameState.PLAYING;
                    renderer.setPauseScreenVisible(false);
                    // Hide cursor again for gameplay
                    Toolkit toolkit = Toolkit.getDefaultToolkit();
                    BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
                    Cursor blankCursor = toolkit.createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
                    renderer.getFrame().setCursor(blankCursor);

                    // Ensure the game window regains keyboard focus
                    renderer.getFrame().requestFocusInWindow();
                },
                () -> {
                    // Show title screen and normal cursor
                    gameState = GameState.TITLE;
                    renderer.setPauseScreenVisible(false);
                    renderer.setTitleScreenVisible(true);
                    renderer.getFrame().setCursor(Cursor.getDefaultCursor());
                    renderer.updateMenuButtonPositions(); // <-- Add this line
                }
            );

            renderer.setGameOverActions(
                () -> { // Restart
                    // Reset game state and player position, cubes, score, etc.
                    points = 0;
                    spawnCount = 0;
                    world.movingCubes.clear();
                    player.setX(0);
                    player.setY(world.platform.getY() + world.platform.height / 2f + Player.SIZE / 2f);
                    player.setZ(0);
                    player.setVelocityY(0);
                    gameState = GameState.PLAYING;
                    // Hide cursor for gameplay
                    Toolkit toolkit = Toolkit.getDefaultToolkit();
                    BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
                    Cursor blankCursor = toolkit.createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
                    renderer.getFrame().setCursor(blankCursor);
                    renderer.getFrame().requestFocusInWindow();
                },
                () -> { // Menu
                    gameState = GameState.TITLE;
                    renderer.setTitleScreenVisible(true);
                    renderer.getFrame().setCursor(Cursor.getDefaultCursor());
                }
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Optionally, reset lastMouseX/lastMouseY on window enter
        renderer.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                lastMouseX = e.getX();
                lastMouseY = e.getY();
            }
        });

        inputHandler = new InputHandler();
        renderer.initialize();
        inputHandler.attachToFrame(renderer.getFrame());
    }

    public void start() {
        running = true;
        renderer.setTitleScreenVisible(true); // Show title screen initially
        renderer.setPlayButtonListener(() -> {
            // Reset game state
            points = 0;
            spawnCount = 0;
            world.movingCubes.clear();
            player.setX(0);
            player.setY(world.platform.getY() + world.platform.height / 2f + Player.SIZE / 2f);
            player.setZ(0);
            player.setVelocityY(0);

            gameState = GameState.PLAYING;
            renderer.setTitleScreenVisible(false);
            // Hide cursor when game starts
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Cursor blankCursor = toolkit.createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
            renderer.getFrame().setCursor(blankCursor);
            renderer.getFrame().requestFocusInWindow();
        });
        renderer.setTitleScreenVisible(true);
        // Show normal cursor on title screen
        renderer.getFrame().setCursor(Cursor.getDefaultCursor());
        while (running) {
            if (gameState == GameState.PLAYING) {
                update();
            }
            render();
            try { Thread.sleep(16); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }

    private void update() {
        inputHandler.processInput();
        if (inputHandler.isKeyPressed(java.awt.event.KeyEvent.VK_ESCAPE)) {
            if (gameState == GameState.PLAYING) {
                gameState = GameState.PAUSED;
                renderer.setPauseScreenVisible(true);
                // Show normal cursor
                renderer.getFrame().setCursor(Cursor.getDefaultCursor());
            } else if (gameState == GameState.PAUSED) {
                gameState = GameState.PLAYING;
                renderer.setPauseScreenVisible(false);
                // Hide cursor
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
                Cursor blankCursor = toolkit.createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
                renderer.getFrame().setCursor(blankCursor);
            }
        }
        handleCameraInput();
        handlePlayerInput();
        handlePhysicsAndCollision();

        // Add this code to update() for spawning cubes
        long now = System.currentTimeMillis();
        long dynamicSpawnInterval = Math.max((long)(spawnInterval / getDifficultyMultiplier()), 300); // never faster than 300ms
        if (now - lastSpawnTime > dynamicSpawnInterval) {
            lastSpawnTime = now;
            spawnCount++;

            // Spawn a red cube far away, at platform height
            float dist = 1200f + random.nextFloat() * 400f;
            int dir = random.nextInt(4);
            float defaultY = world.platform.getY() + world.platform.height / 2f + 20;
            float bias = random.nextFloat();
            float yOffset = -bias * bias * 200f; // More likely negative, range [-200, 0]
            float y = Math.max(defaultY, defaultY + yOffset); // Clamp so y is never below defaultY
            float x = 0, z = 0, vx = 0, vz = 0;
            float xOffset = (random.nextFloat() - 0.5f) * 200f; // random X offset
            float zOffset = (random.nextFloat() - 0.5f) * 200f; // random Z offset
            float speed = (2f + random.nextFloat() * 2f) * getDifficultyMultiplier();
            switch (dir) {
                case 0: x = dist + xOffset; z = zOffset; vx = -speed; break;
                case 1: x = -dist + xOffset; z = zOffset; vx = speed; break;
                case 2: x = xOffset; z = dist + zOffset; vz = -speed; break;
                case 3: x = xOffset; z = -dist + zOffset; vz = speed; break;
            }
            GameObject cube = new GameObject(x, y, z, java.awt.Color.RED);
            cube.setVelocityX(vx);
            cube.setVelocityZ(vz);
            world.movingCubes.add(cube);

            // Probability increases with spawnCount, capped at 90%
            float probability = Math.min(0.25f + 0.01f * spawnCount, 0.9f);
            if (random.nextFloat() < probability) {
                // -1 means no forbidden axis/sign for the first call
                trySpawnAdjacentCube(cube, vx, vz, probability, -1, 0f);
            }
        }

        // Update moving cubes
        for (GameObject cube : world.movingCubes) {
            cube.setX(cube.getX() + cube.getVelocityX());
            cube.setZ(cube.getZ() + cube.getVelocityZ());
        }

        // Despawn cubes that are far from the platform (e.g., 1500 units from platform center)
        float platformX = world.platform.getX();
        float platformY = world.platform.getY();
        float platformZ = world.platform.getZ();
        float despawnDistance = 1500f;

        int cubesBefore = world.movingCubes.size(); // <-- Track number of cubes before despawning
        world.movingCubes.removeIf(cube -> {
            float dx = cube.getX() - platformX;
            float dy = cube.getY() - platformY;
            float dz = cube.getZ() - platformZ;
            float dist = (float)Math.sqrt(dx*dx + dy*dy + dz*dz);
            // Only despawn if the cube has passed the platform (dot product with velocity > 0)
            float vx = cube.getVelocityX();
            float vz = cube.getVelocityZ();
            float vLen = (float)Math.sqrt(vx*vx + vz*vz);
            float dot = (dx * vx + dz * vz);
            return dist > despawnDistance && dot > 0;
        });
        int cubesAfter = world.movingCubes.size(); // <-- Track number of cubes after despawning
        points += (cubesBefore - cubesAfter); // <-- Increment points by the number of cubes despawned

        // Pass points to the renderer:
        renderer.setPoints(points);
    }

    private void trySpawnAdjacentCube(GameObject baseCube, float vx, float vz, float probability, int forbiddenAxis, float forbiddenSign) {
        // Try up to 6 times to find a valid direction
        for (int attempt = 0; attempt < 6; attempt++) {
            int axis = random.nextInt(3);
            float sign = random.nextBoolean() ? 1f : -1f;

            // Prevent spawning back toward the parent cube
            if (axis == forbiddenAxis && sign == -forbiddenSign) continue;

            float adjX = baseCube.getX();
            float adjY = baseCube.getY();
            float adjZ = baseCube.getZ();
            if (axis == 0) adjX += sign * 40f;
            else if (axis == 1) adjY += sign * 40f;
            else adjZ += sign * 40f;

            // Prevent spawning below the platform
            float platformTop = world.platform.getY() + world.platform.height / 2f;
            float minY = platformTop + 20f; // 20 = half cube size
            if (adjY < minY) continue;

            GameObject cube2 = new GameObject(adjX, adjY, adjZ, java.awt.Color.RED);
            cube2.setVelocityX(vx);
            cube2.setVelocityZ(vz);
            world.movingCubes.add(cube2);

            // Mark as paired for collision logic
            baseCube.pairedWith = cube2;
            cube2.pairedWith = baseCube;

            // Recursively try to spawn another adjacent cube (with the same probability)
            if (random.nextFloat() < probability) {
                trySpawnAdjacentCube(cube2, vx, vz, probability, axis, sign);
            }
            break; // Only spawn one adjacent cube per call
        }
    }

    private void handleCameraInput() {
        float rotSpeed = 2f;
        if (inputHandler.isKeyPressed(java.awt.event.KeyEvent.VK_LEFT)) camera.orbitYaw -= rotSpeed;
        if (inputHandler.isKeyPressed(java.awt.event.KeyEvent.VK_RIGHT)) camera.orbitYaw += rotSpeed;
        if (inputHandler.isKeyPressed(java.awt.event.KeyEvent.VK_UP)) camera.orbitPitch -= rotSpeed;
        if (inputHandler.isKeyPressed(java.awt.event.KeyEvent.VK_DOWN)) camera.orbitPitch += rotSpeed;
        if (camera.orbitPitch < -89) camera.orbitPitch = -89;
        if (camera.orbitPitch > 89) camera.orbitPitch = 89;
    }

    private void handlePlayerInput() {
        float moveSpeed = 5f * getDifficultyMultiplier();
        float yawRad = (float)Math.toRadians(camera.orbitYaw);
        float forwardX = -(float)Math.sin(yawRad);
        float forwardZ = -(float)Math.cos(yawRad);
        float rightX = (float)-Math.cos(yawRad);
        float rightZ = (float)Math.sin(yawRad);

        float px = player.getX();
        float pz = player.getZ();

        if (inputHandler.isKeyPressed(java.awt.event.KeyEvent.VK_W)) {
            px += forwardX * moveSpeed;
            pz += forwardZ * moveSpeed;
        }
        if (inputHandler.isKeyPressed(java.awt.event.KeyEvent.VK_S)) {
            px -= forwardX * moveSpeed;
            pz -= forwardZ * moveSpeed;
        }
        if (inputHandler.isKeyPressed(java.awt.event.KeyEvent.VK_A)) {
            px -= rightX * moveSpeed;
            pz -= rightZ * moveSpeed;
        }
        if (inputHandler.isKeyPressed(java.awt.event.KeyEvent.VK_D)) {
            px += rightX * moveSpeed;
            pz += rightZ * moveSpeed;
        }
        player.setX(px);
        player.setZ(pz);

        // Jumping
        float playerBottomY = player.getY() - player.getHalfSize();
        float prismTop = world.platform.getY() + world.platform.height / 2f;
        boolean onPrismXZ = px >= world.platform.getX() - world.platform.width / 2f &&
                            px <= world.platform.getX() + world.platform.width / 2f &&
                            pz >= world.platform.getZ() - world.platform.depth / 2f &&
                            pz <= world.platform.getZ() + world.platform.depth / 2f;
        boolean onGround = Math.abs(playerBottomY - world.groundY) < 0.01f;
        boolean onPrism = onPrismXZ && Math.abs(playerBottomY - prismTop) < 0.01f;

        // New: Check if standing on top of any cube
        boolean onCube = false;
        for (GameObject cube : world.movingCubes) {
            float cubeTop = cube.getY() + 20f; // 20 = half cube size
            boolean onCubeXZ = px >= cube.getX() - 20f && px <= cube.getX() + 20f &&
                               pz >= cube.getZ() - 20f && pz <= cube.getZ() + 20f;
            if (onCubeXZ && Math.abs(playerBottomY - cubeTop) < 0.01f) {
                onCube = true;
                break;
            }
        }

        if (inputHandler.isKeyPressed(java.awt.event.KeyEvent.VK_SPACE) && (onGround || onPrism || onCube)) {
            player.setVelocityY(15f * getDifficultyMultiplier());
        }
    }

    private void handlePhysicsAndCollision() {
        float gravity = -0.98f * getDifficultyMultiplier();
        float py = player.getY();
        player.addVelocityY(gravity);
        py += player.getVelocityY();

        float playerHalfSize = player.getHalfSize();
        float playerBottomY = py - playerHalfSize;
        float prismTop = world.platform.getY() + world.platform.height / 2f;
        float prismMinX = world.platform.getX() - world.platform.width / 2f;
        float prismMaxX = world.platform.getX() + world.platform.width / 2f;
        float prismMinZ = world.platform.getZ() - world.platform.depth / 2f;
        float prismMaxZ = world.platform.getZ() + world.platform.depth / 2f;
        float px = player.getX();
        float pz = player.getZ();
        boolean onPrismXZ = px >= prismMinX && px <= prismMaxX && pz >= prismMinZ && pz <= prismMaxZ;

        // Land on the platform
        if (onPrismXZ && playerBottomY < prismTop && playerBottomY > prismTop - 50 && player.getVelocityY() <= 0) {
            py = prismTop + player.getHalfSize(); // <-- This ensures the cube sits on top visually
            player.setVelocityY(0);
        }
        // Land on the ground
        if ((!onPrismXZ) && playerBottomY < world.groundY) {
            gameState = GameState.GAME_OVER;
            // Check if top 3
            if (Leaderboard.isTopScore(points)) {
                String name = javax.swing.JOptionPane.showInputDialog(
                    renderer.getFrame(),
                    "New High Score! Enter your name:",
                    "High Score",
                    javax.swing.JOptionPane.PLAIN_MESSAGE
                );
                if (name == null || name.trim().isEmpty()) name = "Player";
                Leaderboard.addScore(name.trim(), points);
            }
            renderer.setGameOverScreenVisible(true, points);
            renderer.getFrame().setCursor(Cursor.getDefaultCursor());
            return;
        }
        player.setY(py);

        // Assume player and cubes are centered at (x, y, z) and have size 20 (player) and 40 (cube)
        float playerSize = 20f;
        float playerHalf = playerSize / 2f;

        for (GameObject cube : world.movingCubes) {
            // Skip inside face collision if this cube is paired and player is between the two
            if (cube.pairedWith != null) {
                GameObject pair = cube.pairedWith;
                // If player is between the two cubes (adjacent in X or Z), skip collision for the shared face
                if (Math.abs(cube.getX() - pair.getX()) == 40f && Math.abs(player.getX() - (cube.getX() + pair.getX()) / 2f) < 20f &&
                    Math.abs(player.getY() - cube.getY()) < 20f && Math.abs(player.getZ() - cube.getZ()) < 20f) {
                    continue;
                }
                if (Math.abs(cube.getZ() - pair.getZ()) == 40f && Math.abs(player.getZ() - (cube.getZ() + pair.getZ()) / 2f) < 20f &&
                    Math.abs(player.getY() - cube.getY()) < 20f && Math.abs(player.getX() - cube.getX()) < 20f) {
                    continue;
                }
            }

            float cubeSize = 40f;
            float cubeHalf = cubeSize / 2f;

            // Player bounds
            float pxMin = player.getX() - playerHalf;
            float pxMax = player.getX() + playerHalf;
            float pyMin = player.getY() - playerHalf;
            float pyMax = player.getY() + playerHalf;
            float pzMin = player.getZ() - playerHalf;
            float pzMax = player.getZ() + playerHalf;

            // Cube bounds
            float cxMin = cube.getX() - cubeHalf;
            float cxMax = cube.getX() + cubeHalf;
            float cyMin = cube.getY() - cubeHalf;
            float cyMax = cube.getY() + cubeHalf;
            float czMin = cube.getZ() - cubeHalf;
            float czMax = cube.getZ() + cubeHalf;

            // Check for overlap in all 3 axes
            boolean overlapX = pxMax > cxMin && pxMin < cxMax;
            boolean overlapY = pyMax > cyMin && pyMin < cyMax;
            boolean overlapZ = pzMax > czMin && pzMin < czMax;

            if (overlapX && overlapY && overlapZ) {
                // Calculate overlap on each axis
                float[] overlaps = {
                    Math.min(pxMax, cxMax) - Math.max(pxMin, cxMin), // X
                    Math.min(pyMax, cyMax) - Math.max(pyMin, cyMin), // Y
                    Math.min(pzMax, czMax) - Math.max(pzMin, czMin)  // Z
                };

                // Find the axis of minimum penetration
                int minAxis = 0;
                for (int i = 1; i < 3; i++) {
                    if (overlaps[i] < overlaps[minAxis]) minAxis = i;
                }

                // Push player out along that axis
                if (minAxis == 0) { // X axis
                    if (player.getX() < cube.getX()) {
                        player.setX(cxMin - playerHalf);
                    } else {
                        player.setX(cxMax + playerHalf);
                    }
                } else if (minAxis == 1) { // Y axis
                    if (player.getY() < cube.getY()) {
                        // Player is below cube, prevent moving up into it
                        player.setY(cyMin - playerHalf);
                        // Only zero velocity if moving up into the cube
                        if (player.getVelocityY() > 0) {
                            player.setVelocityY(0);
                        }
                    } else {
                        // Player is above cube, land on top
                        player.setY(cyMax + playerHalf);
                        // Only zero velocity if falling onto the cube
                        if (player.getVelocityY() < 0) {
                            player.setVelocityY(0);
                        }
                    }
                } else { // Z axis
                    if (player.getZ() < cube.getZ()) {
                        player.setZ(czMin - playerHalf);
                    } else {
                        player.setZ(czMax + playerHalf);
                    }
                }
            }
        }
    }

    private void render() {
        float[] camPos = camera.getPosition(player.getX(), player.getY(), player.getZ());
        float[] lookAngles = camera.getLookAngles(camPos[0], camPos[1], camPos[2], player.getX(), player.getY(), player.getZ());
        renderer.setCamera(camPos[0], camPos[1], camPos[2], lookAngles[0], lookAngles[1]);
        renderer.setPlatform(world.platform);
        renderer.setPlayer(player);
        renderer.setObjectsToRender(world.movingCubes); // <-- Make sure this is the red cubes list!
    }

    public void stop() { running = false; }

    private float getDifficultyMultiplier() {
        // Increases by 1% per spawn, capped at 3x speed
        return Math.min(1f + 0.01f * spawnCount, 3f);
    }
}