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
	
	private static final String ICON_PATH = "org/eztarget/vidcheroo/resources/icon.png";
	
	private static final int FRAME_INITIAL_X	= 40;
	private static final int FRAME_INITIAL_Y	= 40;
	private static final int FRAME_WIDTH		= 240;
	private static final int FRAME_HEIGHT		= (int) (FRAME_WIDTH * 2.2f);
	private static final int MARGIN				= 8;
	private static final int ELEMENT_WIDTH		= FRAME_WIDTH - (2 * MARGIN);
	private static final int ELEMENT_WIDTH_S	= ELEMENT_WIDTH / 2;
	private static final int ELEMENT_HEIGHT		= 26;
	private static final int ELEMENT_S_COL2_X 	= FRAME_WIDTH - MARGIN - ELEMENT_WIDTH_S;
	
	//TODO: Determine OS.
	private static final boolean IS_OSX = true;
		
	private JLabel statusLabel = new JLabel("Waiting for engine...");
	private JTextField tempoTextField;
	private JButton mediaPathButton, vlcPathButton;
	
	public VidcherooControlFrame() {
		System.out.println("Initialising Control Frame.");
		
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
		java.net.URL url = ClassLoader.getSystemResource(ICON_PATH);
		Toolkit kit = Toolkit.getDefaultToolkit();
		Image image = kit.createImage(url);
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
		contentPane.setBackground(Color.WHITE);
		
		/*
		 * Top Panel:
		 */
		
		JPanel topPanel = new JPanel();
		final int fTopPanelHeight = MARGIN + (2 * ELEMENT_HEIGHT) + MARGIN + ELEMENT_HEIGHT + MARGIN;
		topPanel.setBounds(0, 0, FRAME_WIDTH, fTopPanelHeight);
		contentPane.add(topPanel);
		topPanel.setLayout(null);
		
		// PLAY Button:
		JButton playButton = new JButton("Play");
		playButton.setBounds(MARGIN, MARGIN, ELEMENT_WIDTH, ELEMENT_HEIGHT * 2);
		playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Engine.play();
			}
		});
		topPanel.add(playButton);
		
		final int fTopPanelRow2Y = MARGIN + (ELEMENT_HEIGHT * 2) + MARGIN;
		
		// PAUSE Button:
		JButton pauseButton = new JButton("Pause");
		
		// If a full-screen button is displayed, shorten the pause button.
		int pauseButtonWidth;
		if (IS_OSX) pauseButtonWidth = ELEMENT_WIDTH;
		else pauseButtonWidth = ELEMENT_WIDTH_S;
		
		pauseButton.setBounds(MARGIN, fTopPanelRow2Y, pauseButtonWidth, ELEMENT_HEIGHT);
		pauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Engine.pause();
			}
		});
		topPanel.add(pauseButton);
		
		if (!IS_OSX) {
			// FULLSCREEN Button
			JButton fullscreenButton = new JButton("Full Screen");
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
		contentPane.add(tempoTextField);
		tempoTextField.setColumns(5);

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
				FRAME_HEIGHT - bottomPanelHeight,
				FRAME_WIDTH,
				bottomPanelHeight
				);
		//bottomPanel.setBackground(Color.BLUE);
		bottomPanel.setLayout(null);
		contentPane.add(bottomPanel);
		
		// FIND MEDIA FILES Button:
		mediaPathButton = new JButton("Select Media Path");
		mediaPathButton.addActionListener(openMediaPathSelector);
		mediaPathButton.setBounds(MARGIN, MARGIN, ELEMENT_WIDTH, ELEMENT_HEIGHT);
		bottomPanel.add(mediaPathButton);
		
		final int fBottomRow2Y = MARGIN + ELEMENT_HEIGHT + MARGIN;
		
		// FIND VLC Button:
		vlcPathButton = new JButton("Select VLC Path");
		vlcPathButton.addActionListener(openMediaPathSelector);
		vlcPathButton.setBounds(MARGIN, fBottomRow2Y, ELEMENT_WIDTH, ELEMENT_HEIGHT);
		bottomPanel.add(vlcPathButton);
				
		// STATUS Label:
		statusLabel.setBounds(
				MARGIN,
				fBottomRow2Y + ELEMENT_HEIGHT,
				ELEMENT_WIDTH,
				ELEMENT_HEIGHT
				);
		bottomPanel.add(statusLabel);
		
		setVisible(true);
	}
	
	ActionListener openMediaPathSelector = new ActionListener() {
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
			
			if (actionCommand == BeatHandler.readableBeatLengths[0]) {
				Engine.setBeatFraction(BeatHandler.tempoMultipliers[0]); // 1/16
			} else if (actionCommand == BeatHandler.readableBeatLengths[1]) {
				Engine.setBeatFraction(BeatHandler.tempoMultipliers[1]); // 1/8
			} else if (actionCommand == BeatHandler.readableBeatLengths[3]) {
				Engine.setBeatFraction(BeatHandler.tempoMultipliers[3]); // 1/2
			} else {
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
