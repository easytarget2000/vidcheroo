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

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Method;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

public class VidcherooControlFrame extends JFrame {
	
	private static final long serialVersionUID = 201408251912L;
	
	
	private static final int FRAME_INITIAL_X	= 40;
	private static final int FRAME_INITIAL_Y	= 40;
	private static final int FRAME_WIDTH		= 240;
	private static final int FRAME_HEIGHT		= (int) (FRAME_WIDTH * 2.2f);
	private static final int MARGIN				= 8;
	private static final int ELEMENT_WIDTH		= FRAME_WIDTH - (2 * MARGIN) - 5;
	private static final int ELEMENT_WIDTH_S	= (int) (ELEMENT_WIDTH * 0.49);
	private static final int ELEMENT_HEIGHT		= 26;
	private static final int ELEMENT_S_COL2_X 	= FRAME_WIDTH - MARGIN - ELEMENT_WIDTH_S;
	
	private static final boolean APPLY_DESIGN	= Engine.getOs() != SupportedOperatingSystems.OSX;
	private static final Color COLOR_1			= new Color(246, 127, 1);
	private static final Color COLOR_2			= Color.WHITE;
	private static final Color COLOR_3			= new Color(255, 147, 21);
		
	private JLabel statusLabel = new JLabel("Waiting for engine...");
	private JTextField tempoTextField;
	private JButton mediaPathButton, vlcPathButton;
	
	public VidcherooControlFrame() {
		System.out.println("Initialising Control Frame.");
		System.out.println("Applying colours: " + APPLY_DESIGN);
		
		setBounds(FRAME_INITIAL_X, FRAME_INITIAL_Y, FRAME_WIDTH, FRAME_HEIGHT + 20);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                new ExitDialog(null);
            }
        });
		setResizable(false);
		setLayout(null);
		setTitle("Vidcheroo Controller");
		
		/*
		 * Application Icon
		 */
		
		java.net.URL url = ClassLoader.getSystemResource(Launcher.ICON_PATH);
		Toolkit kit = Toolkit.getDefaultToolkit();
		Image image = kit.createImage(url);
		setIconImage(image);

		if (Engine.getOs() == SupportedOperatingSystems.OSX) {
			try {
				Class<?> application = Class.forName("com.apple.eawt.Application");
				Method getApplication;
				getApplication = application.getMethod("getApplication", new Class[0]);
				Object app = getApplication.invoke(null);
				Method setDockImage = application.getMethod("setDockIconImage", Image.class);
				setDockImage.invoke(app, image);
				//Application application = Application.getApplication();
				//application.setDockIconImage(image);
			} catch (Exception e) {
				System.err.println("ERROR: Cannot load application icon.");
				e.printStackTrace();
			}
		}
		
		JPanel contentPane = (JPanel) getContentPane();
		System.out.println("Controller content pane dimensions: " + contentPane.getBounds().toString());
