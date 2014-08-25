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

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

import com.apple.eawt.FullScreenUtilities;

public class Engine {

	private static Engine instance = null;
	
	private static VidcherooControlFrame controlFrame;
	private static VidcherooMediaFrame mediaFrame;
	
	private static boolean isFullScreen = false;
	private static VidcherooStatus status = VidcherooStatus.NOFILES;
	private static float beatFraction = 1.0f;
	private static int beatSleepLength = 500;
	private static float tempo = 120f;
	
	protected Engine() {
		FileCrawler.getInstance().loadFileList();
		if (FileCrawler.getInstance().getFileListLength() > 0) {
			setStatus(VidcherooStatus.READY);
		}
		
		// Add key event dispatcher.
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(
				new KeyEventDispatcher()  { 
					public boolean dispatchKeyEvent(KeyEvent e){
						if(e.getID() == KeyEvent.KEY_PRESSED){
							handleKeyPress(e.getKeyCode());
						}
						return false;
					}
				});
	}

	public static Engine getInstance() {
		if (instance == null) {
			instance = new Engine();
		}
		return instance;
	}
	
	/**
	 * 
	 * @param keyCode
	 */
	private void handleKeyPress(int keyCode) {
		System.out.println("Key pressed: " + keyCode);
		
		switch (keyCode) {
		case 27:	// ESC
			if (isFullScreen) toggleFullScreen();
			break;
		case 32:
			play();
			break;
		case 49:
			setBeatFraction(5);
		default:
			break;
		}
	} 

	public void setControlFrame(VidcherooControlFrame controlFrame) {
		Engine.controlFrame = controlFrame;
		Engine.controlFrame.setTempoText(tempo);
	}

	public void setMediaFrame(VidcherooMediaFrame mediaFrame) {
		Engine.mediaFrame = mediaFrame;
		FullScreenUtilities.setWindowCanFullScreen(Engine.mediaFrame,true);
	}
	
	private static final int SLEEP_INTERVAL = 5;
	private static int sleepCounter = 0;
	
	//private Thread t;

	/**
	 * 
	 */
	public void play() {
		
		if (status == VidcherooStatus.NOFILES) {
			System.err.println("ERROR: No media files ready to play.");
			return;
		}
		
		if (status == VidcherooStatus.PLAYING) {
			Engine.sleepCounter = beatSleepLength;
		}
		
		if (status == VidcherooStatus.READY) {

			Thread t = new Thread() {
				public void run() {
					System.out.println("Starting new Engine Play thread.");
					Engine.getInstance().setStatus(VidcherooStatus.PLAYING);

					while (status == VidcherooStatus.PLAYING) {
						// Play the next file.
						Engine.mediaFrame.playMediaFile(FileCrawler.getInstance().getRandomMediaPath());

						// Sleep for one beat length.
						try {
							for (Engine.sleepCounter = 0; Engine.sleepCounter < beatSleepLength; Engine.sleepCounter += SLEEP_INTERVAL) {
								sleep(SLEEP_INTERVAL);
							}
							//sleep(beatSleepLength);
						} catch (InterruptedException ex) {
							System.err.println(ex.toString());
						}
					}
					Engine.mediaFrame.pause();
					System.out.println("Reached end of Engine Play thread.");
				}
			};
			t.start();
		}
	}

	public void pause() {
		if (status == VidcherooStatus.READY || status == VidcherooStatus.PLAYING) {
			Engine.mediaFrame.pause();
			Engine.sleepCounter = beatSleepLength;
			setStatus(VidcherooStatus.READY);
		}
	}
	
	public void toggleFullScreen() {
		// Toggle boolean first.
		isFullScreen = !isFullScreen;
		
		if (isFullScreen) mediaFrame.setWindowed(false);
		else mediaFrame.setWindowed(true);
	}
	
	private static final float MIN_TEMPO = 60.0f;
	private static final float MAX_TEMPO = 180.0f;

	public void setTempo(String tempoText) {
		float newTempo = 0.0f;
		
		// Attempt to read a float value from the given string.
		try {
			newTempo = Float.parseFloat(tempoText);
		} catch(Exception ex) {
			System.err.println(ex.toString());
		}
		
		// Only replace the tempo if a valid BPM value was given.
		if(newTempo >= MIN_TEMPO && newTempo <= MAX_TEMPO) {
			tempo = newTempo;
		} else {
			controlFrame.setStatusText(MIN_TEMPO + " < Tempo < " + MAX_TEMPO + "!");
		}
		updateBeatTime();
		controlFrame.setTempoText(tempo);
	}

	public void setBeatFraction(float newBeatFraction) {
		Engine.beatFraction = newBeatFraction;
		updateBeatTime();
	}
	
	public void shutdown() {
		System.out.println("Exiting Vidcheroo");
		status = VidcherooStatus.READY;
		Engine.sleepCounter = beatSleepLength;
		mediaFrame.stop();
	}
	
	/**
	 *  60s / BPM * beat fraction
	 */
	private void updateBeatTime() {
		beatSleepLength = (int) ((60.0f / (tempo * beatFraction)) * 1000.0f);
		System.out.println("New switch time: " + beatSleepLength);
	}
	
	private void setStatus(VidcherooStatus newStatus) {
		Engine.status = newStatus;
		
		if (controlFrame != null) {
			if (status == VidcherooStatus.READY) {
				controlFrame.setStatusText("Ready");
			} else if (status == VidcherooStatus.PLAYING) {
				controlFrame.setStatusText("Playing");
			} else {
				controlFrame.setStatusText("No media files found");
			}
		}
	}
}
