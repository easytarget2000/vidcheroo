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
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.lang.reflect.Method;
import java.util.Random;
import java.util.concurrent.locks.LockSupport;

/**
 * Singleton class that handles the play-back flow and Object communication of a Vidcheroo session.
 * 
 * @author michel@easy-target.org
 */
public class Engine {

	private static Engine instance = null;
	
	// Initialise worst status, improve from there.
	private static boolean didFindFeed		= false;
	private static boolean didFindVlc		= false;
	private static Status status 	= Status.NOTREADY;
	
	private static SupportedOperatingSystems os = SupportedOperatingSystems.UNK;
	private static ControlFrame controlFrame;
	private static VidcherooMediaFrame mediaFrame;
	
	private static boolean isFullScreen = false;
	private static float beatFraction = 1.0f;
	private static long beatSleepLength = 500l;
	
	/*
	 * Single Initialisation
	 */
	
	/**
	 * Constructor
	 * Starts the key event dispatcher.
	 */
	protected Engine() {
		System.out.println("Constructing Engine.");
		
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
		
		// Refresh status on controller frame.
		setStatus(status);
	}

	/**
	 * Call back to constructor
	 * 
	 * @return Static singleton instance
	 */
	public static Engine getInstance() {
		if (instance == null) {
			instance = new Engine();
		}
		return instance;
	}
	
	/*
	 * Public Getter/Setter Methods
	 */
	
	public static void setBeatFraction(float newBeatFraction) {
		Engine.beatFraction = newBeatFraction;
		updateBeatTime();
	}

	public static void setControlFrame(ControlFrame controlFrame) {
		Engine.controlFrame = controlFrame;
		Engine.controlFrame.setTempoText(Config.getTempo());
	}

	public static void setMediaFrame(VidcherooMediaFrame mediaFrame) {
		Engine.mediaFrame = mediaFrame;
		
		// Enable OS-specific full-screen modes.
		// To ensure platform independence, use reflection to get available classes after compilation.
		if (os == SupportedOperatingSystems.OSX) {
			
			//FullScreenUtilities.setWindowCanFullScreen(Window arg0, boolean arg1);
			try {
				Class<?> fullScreenUtilities = Class.forName("com.apple.eawt.FullScreenUtilities");
				Method setWindowCanFullScreen;
				setWindowCanFullScreen = fullScreenUtilities.getMethod("setWindowCanFullScreen", Window.class, boolean.class);
				//FullScreenUtilities.setWindowCanFullScreen(Engine.mediaFrame, true);
				setWindowCanFullScreen.invoke(Engine.mediaFrame, true);
			} catch (Exception e) {
				System.err.println("WARNING: Problem enabling OSX full-screen mode.");
				e.printStackTrace();
			}
		}
	}
	
	public static Status getStatus() {
		return status;
	}
	
	/**
	 * Changes the engine state and displays a message in the control frame.
	 * 
	 * @param newStatus		Status to change to
	 */
	public static void setStatus(Status newStatus) {
		Engine.status = newStatus;
		System.out.println("New Status: " + newStatus.toString());
		updateStatus();
	}
	
	private static void updateStatus() {
		if (controlFrame == null) return;
		
		// Go through statuses from "worst to best".
		if (!didFindVlc) {
			controlFrame.setStatusText("VLC not found.");
			controlFrame.setEnabled(true);
		} else if (!didFindFeed) {
			controlFrame.setStatusText("No media files found.");
			controlFrame.setEnabled(true);
		} else {
			switch (Engine.status) {
			case READY:
				if (mediaFrame == null) mediaFrame = new VidcherooMediaFrame();
				controlFrame.setStatusText("Ready.");
				controlFrame.setPlayControlEnabled(true);
				controlFrame.setPathControlEnabled(true);
				controlFrame.setEnabled(true);
				break;
			case PLAYING:
				controlFrame.setStatusText("Playing.");
				controlFrame.setPathControlEnabled(false);
				controlFrame.setEnabled(true);
				break;
			case PARSING:
				controlFrame.setStatusText("Analysing files.");
				controlFrame.setPlayControlEnabled(false);
				controlFrame.setPathControlEnabled(false);
				controlFrame.setEnabled(false);
				break;
			default:
				controlFrame.setStatusText("Status unknown.");
				controlFrame.setEnabled(false);
				break;
			}
		}
	}
	
	public static SupportedOperatingSystems getOs() {
		return os;
	}
	
	/**
	 * Let this instance of the application know the current OS,
	 * only if it is currently unknown.
	 * @param os	Operating system enumerator value
	 */
	public static void setOs(SupportedOperatingSystems os) {
		if (Engine.os == SupportedOperatingSystems.UNK) {
			Engine.os = os;
		}
	}
	
	/*
	 * Flow Control
	 */
	
	//TODO: Tweak these settings.
	private static final long SLEEP_INTERVAL = 4l;
	private static long sleepCounter = 0l;
	
