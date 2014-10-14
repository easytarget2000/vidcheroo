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

	/**
	 * Static singleton instance
	 */
	private static Engine instance = null;
	
	// Initialise worst status, improve from there.
	
	/**
	 * Status flag that stores if a list of media files is available.
	 */
	private static boolean didFindFeed = false;
	
	/**
	 * Status flag that stores if the VLC libraries have been found.
	 */
	private static boolean didFindVlc = false;
	
	/**
	 * General engine status
	 */
	private static Status status = Status.NOTREADY;
	
	/**
	 * Operating system that this instance is running on.
	 */
	private static SupportedOperatingSystems os = SupportedOperatingSystems.UNK;
	
	/**
	 * Control frame with buttons and the status label
	 */
	private static ControlFrame controlFrame;
	
	/**
	 * Media frame that contains the VLC player.
	 */
	private static MediaFrame mediaFrame;
	
	/**
	 * The engine knows if the media frame is in full-screen mode,
	 * so that it can behave accordingly when the toggle key is pressed.
	 */
	private static boolean isFullScreen = false;
	
	/**
	 * Tempo multiplier to speed up or slow down switch times.
	 * 1.0 for quarter notes, 4.0 for 1/16th notes, 0.25 for full notes.
	 * Should only have values that are available in NoteLength class.
	 */
	private static float tempoMultiplier = 1.0f;
	
	/**
	 * Duration in ms to let the play thread sleep
	 */
	private static long noteSleepLength = 500l;
	
	/**
	 * Time value in ms that is used for rhythmically looping short videos
	 * Typically the length of a 1/8th note
	 */
	private static long noteModuloLength = 250l;
	
	/*
	 * SINGLETON INITIALISATION
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
	 * @return Static singleton instance
	 */
	public static Engine getInstance() {
		if (instance == null) {
			instance = new Engine();
		}
		return instance;
	}
	
	/**
	 * Used by key event dispatcher.
	 * @param keyCode Numeric value of the pressed key
	 */
	private static void handleKeyPress(int keyCode) {
		System.out.println("Key pressed: " + keyCode);
		
		switch (keyCode) {
			case 27:	// ESC
				if (isFullScreen) toggleFullscreen();
				//TODO: Close full-screen on OSX.
				break;
			case 32:
				play();
				break;
			case 81:
				setTempoMultiplier(0);
				break;
			case 87:
				setTempoMultiplier(1);
				break;
			case 69:
				setTempoMultiplier(2);
				break;
			case 82:
				setTempoMultiplier(3);
				break;
			case 84:
				setTempoMultiplier(4);
				break;
			case 79:
				System.err.println("---MANUAL MARKER---");
				break;
			default:
				break;
		}
	}
	
	/*
	 * PUBLIC GETTER / SETTER
	 */
	
	/**
	 * Attribute getter method
	 * @return Value of didFindFeed
	 */
	public static boolean hasFoundFeed() {
		return didFindFeed;
	}

	/**
	 * Attribute setter method
	 * Updates status.
	 * @param didFindFeed New value of didFindFeed
	 */
	public static void setDidFindFeed(boolean didFindFeed) {
		Engine.didFindFeed = didFindFeed;
		updateStatus();
	}

	/**
	 * Attribute getter method
	 * @return Value of didFindVlc
	 */
	public static boolean hasFoundVlc() {
		return didFindVlc;
	}

	/**
	 * Attribute setter method
	 * Blinks on false and updates status.
	 * @param didFindVlc New value of didFindVlc
	 */
	public static void setDidFindVlc(boolean didFindVlc) {
		if (!didFindVlc) blinkStatusText();
		
		Engine.didFindVlc = didFindVlc;
		updateStatus();
	}
	
	/**
	 * Attribute setter method
	 * Prepares the media frame for the current operating system.
	 * @param mediaFrame Media frame of this session
	 */
	public static void setMediaFrame(MediaFrame mediaFrame) {
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
	
	/**
	 * Changes the current note length and updates the sleep time.
	 * @param noteLengthIndex Index of the note length multiplier in NoteLength class
	 */
	public static void setTempoMultiplier(int noteLengthIndex) {
		if (NoteLength.tempoMultipliers.length < noteLengthIndex) {
			System.err.println("ERROR: Cannot find note length for index " + noteLengthIndex + ".");
		} else {
			tempoMultiplier = NoteLength.tempoMultipliers[noteLengthIndex];
			updateSleepLength();
			controlFrame.setLengthButtonHighlighted(noteLengthIndex);
		}
	}

	/**
	 * Attribute setter method
	 * Updates the tempo text field.
	 * @param controlFrame Control frame of this session
	 */
	public static void setControlFrame(ControlFrame controlFrame) {
		Engine.controlFrame = controlFrame;
		Engine.controlFrame.setTempoText(ConfigurationHandler.getTempo());
	}
	
	/**
	 * @return Status attribute
	 */
	public static Status getStatus() {
		return status;
	}
	
	/**
	 * Changes the engine state and displays a message in the control frame.
	 * @param newStatus		Status to change to
	 */
	public static void setStatus(Status newStatus) {
		Engine.status = newStatus;
		System.out.println("New Status: " + newStatus.toString());
		updateStatus();
	}
	
	/**
	 * Looks at flags and the Status object to determine "readable" status.
	 * The control frame GUI will be updated accordingly.
	 */
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
				if (mediaFrame == null) mediaFrame = new MediaFrame();
				mediaFrame.setVisible(true);
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
				if (mediaFrame != null) {
					mediaFrame.setVisible(false);
				}
				break;
			default:
				controlFrame.setStatusText("Status unknown.");
				controlFrame.setEnabled(false);
				break;
			}
		}
	}
	
	/**
	 * @return Current OS attribute
	 */
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
	 * FLOW CONTROL
	 */
	
	//TODO: Tweak sleep length settings.
	
	/**
	 * Length of sleep steps in ms
	 */
	private static final long SLEEP_INTERVAL = 4l;
	
	/**
	 * Videos that are longer than this might not start playing at 0 sec.
	 */
	private static final float SKIP_MIN_LENGTH = 6000l;
	
	/**
	 * Counts the sleep steps. Values in ms.
	 */
	private static long sleepCounter = 0l;
		
	/**
	 * Puts the Engine into Playing state and creates a thread that loops forever until this state is left.
	 * The thread loads a media file into the player, sleeps and then loads another one.
	 * The length of the sleep depends on the current tempo and "note" length
	 */
	public static void play() {
		if (status == Status.PLAYING) {
			sleepCounter = noteSleepLength;
		}
		
		if (status == Status.READY) {
			
			/*
			 * BEAT DETECTOR TESTING
			 */
			BeatDetector.getInstance();
			//BeatDetector.start();

			Thread playThread = new Thread() {
				public void run() {
					System.out.println("Starting new Engine Play thread.");
					setStatus(Status.PLAYING);
					
					// We will randomly skip through long videos.
					Random rand = new Random();
					
					while (status == Status.PLAYING) {
						// Play the next file.
						MediaFile mediaFile = MediaFileParser.getRandomMediaFile();
						System.out.println(" " + mediaFile.path);
						
						// Generally start a media file at t=0ms.
						long startTime = 0l;
						
						// The length of the media file in ms.
						long mediaLength = mediaFile.length;
						
						// Only bother checking for a different start time, if we are not switching very fast.
						if (noteSleepLength > 1000) {	
							if (mediaLength > SKIP_MIN_LENGTH) {
								// Skip to a random point in the media that will not let it reach the end of the file.
								startTime = (long) (rand.nextFloat() * (mediaLength - SKIP_MIN_LENGTH));
								System.out.println("Skipping to " + startTime + "/" + mediaLength + " of " + mediaFile.path);
							}
						}
						mediaFrame.playMediaFilePath(mediaFile.path, startTime);
						
						// Time value in ms at which the media file will be reset to the start.
						// The max. multiple of 1/8th notes in mediaLength - startTime
						long repeatTime = mediaLength - startTime;
						repeatTime -= repeatTime % noteModuloLength;
						if (repeatTime <= 0l) {
							System.err.println("WARNING: repeatTime=" + repeatTime + ". Will be reset.");
							repeatTime = noteModuloLength;
						}
						
						//DEBUG
						System.out.println("  " + mediaLength + " startTime=" + startTime + " repeatTime=" + repeatTime);

						final long fNanoInterval = SLEEP_INTERVAL * 1000000l;

						// Sleep for one beat length.
						try {
							for (sleepCounter = 0l; sleepCounter < noteSleepLength; sleepCounter += SLEEP_INTERVAL) {
								// If the sleep duration is reaching the time at which this file is ending,
								// call play() to stop the current media and ask for a new one.
								if (sleepCounter > repeatTime) {
									System.out.println("Force switch at " + sleepCounter + " ms.");
									play();
									break;
								}
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
			Engine.sleepCounter = noteSleepLength;
			setStatus(Status.READY);
		}
	}
	
	/*
	 * MISC CONTROL
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
	
	/**
	 * Length in ms during which the blinking text is visible
	 */
	private static final int BLINK_TIME_SHOW = 600;
	
	/**
	 * Length in ms during which the blinking text is not visible
	 */
	private static final int BLINK_TIME_HIDE = 200;
	
	/**
	 * Number of status text blinks
	 */
	private static final short BLINK_REPEATS = 5;
	
	/**
	 * Lets the currently displayed status text blink.
	 */
	private static void blinkStatusText() {
		if (controlFrame == null) {
			System.err.println("ERROR: Control frame not initialised.");
			return;
		}
		
		updateStatus();
		blinkStatusText(controlFrame.getStatusText());
	}

	/**
	 * Lets a given status text blink by setting the text, sleeping,
	 * setting no text, sleeping again and repeating this.
	 * @param statusText Text to temporarily put into status label and blink.
	 */
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
	
	/**
	 * Recalculates the note length and write the current tempo into the control frame.
	 */
	public static void updateTempo() {
		updateSleepLength();
		float tempo = ConfigurationHandler.getTempo();
		noteModuloLength = (long) ((60f / (tempo * 2f)) * 1000f);

		if (controlFrame != null) {
			controlFrame.setTempoText(ConfigurationHandler.getTempo());
		}
	}
	
	/**
	 * Stop the play thread, stop the video, store the settings and exit.
	 */
	public static void shutdown() {
		System.out.println("Exiting Vidcheroo");
		status = Status.READY;
		Engine.sleepCounter = noteSleepLength;
		try {
			if (mediaFrame != null) {
				mediaFrame.stop();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ConfigurationHandler.storeConfigProperties();		
		
		System.exit(0);
	}
	
	/**
	 * Calculates the current note length in ms.
	 * 60 sec. / BPM * tempo multiplier
	 * The value is multiplied with 1000 to get the millisecond value. 
	 */
	private static void updateSleepLength() {
		float tempo = ConfigurationHandler.getTempo();
		noteSleepLength = (long) ((60f / (tempo * tempoMultiplier)) * 1000f);
		System.out.println("New switch time: " + noteSleepLength);
	}
}
