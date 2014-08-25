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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class VidcherooControlFrame extends JFrame {
	
	private static final long serialVersionUID = 201408251912L;
	
	private static int FRAME_INITIAL_X	= 40;
	private static int FRAME_INITIAL_Y	= 40;
	private static int FRAME_WIDTH		= 240;
	private static int FRAME_HEIGHT		= (int) (FRAME_WIDTH * 2.2f);
	private static int MARGIN			= 8;
	private static int ELEMENT_WIDTH	= FRAME_WIDTH - (2 * MARGIN);
	private static int ELEMENT_WIDTH_S	= ELEMENT_WIDTH / 2;
	private static int ELEMENT_HEIGHT	= 26;
	private static int ELEMENT_S_COL2_X = FRAME_WIDTH - MARGIN - ELEMENT_WIDTH_S;
	
	private static String[] BEAT_LENGTHS = {"1/16", "1/8", "1/4", "1/2"};
	
	private JLabel statusLabel = new JLabel("Waiting for engine...");
	private JTextField tempoTextField;
	
	public VidcherooControlFrame() {
		System.out.println("Initialising Control Frame.");
		
		setBounds(FRAME_INITIAL_X, FRAME_INITIAL_Y, FRAME_WIDTH, FRAME_HEIGHT + 20);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setLayout(null);
		setTitle("Vidcheroo Controller");
		
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
				Engine.getInstance().play();
			}
		});
		topPanel.add(playButton);
		
		// PAUSE Button:
		JButton pauseButton = new JButton("Pause");
		final int fTopPanelRow2Y = MARGIN + (ELEMENT_HEIGHT * 2) + MARGIN;
		pauseButton.setBounds(MARGIN, fTopPanelRow2Y, ELEMENT_WIDTH_S, ELEMENT_HEIGHT);
		pauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Engine.getInstance().pause();
			}
		});
		topPanel.add(pauseButton);
		
		// FULLSCREEN Button
		JButton fullscreenButton = new JButton("Fullscreen");
		fullscreenButton.setBounds(
				ELEMENT_S_COL2_X,
				fTopPanelRow2Y,
				ELEMENT_WIDTH_S, 
				ELEMENT_HEIGHT);
		fullscreenButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Engine.getInstance().toggleFullScreen();
			}
		});
		topPanel.add(fullscreenButton);
		
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
				Engine.getInstance().setTempo(tempoTextField.getText());				
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
		
		final int fBeatSectionRow1Y = fTempoSectionRow1Y + ELEMENT_HEIGHT + MARGIN;
		
		JButton beatButton1 = new JButton(BEAT_LENGTHS[0]);
		beatButton1.setBounds(MARGIN, fBeatSectionRow1Y, ELEMENT_WIDTH_S, ELEMENT_HEIGHT * 2);
		beatButton1.addActionListener(beatFracChanged);
		contentPane.add(beatButton1);
		
		JButton beatButton2 = new JButton(BEAT_LENGTHS[1]);
		beatButton2.setBounds(ELEMENT_S_COL2_X, fBeatSectionRow1Y, ELEMENT_WIDTH_S, ELEMENT_HEIGHT * 2);
		beatButton2.addActionListener(beatFracChanged);
		contentPane.add(beatButton2);
		
		final int fBeatSectionRow2Y = fBeatSectionRow1Y + (ELEMENT_HEIGHT * 2)  + MARGIN;
		
		JButton btnBeatFourth = new JButton(BEAT_LENGTHS[2]);
		btnBeatFourth.setBounds(MARGIN, fBeatSectionRow2Y, ELEMENT_WIDTH_S, ELEMENT_HEIGHT * 2);
		btnBeatFourth.addActionListener(beatFracChanged);
		contentPane.add(btnBeatFourth);
		
		JButton btnBeatHalf = new JButton(BEAT_LENGTHS[3]);
		btnBeatHalf.setBounds(ELEMENT_S_COL2_X, fBeatSectionRow2Y, ELEMENT_WIDTH_S, ELEMENT_HEIGHT * 2);
		btnBeatHalf.addActionListener(beatFracChanged);
		contentPane.add(btnBeatHalf);
		
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
		JButton mediaPathButton = new JButton("Select Media Path");
		mediaPathButton.addActionListener(openMediaPathSelector);
		mediaPathButton.setBounds(MARGIN, MARGIN, ELEMENT_WIDTH, ELEMENT_HEIGHT);
		bottomPanel.add(mediaPathButton);
		
		final int fBottomRow2Y = MARGIN + ELEMENT_HEIGHT + MARGIN;
		
		// FIND VLC Button:
		JButton vlcPathButton = new JButton("Select VLC Path");
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
//			// Initialise a JFileChooser acting as "directories only".
//			JFileChooser dirChooser = new JFileChooser();
//			String strDir;
//			
//			dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//			dirChooser.showSaveDialog(null);
//			strDir = dirChooser.getSelectedFile().getAbsolutePath();
//			
//			if (e.getActionCommand() == "")
//
//			switch (e.getActionCommand()) {
//			case ("Find Media Files"):
//				engine.loadFiles(strDir);
//				break;
//			default:
//				engine.setVlcPath(strDir);
//				break;
//			}
		}
	};
	
	ActionListener beatFracChanged = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			String actionCommand = e.getActionCommand();
			System.out.println("Changing beat length: " + actionCommand);
			
			if (actionCommand == BEAT_LENGTHS[0]) {
				Engine.getInstance().setBeatFraction(4.0f);	// 1/16
			} else if (actionCommand == BEAT_LENGTHS[1]) {
				Engine.getInstance().setBeatFraction(2.0f); // 1/8
			} else if (actionCommand == BEAT_LENGTHS[3]) {
				Engine.getInstance().setBeatFraction(0.5f); // 1/2
			} else {
				// Default is 1/4.
				Engine.getInstance().setBeatFraction(1.0f);
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
