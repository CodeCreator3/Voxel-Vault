package graphics;

import engine.Leaderboard;
import objects.GameObject;
import objects.RectangularPrismWithWireframe;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class Renderer extends JPanel {
    private JFrame frame;
    private List<GameObject> objectsToRender;

    private float cameraX, cameraY, cameraZ, cameraYaw, cameraPitch;

    // Add a field and setter for the platform:
    private RectangularPrismWithWireframe platform;

    private GameObject player;

    // Add this field and setter:
    private int points = 0;
    public void setPoints(int points) { this.points = points; }

    private boolean showTitleScreen = false;
    private JButton playButton;

    private boolean showPauseScreen = false;
    private JButton resumeButton, quitButton;

    // Add these setters:
    private Runnable onResume, onQuit;
    public void setPauseActions(Runnable onResume, Runnable onQuit) {
        this.onResume = onResume;
        this.onQuit = onQuit;
    }

    private boolean showGameOverScreen = false;
    private int finalScore = 0;
    private JButton restartButton, menuButton;

    // Add these setters:
    private Runnable onRestart, onMenu;
    public void setGameOverActions(Runnable onRestart, Runnable onMenu) {
        this.onRestart = onRestart;
        this.onMenu = onMenu;
    }

    // Add these fields:
    private boolean showSettingsScreen = false;
    private JButton settingsButton, settingsBackButton, fullscreenButton;
    private boolean isFullscreen = false;

    public void initialize() {
        frame = new JFrame("3D Game Renderer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.add(this);
        frame.setVisible(true);

        // Hide the cursor
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Cursor blankCursor = toolkit.createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
        frame.setCursor(blankCursor);

        // Keep buttons centered on resize/fullscreen
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateMenuButtonPositions();
            }
        });
    }

    // Call this to update the list of objects to render
    public void setObjectsToRender(List<GameObject> objects) {
        this.objectsToRender = objects;
        repaint();
    }

    public void renderObject(GameObject obj) {
        // Not used directly; rendering is handled in paintComponent
    }

    public void clearScreen() {
        repaint();
    }

    public void setCamera(float x, float y, float z, float yaw, float pitch) {
        this.cameraX = x;
        this.cameraY = y;
        this.cameraZ = z;
        this.cameraYaw = yaw;
        this.cameraPitch = pitch;
    }

    // Add this setter for the platform:
    public void setPlatform(RectangularPrismWithWireframe platform) {
        this.platform = platform;
    }

    public void setPlayer(GameObject player) {
        this.player = player;
    }

    public void setTitleScreenVisible(boolean visible) {
        showTitleScreen = visible;
        int centerX = getWidth() / 2 - 100; // Centered for 200px wide buttons

        // Move Play button higher
        if (playButton != null) playButton.setVisible(visible);
        if (settingsButton == null) {
            settingsButton = new JButton("Settings");
            settingsButton.setFont(new Font("Arial", Font.BOLD, 24));
            settingsButton.setBounds(centerX, 320, 200, 50); // Settings button stays at 320
            settingsButton.addActionListener(e -> setSettingsScreenVisible(true));
            setLayout(null);
            add(settingsButton);
        }
        settingsButton.setVisible(visible);
        repaint();
    }

    public void setPlayButtonListener(Runnable onPlay) {
        int centerX = getWidth() / 2 - 100;
        if (playButton == null) {
            playButton = new JButton("Play");
            playButton.setFont(new Font("Arial", Font.BOLD, 32));
            playButton.setBounds(centerX, 230, 200, 60); // Play button at 230
            playButton.addActionListener(e -> onPlay.run());
            setLayout(null);
            add(playButton);
        }
        playButton.setVisible(showTitleScreen);
    }

    public void setPauseScreenVisible(boolean visible) {
        showPauseScreen = visible;
        if (resumeButton == null) {
            resumeButton = new JButton("Resume");
            resumeButton.setFont(new Font("Arial", Font.BOLD, 28));
            resumeButton.setBounds(300, 250, 200, 60);
            resumeButton.addActionListener(e -> {
                setPauseScreenVisible(false);
                if (onResume != null) onResume.run();
            });
            setLayout(null);
            add(resumeButton);
        }
        if (quitButton == null) {
            quitButton = new JButton("Quit");
            quitButton.setFont(new Font("Arial", Font.BOLD, 28));
            quitButton.setBounds(300, 330, 200, 60);
            quitButton.addActionListener(e -> {
                if (onQuit != null) onQuit.run();
            });
            setLayout(null);
            add(quitButton);
        }
        if (settingsButton == null) {
            settingsButton = new JButton("Settings");
            settingsButton.setFont(new Font("Arial", Font.BOLD, 24));
            settingsButton.setBounds(300, 410, 200, 50);
            settingsButton.addActionListener(e -> setSettingsScreenVisible(true));
            setLayout(null);
            add(settingsButton);
        }
        resumeButton.setVisible(visible);
        quitButton.setVisible(visible);
        settingsButton.setVisible(visible && !showSettingsScreen); // <-- Only show if not in settings screen

        updateMenuButtonPositions();
        repaint();
    }

    public void setGameOverScreenVisible(boolean visible, int score) {
        showGameOverScreen = visible;
        finalScore = score;
        if (restartButton == null) {
            restartButton = new JButton("Restart");
            restartButton.setFont(new Font("Arial", Font.BOLD, 28));
            restartButton.setBounds(300, 300, 200, 60);
            restartButton.addActionListener(e -> {
                setGameOverScreenVisible(false, 0);
                if (onRestart != null) onRestart.run();
            });
            setLayout(null);
            add(restartButton);
        }
        if (menuButton == null) {
            menuButton = new JButton("Menu");
            menuButton.setFont(new Font("Arial", Font.BOLD, 28));
            menuButton.setBounds(300, 380, 200, 60);
            menuButton.addActionListener(e -> {
                setGameOverScreenVisible(false, 0);
                if (onMenu != null) onMenu.run();
            });
            setLayout(null);
            add(menuButton);
        }
        int centerX = getWidth() / 2 - 100; // 200px wide buttons
        if (restartButton != null) {
            if (showGameOverScreen) {
                restartButton.setBounds(centerX, 320, 200, 60); // Centered and a bit lower
                restartButton.setVisible(true);
            } else {
                restartButton.setVisible(false);
            }
        }
        if (menuButton != null) {
            if (showGameOverScreen) {
                menuButton.setBounds(centerX, 400, 200, 60); // Centered and below restart
                menuButton.setVisible(true);
            } else {
                menuButton.setVisible(false);
            }
        }
        repaint();
    }

    // --- Settings screen logic ---
    public void setSettingsScreenVisible(boolean visible) {
        showSettingsScreen = visible;

        // Hide all other menu buttons when in settings screen
        if (playButton != null) playButton.setVisible(false);
        if (resumeButton != null) resumeButton.setVisible(false);
        if (quitButton != null) quitButton.setVisible(false);
        if (settingsButton != null) settingsButton.setVisible(false);
        if (restartButton != null) restartButton.setVisible(false);
        if (menuButton != null) menuButton.setVisible(false);

        // Show/hide settings menu buttons
        if (settingsBackButton == null) {
            settingsBackButton = new JButton("Back");
            settingsBackButton.setFont(new Font("Arial", Font.BOLD, 24));
            settingsBackButton.setBounds(getWidth() / 2 - 100, 300, 200, 50);
            settingsBackButton.addActionListener(e -> {
                setSettingsScreenVisible(false);
                // Return to previous menu
                if (showPauseScreen) setPauseScreenVisible(true);
                else setTitleScreenVisible(true);
                repaint();
            });
            setLayout(null);
            add(settingsBackButton);
        }
        if (fullscreenButton == null) {
            fullscreenButton = new JButton("Toggle Fullscreen");
            fullscreenButton.setFont(new Font("Arial", Font.BOLD, 24));
            fullscreenButton.setBounds(getWidth() / 2 - 150, 220, 300, 50);
            fullscreenButton.addActionListener(e -> toggleFullscreen());
            setLayout(null);
            add(fullscreenButton);
        }
        settingsBackButton.setVisible(visible);
        fullscreenButton.setVisible(visible);

        updateMenuButtonPositions();
        repaint();
    }

    private void toggleFullscreen() {
        if (!isFullscreen) {
            frame.dispose();
            frame.setUndecorated(true);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setVisible(true);
            isFullscreen = true;
        } else {
            frame.dispose();
            frame.setUndecorated(false);
            frame.setExtendedState(JFrame.NORMAL);
            frame.setSize(800, 600);
            frame.setVisible(true);
            isFullscreen = false;
        }
        updateMenuButtonPositions(); // <-- Add this line
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (showTitleScreen) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("Voxel Vault", getWidth()/2 - 130, 100); // Title

            // Move leaderboard higher up
            int leaderboardY = 410; // Was 470, now 390
            g.setFont(new Font("Arial", Font.BOLD, 28));
            g.drawString("Leaderboard:", getWidth()/2 - 100, leaderboardY);
            java.util.List<Leaderboard.Entry> entries = Leaderboard.load();
            for (int i = 0; i < entries.size(); i++) {
                Leaderboard.Entry e = entries.get(i);
                g.drawString((i+1) + ". " + e.name + " - " + e.score, getWidth()/2 - 100, leaderboardY + 40 + i*36);
            }
            return;
        }

        // Draw the platform
        if (platform != null) {
            drawRectangularPrismWithWireframe(g,
                platform.getX(), platform.getY(), platform.getZ(),
                platform.width, platform.height, platform.depth, platform.getColor());
        }

        // Draw the player as a blue cube
        if (player != null) {
            drawCube(g, player.getX(), player.getY(), player.getZ(), 20, player.getColor());
        }

        // Draw other objects (red cubes)
        if (objectsToRender != null) {
            for (GameObject obj : objectsToRender) {
                drawCube(g, obj.getX(), obj.getY(), obj.getZ(), 40, obj.getColor());
            }
        }

        // Draw the points in the top left
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Points: " + points, 20, 32);

        if (showGameOverScreen) {
            g.setColor(new Color(0, 0, 0, 200));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("Game Over", getWidth()/2 - 140, 200);
            g.setFont(new Font("Arial", Font.BOLD, 32));
            g.drawString("Score: " + finalScore, getWidth()/2 - 80, 260);
            // Buttons are real JButtons, so no need to draw them here
            return;
        }

        if (showSettingsScreen) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("Settings", getWidth()/2 - 120, 200);
            // Buttons are real JButtons, so no need to draw them here
            return;
        }
    }

    // Draws a rectangular prism centered at (cx, cy, cz) with given width, height, depth, and wireframe surface
    private void drawRectangularPrismWithWireframe(Graphics g, float cx, float cy, float cz, float width, float height, float depth, Color color) {
        // 8 vertices of the prism
        float[][] vertices = {
            {-width/2, -height/2, -depth/2}, {width/2, -height/2, -depth/2}, {width/2, height/2, -depth/2}, {-width/2, height/2, -depth/2},
            {-width/2, -height/2, depth/2},  {width/2, -height/2, depth/2},  {width/2, height/2, depth/2},  {-width/2, height/2, depth/2}
        };
        int[][] edges = {
            {0,1},{1,2},{2,3},{3,0}, // back face
            {4,5},{5,6},{6,7},{7,4}, // front face
            {0,4},{1,5},{2,6},{3,7}  // connections
        };

        int[] sx = new int[8];
        int[] sy = new int[8];
        int w = getWidth();
        int h = getHeight();

        for (int i = 0; i < 8; i++) {
            float x = cx + vertices[i][0];
            float y = cy + vertices[i][1];
            float z = cz + vertices[i][2];
            float[] cam = getCameraSpace(x, y, z);
            float[] proj = projectCameraSpace(cam, w, h);
            sx[i] = (int) proj[0];
            sy[i] = (int) proj[1];
        }

        // Draw main edges with bold stroke
        Graphics2D g2 = (Graphics2D) g;
        Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(3)); // Bold lines
        g2.setColor(color);
        for (int[] edge : edges) {
            // Use projectAndDrawLine for proper near-plane clipping
            projectAndDrawLine(g2,
                cx + vertices[edge[0]][0], cy + vertices[edge[0]][1], cz + vertices[edge[0]][2],
                cx + vertices[edge[1]][0], cy + vertices[edge[1]][1], cz + vertices[edge[1]][2]
            );
        }
        g2.setStroke(oldStroke);

        // Draw wireframe on surfaces (vertical and horizontal lines on top face) in the same color, but normal thickness
        g2.setColor(color);
        int wireCount = 5;
        // On top face (between vertices 3-2-6-7)
        for (int i = 1; i < wireCount; i++) {
            float alpha = i / (float)wireCount;
            float xA = lerp(vertices[3][0], vertices[2][0], alpha) + cx;
            float yA = lerp(vertices[3][1], vertices[2][1], alpha) + cy;
            float zA = lerp(vertices[3][2], vertices[2][2], alpha) + cz;
            float xB = lerp(vertices[7][0], vertices[6][0], alpha) + cx;
            float yB = lerp(vertices[7][1], vertices[6][1], alpha) + cy;
            float zB = lerp(vertices[7][2], vertices[6][2], alpha) + cz;
            projectAndDrawLine(g2, xA, yA, zA, xB, yB, zB);
        }
        // On top face (between vertices 3-7 and 2-6)
        for (int i = 1; i < wireCount; i++) {
            float alpha = i / (float)wireCount;
            float xA = lerp(vertices[3][0], vertices[7][0], alpha) + cx;
            float yA = lerp(vertices[3][1], vertices[7][1], alpha) + cy;
            float zA = lerp(vertices[3][2], vertices[7][2], alpha) + cz;
            float xB = lerp(vertices[2][0], vertices[6][0], alpha) + cx;
            float yB = lerp(vertices[2][1], vertices[6][1], alpha) + cy;
            float zB = lerp(vertices[2][2], vertices[6][2], alpha) + cz;
            projectAndDrawLine(g2, xA, yA, zA, xB, yB, zB);
        }
    }

    // Draws a wireframe cube centered at (cx, cy, cz) with given size
    private void drawCube(Graphics g, float cx, float cy, float cz, float size, Color color) {
        // Should use size/2 for offsets from center
        float[][] vertices = {
            {-size/2, -size/2, -size/2}, {size/2, -size/2, -size/2},
            {size/2, size/2, -size/2},   {-size/2, size/2, -size/2},
            {-size/2, -size/2, size/2},  {size/2, -size/2, size/2},
            {size/2, size/2, size/2},    {-size/2, size/2, size/2}
        };
        int[][] edges = {
            {0,1},{1,2},{2,3},{3,0},
            {4,5},{5,6},{6,7},{7,4},
            {0,4},{1,5},{2,6},{3,7}
        };

        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(color);

        // Use projectAndDrawLine for all edges to ensure proper near-plane clipping
        for (int[] edge : edges) {
            float x1 = cx + vertices[edge[0]][0];
float y1 = cy + vertices[edge[0]][1];
float z1 = cz + vertices[edge[0]][2];
float x2 = cx + vertices[edge[1]][0];
float y2 = cy + vertices[edge[1]][1];
float z2 = cz + vertices[edge[1]][2];
            projectAndDrawLine(g2, x1, y1, z1, x2, y2, z2);
        }
    }

    // Draws a wireframe ground grid centered at y = 0
    private void drawGroundGrid(Graphics g, int gridX, int gridZ, float spacing) {
        g.setColor(Color.LIGHT_GRAY);
        int halfX = gridX / 2;
        int halfZ = gridZ / 2;
        for (int x = -halfX; x <= halfX; x++) {
            // Line along Z
            projectAndDrawLine(g,
                x * spacing, 0, -halfZ * spacing,
                x * spacing, 0, halfZ * spacing
            );
        }
        for (int z = -halfZ; z <= halfZ; z++) {
            // Line along X
            projectAndDrawLine(g,
                -halfX * spacing, 0, z * spacing,
                halfX * spacing, 0, z * spacing
            );
        }
    }

    // Projects two 3D points and draws a line between them
    private void projectAndDrawLine(Graphics g, float x1, float y1, float z1, float x2, float y2, float z2) {
        int w = getWidth();
        int h = getHeight();
        float nearPlane = 1f; // Slightly larger for stability
        float epsilon = 1e-4f;

        // Get camera-space coordinates for both points
        float[] cam1 = getCameraSpace(x1, y1, z1);
        float[] cam2 = getCameraSpace(x2, y2, z2);

        // If both points are behind the near plane, don't draw
        if (cam1[2] < nearPlane - epsilon && cam2[2] < nearPlane - epsilon) return;

        // If one point is behind the near plane, clip the line
        if (cam1[2] < nearPlane - epsilon || cam2[2] < nearPlane - epsilon) {
            float[] in, out;
            if (cam1[2] < nearPlane - epsilon) {
                in = cam2;
                out = cam1;
            } else {
                in = cam1;
                out = cam2;
            }
            float t = (nearPlane - in[2]) / (out[2] - in[2]);
            float ix = in[0] + t * (out[0] - in[0]);
            float iy = in[1] + t * (out[1] - in[1]);
            float iz = nearPlane;
            if (cam1[2] < nearPlane - epsilon) {
                cam1 = new float[]{ix, iy, iz};
            } else {
                cam2 = new float[]{ix, iy, iz};
            }
        }

        // Project both points
        float[] p1 = projectCameraSpace(cam1, w, h);
        float[] p2 = projectCameraSpace(cam2, w, h);

        g.drawLine((int)p1[0], (int)p1[1], (int)p2[0], (int)p2[1]);
    }

    // Converts world coordinates to camera space (after camera transform and rotation)
    private float[] getCameraSpace(float x, float y, float z) {
        // Camera position
        float px = x - cameraX;
        float py = y - cameraY;
        float pz = z - cameraZ;

        // Build camera axes (assuming yaw/pitch FPS camera)
        double yawRad = Math.toRadians(cameraYaw);
        double pitchRad = Math.toRadians(cameraPitch);

        // Forward (N)
        float nx = (float)(Math.sin(yawRad) * Math.cos(pitchRad));
        float ny = (float)(-Math.sin(pitchRad));
        float nz = (float)(Math.cos(yawRad) * Math.cos(pitchRad));

        // Up (V)
        float upx = 0, upy = 1, upz = 0;

        // Right (U) = up x forward
        float ux = upy * nz - upz * ny;
        float uy = upz * nx - upx * nz;
        float uz = upx * ny - upy * nx;

        // Normalize U
        float ul = (float)Math.sqrt(ux*ux + uy*uy + uz*uz);
        ux /= ul; uy /= ul; uz /= ul;

        // Recompute V = N x U (guarantees orthogonality)
        float vx = ny * uz - nz * uy;
        float vy = nz * ux - nx * uz;
        float vz = nx * uy - ny * ux;

        // Project point onto camera axes
        float camX = px * ux + py * uy + pz * uz;
        float camY = px * vx + py * vy + pz * vz;
        float camZ = px * nx + py * ny + pz * nz;

        return new float[]{camX, camY, camZ};
    }

    // Projects a camera-space point to screen coordinates
    private float[] projectCameraSpace(float[] cam, int w, int h) {
        float perspective = 400 / (cam[2]);
        float sx = w / 2 + cam[0] * perspective;
        float sy = h / 2 - cam[1] * perspective;
        return new float[]{sx, sy};
    }

    public JFrame getFrame() {
        return frame;
    }

    // Add this helper method:
    private float lerp(float a, float b, float t) {
        return a * (1 - t) + b * t;
    }

    public void updateMenuButtonPositions() {
        int centerX = getWidth() / 2 - 100; // 200px wide buttons

        if (playButton != null)
            playButton.setBounds(centerX, 230, 200, 60);

        // Only show settingsButton when NOT in settings screen
        if (settingsButton != null) {
            if (showSettingsScreen) {
                settingsButton.setVisible(false);
            } else if (showPauseScreen) {
                settingsButton.setBounds(centerX, 410, 200, 50);
                settingsButton.setVisible(true);
            } else if (showTitleScreen) {
                settingsButton.setBounds(centerX, 320, 200, 50);
                settingsButton.setVisible(true);
            } else {
                settingsButton.setVisible(false); // Hide during gameplay and other screens
            }
        }
        if (fullscreenButton != null) {
            fullscreenButton.setBounds(centerX - 50, 220, 300, 50);
            fullscreenButton.setVisible(showSettingsScreen);
        }
        if (settingsBackButton != null) {
            settingsBackButton.setBounds(centerX, 300, 200, 50);
            settingsBackButton.setVisible(showSettingsScreen);
        }

        if (resumeButton != null)
            resumeButton.setBounds(centerX, 250, 200, 60);
        if (quitButton != null)
            quitButton.setBounds(centerX, 330, 200, 60);

        // Settings menu buttons: only visible in settings screen, spaced out
        if (fullscreenButton != null) {
            fullscreenButton.setBounds(centerX - 50, 220, 300, 50); // Higher up
            fullscreenButton.setVisible(showSettingsScreen);
        }
        if (settingsBackButton != null) {
            settingsBackButton.setBounds(centerX, 300, 200, 50); // Below fullscreen button, no overlap
            settingsBackButton.setVisible(showSettingsScreen);
        }

        if (restartButton != null) {
            if (showGameOverScreen) {
                restartButton.setBounds(centerX, 320, 200, 60); // Centered and a bit lower
                restartButton.setVisible(true);
            } else {
                restartButton.setVisible(false);
            }
        }
        if (menuButton != null) {
            if (showGameOverScreen) {
                menuButton.setBounds(centerX, 400, 200, 60); // Centered and below restart
                menuButton.setVisible(true);
            } else {
                menuButton.setVisible(false);
            }
        }
    }
}