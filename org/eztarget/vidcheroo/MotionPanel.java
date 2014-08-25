package org.eztarget.vidcheroo;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class MotionPanel extends JComponent{
    private Point initialClick;
    private JFrame parent;

    public MotionPanel(final JFrame parent){
    	this.parent = parent;

    	addMouseListener(new MouseAdapter() {
    		public void mousePressed(MouseEvent e) {
    			initialClick = e.getPoint();
    			getComponentAt(initialClick);
    		}
    	});

    	addMouseMotionListener(new MouseMotionAdapter() {
    		@Override
    		public void mouseDragged(MouseEvent e) {

    			// get location of Window
    			int thisX = parent.getLocation().x;
    			int thisY = parent.getLocation().y;

    			// Determine how much the mouse moved since the initial click
    			int xMoved = (thisX + e.getX()) - (thisX + initialClick.x);
    			int yMoved = (thisY + e.getY()) - (thisY + initialClick.y);

    			// Move window to this position
    			int X = thisX + xMoved;
    			int Y = thisY + yMoved;
    			parent.setLocation(X, Y);
    		}
    	});
    	
    	setOpaque(true);
    }
}
