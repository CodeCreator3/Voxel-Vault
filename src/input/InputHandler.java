package input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;
import javax.swing.SwingUtilities;

public class InputHandler implements KeyListener {
    private final Set<Integer> pressedKeys = new HashSet<>();

    public InputHandler() {
        // Attach this KeyListener to the main frame if available
        // This should be called after the JFrame is created in Renderer
        // You may need to expose a method in Renderer to add this listener
    }

    public void processInput() {
        // No-op for now; input is handled via KeyListener events
    }

    public boolean isKeyPressed(int keyCode) {
        return pressedKeys.contains(keyCode);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        pressedKeys.add(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }

    // Utility method to attach this handler to a JFrame
    public void attachToFrame(javax.swing.JFrame frame) {
        SwingUtilities.invokeLater(() -> frame.addKeyListener(this));
    }
}