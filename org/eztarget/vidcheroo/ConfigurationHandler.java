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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

/**
 * Singleton class that stores, restores and presents configuration variables.
 * 
 * @author michel@easy-target.org
 *
 */
public class ConfigurationHandler {
	
	/**
	 * Singleton instance
	 */
	private static ConfigurationHandler instance = null;

	// TODO: Find further variations of default paths.
	
	/**
	 * Default path of VLC libraries in Linux distros
	 */
	private static final String VLC_DEFAULT_PATH_LIN = "/usr/lib/vlc";
	
	/**
	 * Default path of VLC libraries in OSX.
	 */
	private static final String VLC_DEFAULT_PATH_OSX = "/Applications/VLC.app/Contents/MacOS/lib";
	
	/**
	 * Default path of VLC libraries in Windows.
	 * x64 VLC should be used and stored here on x64 Windows.
	 */
	private static final String VLC_DEFAULT_PATH_WIN = "C:\\Program Files\\VideoLAN\\VLC\\";
	
	/**
	 * Absolute path to the directory that contains the media feed.
	 */
	private static String mediaPath;
	
	/**
	 * Absolute path to the directory that contains the VLC libraries.
	 */
	private static String vlcPath;
	
	/**
	 * The current musical tempo value, i.e. beats or quarter notes per minute.
	 */
	private static float tempo = 120f;
	
	/*
	 * Singleton Constructor Methods
	 */
	
	/**
	 * Constructor
	 * Attempts to restore the configuration.
	 * If this fails, it looks into default directories.
	 */
	protected ConfigurationHandler() {
		restoreConfigProperties();
		
		// TODO: Find VLC without help.
		if (vlcPath == null) {
			switch (Engine.getOs()) {
			case LIN:
				setVlcPath(VLC_DEFAULT_PATH_LIN);
				break;
			case OSX:
				setVlcPath(VLC_DEFAULT_PATH_OSX);
				break;
			case WIN:
				setVlcPath(VLC_DEFAULT_PATH_WIN);
				// TODO: Try other directory on fail.
				break;
			default:
				break;
			}
		}
		
		// TODO: Read these values from settings or open file picker dialogue.
		if (mediaPath == null) {
			URL url = Launcher.class.getResource("feed/");
			if (url != null) {
				mediaPath = url.getPath();
			}
		}
		
		System.out.println("Using feed path: " + mediaPath);
	}

	/**
	 * Call back to constructor
	 * @return Static singleton instance
	 */
	public static ConfigurationHandler getInstance() {
		if (instance == null) {
			instance = new ConfigurationHandler();
		}
		return instance;
	}
	
	
	/*
	 * Properties File
	 */

	//TODO: Test if getProtectionDomain() causes access problems.
	
	/**
	 * The absolute path to the directory from which the Launcher has been started.
	 */
	private static String fClassPath = Launcher.class.getProtectionDomain().getCodeSource().getLocation().getPath();
	
	/**
	 * The absolute path to a possible file containing the configuration properties.
	 */
	private static final String CONFIG_PROPERTIES_FILE = fClassPath + "properties.vch";
	
	/**
	 * Key of the media path attribute in the configuration properties file.
	 */
	private static final String CONFIG_KEY_MEDIA_PATH = "media_path";
	
	/**
	 * Key of the VLC path attribute in the configuration properties file.
	 */
	private static final String CONFIG_KEY_VLC_PATH = "vlc_path";
	
	/**
	 * Key of the tempo attribute in the configuration properties file.
	 */
	private static final String CONFIG_KEY_TEMPO = "tempo";

