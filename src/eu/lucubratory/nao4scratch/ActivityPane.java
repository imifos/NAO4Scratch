package eu.lucubratory.nao4scratch;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import javax.swing.JPanel;

/**
 * Draws a rotating wheel to show activity.
 * <p/>
 * From: https://stackoverflow.com/questions/17586116/rotating-wheel-in-swing
 */
public class ActivityPane extends JPanel {

    private float cycle=1f;
    private boolean invert = false;

    public ActivityPane() {
    }

    public void signal() {
        cycle += 0.05f;
        if (cycle > 1f) {
            cycle = 0f;
            invert = !invert;
        }
        repaint();
    }
    
     public void reset() {
        cycle=0.0f;
        invert=true;
        repaint();
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(40, 40);
    }

    @Override
    protected void paintComponent(Graphics g) {
        
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        int width = getWidth() - 1;
        int height = getHeight() - 1;
        int radius = Math.min(width, height);

        int x = (width - radius) / 2;
        int y = (height - radius) / 2;
        int start = 0;
        int extent = Math.round(cycle * 360f);

        if (invert) {
            start = extent;
            extent = 360 - extent;
        }

        g2d.setColor(Color.GREEN);
        g2d.fill(new Arc2D.Float(x, y, radius, radius, start, extent, Arc2D.PIE));
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.draw(new Arc2D.Float(x, y, radius, radius, start, extent, Arc2D.PIE));
        g2d.dispose();
    }
}
