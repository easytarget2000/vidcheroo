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

public class Engine {

	private static Engine instance = null;
	
	private static VidcherooControlFrame controlFrame;
	private static VidcherooMediaFrame mediaFrame;
	
	private static VidcherooStatus status = VidcherooStatus.NOFILES;
	private static float beatFraction = 1.0f;
	private static int beatTime = 500;
	private static float tempo = 120f;
	
	protected Engine() {
		FileCrawler.getInstance().loadFileList();
		if (FileCrawler.getInstance().getFileListLength() > 0) {
			setStatus(VidcherooStatus.READY);
		}
	}

	public static Engine getInstance() {
		if (instance == null) {
			instance = new Engine();
		}
		return instance;
	}

	public void setControlFrame(VidcherooControlFrame controlFrame) {
		Engine.controlFrame = controlFrame;
		Engine.controlFrame.setTempoText(tempo);
	}

	public void setMediaFrame(VidcherooMediaFrame mediaFrame) {
		Engine.mediaFrame = mediaFrame;
	}

	public void play() {
		if (status != VidcherooStatus.NOFILES) {
			// Always go back into ready state before playing to finish old threads.
			setStatus(VidcherooStatus.READY);

			Thread t = new Thread() {
				public void run() {
					System.out.println("Starting new Engine Play thread.");
					Engine.getInstance().setStatus(VidcherooStatus.PLAYING);

					while (status == VidcherooStatus.PLAYING) {
						// Play the next file.
						mediaFrame.playMediaFile(FileCrawler.getInstance().getRandomMediaPath());

						// Sleep for one beat length.
						try {
							sleep(beatTime);
						} catch (InterruptedException ex) {
							System.err.println(ex.toString());
						}
					}
					System.out.println("Reached end of Engine Play thread.");
				}
			};
			t.start();
		}
	}

	public void pause() {
		if (status == VidcherooStatus.READY || status == VidcherooStatus.PLAYING) {
			setStatus(VidcherooStatus.READY);
			mediaFrame.pause();
		}
	}

	public void toggleFullscreen() {
		// TODO Auto-generated method stub
		
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
		controlFrame.setTempoText(tempo);
	}

	public void setBeatFraction(float newBeatFraction) {
		Engine.beatFraction = newBeatFraction;
		updateBeatTime();
	}
	
	/**
	 *  60s / BPM * beat fraction
	 */
	private void updateBeatTime() {
		beatTime = (int) ((60.0f / (tempo * beatFraction)) * 1000.0f);
		System.out.println("New switch time: " + beatTime);
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