//		contentPane.setBounds(new Rectangle(0, 0, FRAME_WIDTH, FRAME_HEIGHT));
		contentPane.setBackground(COLOR_2);
		
		/*
		 * Top Panel:
		 */
		
		JPanel topPanel = new JPanel();
		final int fTopPanelHeight = MARGIN + (2 * ELEMENT_HEIGHT) + MARGIN + ELEMENT_HEIGHT + MARGIN;
		topPanel.setBounds(0, 0, FRAME_WIDTH, fTopPanelHeight);
		topPanel.setLayout(null);
		if (APPLY_DESIGN) topPanel.setBackground(COLOR_1);
		contentPane.add(topPanel);

		// PLAY Button:
		JButton playButton = new JButton("Play");
		playButton.setBounds(MARGIN, MARGIN, ELEMENT_WIDTH, ELEMENT_HEIGHT * 2);
		playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Engine.play();
			}
		});
		if (APPLY_DESIGN) {
			playButton.setForeground(COLOR_1);
			playButton.setBackground(COLOR_2);
			playButton.setBorderPainted(false);
		}
		topPanel.add(playButton);
		
		final int fTopPanelRow2Y = MARGIN + (ELEMENT_HEIGHT * 2) + MARGIN;
		final boolean fShowFullscrnBtn = Engine.getOs() != SupportedOperatingSystems.OSX;
		
		// PAUSE Button:
		JButton pauseButton = new JButton("Pause");
		
		// If a full-screen button is displayed, shorten the pause button.
		int pauseButtonWidth;
		if (fShowFullscrnBtn) pauseButtonWidth = ELEMENT_WIDTH_S;
		else pauseButtonWidth = ELEMENT_WIDTH;
		
		pauseButton.setBounds(MARGIN, fTopPanelRow2Y, pauseButtonWidth, ELEMENT_HEIGHT);
		pauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Engine.pause();
			}
		});
		if (APPLY_DESIGN) {
			pauseButton.setForeground(COLOR_2);
			pauseButton.setBackground(COLOR_1);
			pauseButton.setBorderPainted(false);
		}
		topPanel.add(pauseButton);
		
		if (fShowFullscrnBtn) {
			// FULLSCREEN Button
			JButton fullscreenButton = new JButton("Fullscreen");
			fullscreenButton.setBounds(
					ELEMENT_S_COL2_X,
					fTopPanelRow2Y,
					ELEMENT_WIDTH_S, 
					ELEMENT_HEIGHT);
			fullscreenButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Engine.toggleFullScreen();
				}
			});
			if (APPLY_DESIGN) {
				fullscreenButton.setForeground(COLOR_2);
				fullscreenButton.setBackground(COLOR_1);
				fullscreenButton.setBorderPainted(false);
			}
			topPanel.add(fullscreenButton);
		}
		
		/*
		 * Tempo Section
		 */
		
		// TEMPO Label:
		JLabel tempoLabel = new JLabel("Tempo");
		//TODO: Calculate width of tempo label.
		final int fTempoLabelWidth = ELEMENT_WIDTH_S / 2;
		final int fTempoSectionRow1Y = fTopPanelHeight + MARGIN;
		tempoLabel.setBounds(MARGIN, fTempoSectionRow1Y, fTempoLabelWidth, ELEMENT_HEIGHT);
		if (APPLY_DESIGN) tempoLabel.setForeground(COLOR_1);
		contentPane.add(tempoLabel);
		
		// TEMPO Text Field:
		tempoTextField = new JTextField();
		tempoTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				VidcherooConfig.setTempo(tempoTextField.getText());				
			}
		});
		tempoTextField.setBounds(
				MARGIN + fTempoLabelWidth,
				fTempoSectionRow1Y,
				FRAME_WIDTH - MARGIN - fTempoLabelWidth - MARGIN,
				ELEMENT_HEIGHT);
		tempoTextField.setColumns(5);
		if (APPLY_DESIGN) {
			tempoTextField.setForeground(COLOR_1);
			tempoTextField.setBackground(COLOR_2);
			tempoTextField.setBorder(null);
		}
		contentPane.add(tempoTextField);

		/*
		 * Beat Length Buttons
		 */
		
		int beatButtonY = fTempoSectionRow1Y + ELEMENT_HEIGHT + MARGIN;
		
		for (int i = 0; i < BeatHandler.readableBeatLengths.length; i++) {
			JButton beatButton = new JButton(BeatHandler.readableBeatLengths[i]);
			
			// Even buttons are left, uneven buttons are right.
			int beatButtonX;
			if (i % 2 == 0) beatButtonX = MARGIN;
			else beatButtonX = ELEMENT_S_COL2_X;
						
			beatButton.addActionListener(beatFracChanged);
			beatButton.setBounds(beatButtonX, beatButtonY, ELEMENT_WIDTH_S, ELEMENT_HEIGHT * 2);
			if (APPLY_DESIGN) {
				beatButton.setForeground(COLOR_2);
				beatButton.setBackground(COLOR_3);
				beatButton.setBorderPainted(false);
			}
			contentPane.add(beatButton);
			
			// Move Y down if this is an uneven button.
			if (i % 2 != 0) beatButtonY += (ELEMENT_HEIGHT * 2) + MARGIN;
		}
		
		/*
		 * Bottom Panel
		 */
		
		JPanel bottomPanel = new JPanel();
		int bottomPanelHeight = MARGIN + ELEMENT_HEIGHT + MARGIN + ELEMENT_HEIGHT + ELEMENT_HEIGHT;
		//bottomPanelHeight = 150;
		//TODO: Figure out correct y value.
		bottomPanel.setBounds(
				0,
				FRAME_HEIGHT - bottomPanelHeight - 5,
				FRAME_WIDTH,
				bottomPanelHeight
				);
		bottomPanel.setLayout(null);
		if (APPLY_DESIGN) bottomPanel.setBackground(COLOR_1);
		contentPane.add(bottomPanel);
		
		// FIND MEDIA FILES Button:
		mediaPathButton = new JButton("Select Media Path");
		mediaPathButton.addActionListener(openPathListener);
		mediaPathButton.setBounds(MARGIN, MARGIN, ELEMENT_WIDTH, ELEMENT_HEIGHT);
		if (APPLY_DESIGN) {
			mediaPathButton.setForeground(COLOR_2);
			mediaPathButton.setBackground(COLOR_1);
			mediaPathButton.setBorderPainted(false);
		}
		bottomPanel.add(mediaPathButton);
		
		final int fBottomRow2Y = MARGIN + ELEMENT_HEIGHT + MARGIN;
		
		// FIND VLC Button:
		vlcPathButton = new JButton("Select VLC Path");
		vlcPathButton.addActionListener(openPathListener);
		vlcPathButton.setBounds(MARGIN, fBottomRow2Y, ELEMENT_WIDTH, ELEMENT_HEIGHT);
		if (APPLY_DESIGN) {
			vlcPathButton.setForeground(COLOR_2);
			vlcPathButton.setBackground(COLOR_1);
			vlcPathButton.setBorderPainted(false);
		}
		bottomPanel.add(vlcPathButton);
				
		// STATUS Label:
		statusLabel.setBounds(
				MARGIN,
				fBottomRow2Y + ELEMENT_HEIGHT,
				ELEMENT_WIDTH,
				ELEMENT_HEIGHT
				);
		if (APPLY_DESIGN) statusLabel.setForeground(COLOR_2);
		bottomPanel.add(statusLabel);
		
		setVisible(true);
	}
	
	ActionListener openPathListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			// Initialise a JFileChooser acting as "directories only".
			String chosenDir = chooseDir();
			if (chosenDir == null) return;
			System.out.println("Chosen Directory: " + chosenDir);
			
			if (e.getSource().equals(mediaPathButton)) {
				VidcherooConfig.setMediaPath(chosenDir);
			} else if (e.getSource().equals(vlcPathButton)){
				boolean didSetVlcPath = VidcherooConfig.setVlcPath(chosenDir);
				if (!didSetVlcPath) chooseDir();
			} else {
				System.err.println("Unknown action event source: " + e.getSource());
			}
		}
		
		private String chooseDir() {
			JFileChooser dirChooser = new JFileChooser();
			dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			dirChooser.showSaveDialog(null);
			
			if (dirChooser.getSelectedFile() == null) return null;
			else return dirChooser.getSelectedFile().getAbsolutePath();
		}
	};
	
	ActionListener beatFracChanged = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			String actionCommand = e.getActionCommand();
			System.out.println("Changing beat length: " + actionCommand);
			
			boolean selectionIsValid = false;
			
			for (int i = 0; i < BeatHandler.readableBeatLengths.length; i++) {
				if (actionCommand == BeatHandler.readableBeatLengths[i]) {
					Engine.setBeatFraction(BeatHandler.tempoMultipliers[i]);
					selectionIsValid = true;
				}
			}
			
			if (!selectionIsValid) {
				// Default is 1/4.
				Engine.setBeatFraction(BeatHandler.tempoMultipliers[2]);
			}
			
		}
	};

	public void setStatusText(String status) {
		statusLabel.setText(status);
	}
	
	public void setTempoText(float tempo) {
		tempoTextField.setText(String.format("%g", tempo));
	}
}
