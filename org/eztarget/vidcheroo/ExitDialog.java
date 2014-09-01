/*
 * Copyright (C) 2014 Easy Target
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eztarget.vidcheroo;

import java.awt.Point;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class ExitDialog extends JDialog{
	private static final long serialVersionUID = 933929761127142977L;
	
	private static final int WIDTH	= 600;
	private static final int HEIGHT = 80; 

	public ExitDialog(JFrame frame) {
		super(frame, "", true);

		final JOptionPane optionPane = new JOptionPane(
			    "Do you really want to exit Vidcheroo?",
			    JOptionPane.QUESTION_MESSAGE,
			    JOptionPane.YES_NO_OPTION
			    );
		
		
		setContentPane(optionPane);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	
		// Centre the dialog.
		double screenCentreX = Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2;
		double screenCentreY = Toolkit.getDefaultToolkit().getScreenSize().getHeight() /2;
		setBounds(
				(int) (screenCentreX - (WIDTH / 2)),
				(int) (screenCentreY - (HEIGHT / 2)),
				WIDTH,
				HEIGHT
				);
		
		optionPane.addPropertyChangeListener(
				new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent e) {
						String prop = e.getPropertyName();

						if (isVisible() 
								&& (e.getSource() == optionPane)
								&& (prop.equals(JOptionPane.VALUE_PROPERTY))) {
							// If you were going to check something before closing the window,
							// you'd do it here.
							setVisible(false);
						}
					}
				});
		pack();
		setVisible(true);

		int value = JOptionPane.NO_OPTION;
		try {
			value = ((Integer)optionPane.getValue()).intValue();
		} catch (Exception e) {
			optionPane.getValue();
		}
		
		if (value == JOptionPane.YES_OPTION) {
			Engine.shutdown();
		} else if (value == JOptionPane.NO_OPTION) {
			System.out.println("Only closing exit dialog. Application stays alive.");
		}
	}
}