	/**
	 * Looks for the properties file in the directory that the application was launched for.
	 * If the file was found, the properties are read and stored in the attributes.
	 */
	private static void restoreConfigProperties() {
		System.out.println("Searching for configuration at " + CONFIG_PROPERTIES_FILE + ".");
				
		InputStream input = null;
		 
		try {
			// Open an input stream from the predefined properties file.
			input = new FileInputStream(CONFIG_PROPERTIES_FILE);

			// Load the properties from the input file.
			Properties prop = new Properties();
			prop.load(input);
	 
			// Get the predefined properties values and apply them to the config attributes.
			// VLC libs path:
			String vlcPathProperty = prop.getProperty(CONFIG_KEY_VLC_PATH);
			setVlcPath(vlcPathProperty);
			// Media/feed path:
			String mediaPathProperty = prop.getProperty(CONFIG_KEY_MEDIA_PATH);
			setMediaPath(mediaPathProperty);
			// Last set tempo:
			String tempoProperty = prop.getProperty(CONFIG_KEY_TEMPO);
			setTempo(tempoProperty);
		} catch (FileNotFoundException fileEx) {
			System.err.println("WARNING: Could not find configuration properties file.");
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
					System.out.println("Closed " + CONFIG_PROPERTIES_FILE  + ".");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Stores the current configuration attributes in a properties file
	 * in the directory that this application was launched in.
	 */
	public static void storeConfigProperties() {
		if (mediaPath == null || vlcPath == null) {
			System.err.println("ERROR: Missing attributes to store config properties.");
			return;
		}
		
		System.out.println("Storing configuration in " + CONFIG_PROPERTIES_FILE + ".");
				
		OutputStream output = null;
		try {
			output = new FileOutputStream(CONFIG_PROPERTIES_FILE);
	 
			// Set the properties value.
			Properties prop = new Properties();
			prop.setProperty(CONFIG_KEY_MEDIA_PATH, mediaPath);
			prop.setProperty(CONFIG_KEY_VLC_PATH, vlcPath);
			prop.setProperty(CONFIG_KEY_TEMPO, tempo + "");
	 
			// Save properties to project root folder.
			prop.store(output, null);
			System.out.println(prop.toString());
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
	
	/*
	 * Public Getter/Setter Methods
	 */
	
	/**
	 * @return Value of mediaPath attribute
	 */
	public static String getMediaPath() {
		return mediaPath;
	}
	
	/**
	 * Changes the media path attribute and attempts to parse the files in the new directory.
	 * @param mediaPath New media path value
	 */
	public static void setMediaPath(String mediaPath) {
		ConfigurationHandler.mediaPath = mediaPath;
		if (vlcPath != null) MediaFileParser.parseMediaPath(mediaPath);
	}
	
	/**
	 * @return Value of musical tempo attribute
	 */
	public static float getTempo() {
		return tempo;
	}
	
	/**
	 * Lowest musical tempo that is allowed.
	 */
	private static final float MIN_TEMPO = 60.0f;
	
	/**
	 * Highest musical tempo that is allowed.
	 */
	private static final float MAX_TEMPO = 180.0f;

	/**
	 * First in chain of tempo changes.
	 * Takes a string as this value usually comes from a text field.
	 * Attempts to parse the string into a numeric value in a certain range.
	 * If this was successful, the engine will be notified to update the note lengths.
	 * If this was not successful, an error notification will be displayed and the text will be reset.
	 * @param tempoText String that can be parsed into a numeric value between MIN_TEMPO & MAX_TEMPO
	 */
	public static void setTempo(String tempoText) {
		System.out.println("Attempting to set tempo " + tempoText + ".");
		float newTempo = 0.0f;
		
		String tempoTextDotted = "";
		for (int i = 0; i < tempoText.length(); i++) {
			char c = tempoText.charAt(i);
			
			if (c < '0' || c  > '9') tempoTextDotted += '.';
			else tempoTextDotted += c;
		}
		
		// Attempt to read a float value from the given string.
		try {
			newTempo = Float.parseFloat(tempoTextDotted);
		} catch(Exception ex) {
			System.err.println(ex.toString());
		}
		
		// Only replace the tempo if a valid BPM value was given.
		if(newTempo >= MIN_TEMPO && newTempo <= MAX_TEMPO) {
			tempo = newTempo;
		} else {
			Engine.blinkStatusText(MIN_TEMPO + " < Tempo < " + MAX_TEMPO + "!");
		}
		
		Engine.updateTempo();
	}
	
	/**
	 * @return Value of vlcPath attribute
	 */
	public static String getVlcPath() {
		return vlcPath;
	}
	
	/**
	 * Looks for the VLC libraries at the given directory.
	 * If they are found there, the attribute will be changed.
	 * The Engine status will be set accordingly.
	 * @param vlcPath Absolute path to a directory containing the VLC libraries
	 */
	public static void setVlcPath(String vlcPath) {
		
		System.out.println("Searching for VLC libraries at " + vlcPath + ".");
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), vlcPath);
		try {
	        Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
			ConfigurationHandler.vlcPath = vlcPath;
			Engine.setDidFindVlc(true);
			System.out.println("Found VLC libraries.");
		} catch (UnsatisfiedLinkError unsatisfied) {
			System.err.println("ERROR: Could not find VLC libraries at " + vlcPath + ".");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		if (vlcPath == null) Engine.setDidFindVlc(false);
	}
	
}
