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
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class VidcherooControlFrame extends JFrame {
	
	private static final long serialVersionUID = 201408251015L;
	
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
	
	public VidcherooControlFrame() {
		System.out.println("Initialising Control Frame.");
		
		setTitle("Vidcheroo Controller");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(FRAME_INITIAL_X, FRAME_INITIAL_Y, FRAME_WIDTH, FRAME_HEIGHT);
		setResizable(false);
		
		JPanel contentPane = new JPanel();
		contentPane.setBounds(new Rectangle(0, 0, FRAME_WIDTH, FRAME_HEIGHT));
		contentPane.setBackground(Color.WHITE);
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
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
				Engine.getInstance().toggleFullscreen();
			}
		});
		topPanel.add(fullscreenButton);
		
		/*
		 * Tempo Section
		 */
		
		// TEMPO Label:
		JLabel tempoLabel = new JLabel("Tempo");
		final int fTempoLabelWidth = 40;
		final int fTempoSectionRow1Y = fTopPanelHeight + MARGIN;
		tempoLabel.setBounds(MARGIN, fTempoSectionRow1Y, fTempoLabelWidth, ELEMENT_HEIGHT);
		contentPane.add(tempoLabel);
		
		// TEMPO Text Field:
		final JTextField fTempoTextField = new JTextField();
		fTempoTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Engine.getInstance().setTempo(fTempoTextField.getText());				
			}
		});
		fTempoTextField.setBounds(
				MARGIN + fTempoLabelWidth,
				fTempoSectionRow1Y,
				FRAME_WIDTH - MARGIN - fTempoLabelWidth - MARGIN,
				ELEMENT_HEIGHT);
		contentPane.add(fTempoTextField);
		fTempoTextField.setColumns(5);

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
		int bottomPanelHeight = MARGIN + ELEMENT_HEIGHT + MARGIN + ELEMENT_HEIGHT + MARGIN + ELEMENT_HEIGHT;
		bottomPanel.setBounds(
				0,
				FRAME_HEIGHT - bottomPanelHeight,
				FRAME_WIDTH,
				bottomPanelHeight
				);
		contentPane.add(bottomPanel);
		bottomPanel.setLayout(null);
		
		// FIND MEDIA FILES Button:
		JButton mediaPathButton = new JButton("Select Media Path");
		mediaPathButton.addActionListener(openMediaPathSelector);
		mediaPathButton.setBounds(MARGIN,MARGIN, ELEMENT_WIDTH, ELEMENT_HEIGHT);
		bottomPanel.add(mediaPathButton);
		
		final int fBottomRow2Y = MARGIN + ELEMENT_HEIGHT + MARGIN;
		
		// FIND VLC Button:
		JButton vlcPathButton = new JButton("Select VLC Path");
		vlcPathButton.setBounds(MARGIN, fBottomRow2Y, ELEMENT_WIDTH, ELEMENT_HEIGHT);
		bottomPanel.add(vlcPathButton);
				
		// STATUS Label:
		statusLabel.setBounds(
				MARGIN,
				fBottomRow2Y + ELEMENT_HEIGHT + MARGIN,
				ELEMENT_WIDTH,
				ELEMENT_HEIGHT
				);
		bottomPanel.add(statusLabel);
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
//			System.out.println("Changing speed.");
//
//			switch (e.getActionCommand()) {
//			case "1/16":
//				engine.setBeatFraction(4.0);
//				break;
//			case "1/8":
//				engine.setBeatFraction(2.0);
//				break;
//			case "1/2":
//				engine.setBeatFraction(0.5);
//				break;
//			default:
//				engine.setBeatFraction(1.0);
//				break;
//			}
		}
	};

	public void setStatusText(String status) {
		statusLabel.setText(status);
	}
}
