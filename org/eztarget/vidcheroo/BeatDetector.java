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
import java.io.ByteArrayOutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class BeatDetector {
	
	/**
	 * Static singleton instance
	 */
	private static BeatDetector instance = null;
	
	/**
	 * 
	 */
	private static boolean isRunning = false;
	
	/**
	 *
	 */
	private static TargetDataLine line;
	
	/**
	 * Constructor
	 * Starts the key event dispatcher.
	 */
	protected BeatDetector() {
		AudioFormat format = new AudioFormat(8000, 8, 1, true, true);
	    System.out.println("Starting beat detector with audio format " + format.toString() + ".");
	    
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, 
				format); // format is an AudioFormat object
		if (!AudioSystem.isLineSupported(info)) {
			System.err.println("ERROR: " + info + " is not supported.");
			//TODO: Make sure nothing in this class is started.
			return;
		}
		
		// Obtain and open the line.
		try {
			line = (TargetDataLine) AudioSystem.getLine(info);
			line.open(format);
			System.out.println("Opened target data line " + line.toString() + ".");
		} catch (LineUnavailableException ex) {
			ex.printStackTrace();
			return;
		}
	}

	/**
	 * Call back to constructor
	 * @return Static singleton instance
	 */
	public static BeatDetector getInstance() {
		if (instance == null) {
			instance = new BeatDetector();
		}
		return instance;
	}
	
	/*
	 * Flow-control
	 */

	/**
	 * Value in ms during which the capture thread will sleep.
	 */
	private static final int SLEEP_TIME = 20;
	
	/**
	 * 
	 */
	public static void start() {
		if (line == null) {
			System.err.println("ERROR: Target data line is null. Cannot start audio monitoring.");
			return;
		}

		
		Thread audioThread = new Thread() {
			public void run() {	
				
				// Assume that the TargetDataLine, line, has already
				// been obtained and opened.
				//int numBytesRead;
				byte[] data = new byte[line.getBufferSize() / 5];

				// Begin audio capture.
				line.start();
				
				isRunning = true;
				
				while (isRunning) {
				   // Read the next chunk of data from the TargetDataLine.
				   //numBytesRead = line.read(data, 0, data.length);
				   line.read(data, 0, data.length);
				   // Save this chunk of data.
				   //out.write(data, 0, numBytesRead);
				   calculateRmsLevel(data);
				   
				   try {
					Thread.sleep(SLEEP_TIME);
				   } catch (InterruptedException e) {
					   e.printStackTrace();
				   }
				}
			}
		};
		// Call-back to run().
		audioThread.start();
	}
	
	/**
	 * 
	 */
	public void stop() {
		isRunning = false;
	}
	
	/**
	 * 
	 */
	private static boolean DEBUG_DISPLAY_RMS = true;
	
	/**
	 * 
	 * @param audioData
	 * @return
	 */
	private static int calculateRmsLevel(byte[] audioData) {
	    long lSum = 0;
	    for (int i=0; i<audioData.length; i++) lSum = lSum + audioData[i];

	    double average = lSum / audioData.length;

	    double sumMeanSquare = 0f;
	    for (int j=0; j<audioData.length; j++) {
	        sumMeanSquare = sumMeanSquare + Math.pow(audioData[j] - average, 2d);
	    }
	    

	    double averageMeanSquare = sumMeanSquare / audioData.length;
	    int rmsLevel = (int) (Math.pow(averageMeanSquare,0.5d) + 0.5);
	    
	    if (DEBUG_DISPLAY_RMS) {
	    	String levelDisplay = "Input level: ";
	    	if (rmsLevel < 10) levelDisplay += '0';
	    	levelDisplay += rmsLevel + " ";
			
	    	for (int i = 0; i < rmsLevel; i ++) {
				levelDisplay += '|';
			}
	    	System.out.println(levelDisplay);
		}
	    
	    return rmsLevel;
	}

}
