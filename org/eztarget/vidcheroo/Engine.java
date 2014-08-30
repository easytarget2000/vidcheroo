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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

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
		MediaFileParser.parseFileList();
		
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
	private static void handleKeyPress(int keyCode) {
		System.out.println("Key pressed: " + keyCode);
		
		switch (keyCode) {
			case 27:	// ESC
				if (isFullScreen) toggleFullScreen();
				break;
			case 32:
				play();
				break;
			case 49:
				setBeatFraction(BeatHandler.tempoMultipliers[0]);
				break;
			case 50:
				setBeatFraction(BeatHandler.tempoMultipliers[1]);
				break;
			case 51:
				setBeatFraction(BeatHandler.tempoMultipliers[2]);
				break;
			case 52:
				setBeatFraction(BeatHandler.tempoMultipliers[3]);
				break;
			default:
				break;
		}
	} 

	public static void setControlFrame(VidcherooControlFrame controlFrame) {
		Engine.controlFrame = controlFrame;
		Engine.controlFrame.setTempoText(tempo);
	}

	public static void setMediaFrame(VidcherooMediaFrame mediaFrame) {
		Engine.mediaFrame = mediaFrame;
		FullScreenUtilities.setWindowCanFullScreen(Engine.mediaFrame,true);
	}
	
	private static final int SLEEP_INTERVAL = 5;
	private static int sleepCounter = 0;
	
	//private Thread t;

	/**
	 * 
	 */
	public static void play() {
		
		if (status == VidcherooStatus.NOFILES) {
			System.err.println("ERROR: No media files ready to play.");
			return;
		}
		
		if (status == VidcherooStatus.PLAYING) {
			Engine.sleepCounter = beatSleepLength;
		}
		
		if (status == VidcherooStatus.READY) {

			Thread playThread = new Thread() {
				public void run() {
					System.out.println("Starting new Engine Play thread.");
					setStatus(VidcherooStatus.PLAYING);
					
					final int fSkipMinLength = beatSleepLength * 2;

					while (status == VidcherooStatus.PLAYING) {
						// Play the next file.
						VidcherooMediaFile mediaFile = MediaFileParser.getRandomMediaFile();
						mediaFrame.playMediaFilePath(mediaFile.path);
						
						// Only bother checking for skip-aheads, if we are not switching very fast.
						if (beatSleepLength > 300) {
							
							long mediaLength = mediaFile.length;
							if (mediaLength > fSkipMinLength) {
								// Skip to a random point in the media that will not let it reach the end of the file.
								long skipTime = (long) (Math.random() * (mediaLength - beatSleepLength - 10));
								System.out.println("Skipping to " + skipTime);
								mediaFrame.setMediaTime(skipTime);
							}
						}

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
			playThread.start();
		}
	}

	public static void pause() {
		if (status == VidcherooStatus.READY || status == VidcherooStatus.PLAYING) {
			Engine.mediaFrame.pause();
			Engine.sleepCounter = beatSleepLength;
			setStatus(VidcherooStatus.READY);
		}
	}
	
	public static void toggleFullScreen() {
		// Toggle boolean first.
		isFullScreen = !isFullScreen;
		
		if (isFullScreen) mediaFrame.setWindowed(false);
		else mediaFrame.setWindowed(true);
	}
	
	public static void setStatus(VidcherooStatus newStatus) {
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
	
	private static final float MIN_TEMPO = 60.0f;
	private static final float MAX_TEMPO = 180.0f;

	public static void setTempo(String tempoText) {
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

	public static void setBeatFraction(float newBeatFraction) {
		Engine.beatFraction = newBeatFraction;
		updateBeatTime();
	}
	
	public static void shutdown() {
		System.out.println("Exiting Vidcheroo");
		status = VidcherooStatus.READY;
		Engine.sleepCounter = beatSleepLength;
		mediaFrame.stop();
	}
	
	/**
	 *  60s / BPM * beat fraction
	 */
	private static void updateBeatTime() {
		beatSleepLength = (int) ((60.0f / (tempo * beatFraction)) * 1000.0f);
		System.out.println("New switch time: " + beatSleepLength);
	}
	
	private static final String CONFIG_PROPERTIES_FILE = "config.properties";
	private static final String CONFIG_KEY_MEDIA_PATH = "media_path";
	
	private static void storeConfigProperties() {
		Properties prop = new Properties();
		OutputStream output = null;
	 
		try {
	 
			output = new FileOutputStream(CONFIG_PROPERTIES_FILE);
	 
			// set the properties value
			prop.setProperty("database", "localhost");
			prop.setProperty("dbuser", "mkyong");
			prop.setProperty("dbpassword", "password");
	 
			// save properties to project root folder
			prop.store(output, null);
	 
		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	 
		}
	}
	
}