	/**
	 * 
	 */
	public static void play() {
		if (status == Status.PLAYING) {
			Engine.sleepCounter = beatSleepLength;
		}
		
		if (status == Status.READY) {

			Thread playThread = new Thread() {
				public void run() {
					System.out.println("Starting new Engine Play thread.");
					setStatus(Status.PLAYING);
					
					// We will randomly skip through long videos.
					Random rand = new Random();
					
					while (status == Status.PLAYING) {
						// Play the next file.
						MediaFile mediaFile = MediaFileParser.getRandomMediaFile();
						
						float startTime = 0f;
						float skipMinLength = beatSleepLength/1000f + 0.1f;
						
						// Only bother checking for a different start time, if we are not switching very fast.
						if (beatSleepLength > 1000) {	
							float mediaLength = mediaFile.length / 1000.0f;
							if (mediaLength > skipMinLength) {
								// Skip to a random point in the media that will not let it reach the end of the file.
								startTime = rand.nextFloat() * (mediaLength - skipMinLength);
								System.out.println("Skipping to " + startTime + "/" + mediaLength);
							}
						}
						mediaFrame.playMediaFilePath(mediaFile.path, startTime);
						
						//TODO: Decide for sleep() or parkNanos().
						final long fNanoInterval = SLEEP_INTERVAL * 1000000l;

						// Sleep for one beat length.
						try {
							for (Engine.sleepCounter = 0l; Engine.sleepCounter < beatSleepLength; Engine.sleepCounter += SLEEP_INTERVAL) {
								//sleep(SLEEP_INTERVAL);
								LockSupport.parkNanos(fNanoInterval);
							}
							//sleep(beatSleepLength);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					Engine.mediaFrame.pause();
					System.out.println("Reached end of Engine Play thread.");
				}
			};
			
			// Call-back to run():
			playThread.start();
		}
	}

	/**
	 * Pause the player and finish the play thread by rising the sleep counter value.
	 */
	public static void pause() {
		// Only if we are playing, we can set the status to ready.
		if (status == Status.PLAYING) {
			Engine.mediaFrame.pause();
			Engine.sleepCounter = beatSleepLength;
			setStatus(Status.READY);
		}
	}
	
	/*
	 * Misc. Control
	 */
	
	/**
	 * Toggles full screen boolean variable and calls resizing method on media frame.
	 * Not used on OSX.
	 */
	public static void toggleFullscreen() {
		if (mediaFrame == null) return;
		
		boolean resumeAfterToggle = false;
		
		if (status == Status.PLAYING) {
			resumeAfterToggle = true;
			pause();
			mediaFrame.stop();
		}

		
		// Toggle boolean first.
		isFullScreen = !isFullScreen;
		
		if (isFullScreen) mediaFrame.setWindowed(false);
		else mediaFrame.setWindowed(true);
		
		if (resumeAfterToggle) play();
	}
	
	private static final int BLINK_TIME_SHOW = 600;
	private static final int BLINK_TIME_HIDE = 200;
	private static final short BLINK_REPEATS = 5;
	
	private static void blinkStatusText() {
		if (controlFrame == null) {
			System.err.println("ERROR: Control frame not initialised.");
			return;
		}
		
		updateStatus();
		blinkStatusText(controlFrame.getStatusText());
	}

	public static void blinkStatusText(String statusText) {
		if (controlFrame == null) {
			System.err.println("ERROR: Control frame not initialised.");
			return;
		}
		
		final String fStatusText = statusText;
		
		Thread blinkThread = new Thread() {
			public void run() {
				for (int i = 0; i < BLINK_REPEATS; i++) {
					// Hide the text, wait shortly, show the blink text, wait longer.
					try {
						controlFrame.setStatusText("");
						Thread.sleep(BLINK_TIME_HIDE);
						
						controlFrame.setStatusText(fStatusText);
						Thread.sleep(BLINK_TIME_SHOW);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				// Write the current status back into the control frame.
				setStatus(status);
			}
			
		};
		
		blinkThread.start();
	}
	
	public static void updateTempo() {
		Engine.updateBeatTime();
		if (controlFrame != null) {
			controlFrame.setTempoText(Config.getTempo());
		}
	}
	
	public static void shutdown() {
		System.out.println("Exiting Vidcheroo");
		status = Status.READY;
		Engine.sleepCounter = beatSleepLength;
		try {
			if (mediaFrame != null) {
				mediaFrame.stop();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Config.storeConfigProperties();		
		
		System.exit(0);
	}
	
	/**
	 *  60s / BPM * beat fraction
	 */
	private static void updateBeatTime() {
		float tempo = Config.getTempo();
		beatSleepLength = (int) ((60.0f / (tempo * beatFraction)) * 1000.0f);
		System.out.println("New switch time: " + beatSleepLength);
	}
	
	/**
	 * 
	 * @param keyCode
	 */
	private static void handleKeyPress(int keyCode) {
		System.out.println("Key pressed: " + keyCode);
		
		switch (keyCode) {
			case 27:	// ESC
				if (isFullScreen) toggleFullscreen();
				break;
			case 32:
				play();
				break;
			case 81:
				setBeatFraction(Beat.tempoMultipliers[0]);
				break;
			case 87:
				setBeatFraction(Beat.tempoMultipliers[1]);
				break;
			case 69:
				setBeatFraction(Beat.tempoMultipliers[2]);
				break;
			case 82:
				setBeatFraction(Beat.tempoMultipliers[3]);
				break;
			case 84:
				setBeatFraction(Beat.tempoMultipliers[4 ]);
				break;
			default:
				break;
		}
	}

	public static boolean hasFoundFeed() {
		return didFindFeed;
	}

	public static void setDidFindFeed(boolean didFindFeed) {
		Engine.didFindFeed = didFindFeed;
		updateStatus();
	}

	public static boolean hasFoundVlc() {
		return didFindVlc;
	}

	public static void setDidFindVlc(boolean didFindVlc) {
		if (!didFindVlc) blinkStatusText();
		
		Engine.didFindVlc = didFindVlc;
		updateStatus();
	}

}
