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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

public class VidcherooConfig {
	
	private static VidcherooConfig instance = null;

	// TODO: Find further variations of this.
	private static final String VLC_DEFAULT_PATH_LIN = "/usr/lib/vlc";
	private static final String VLC_DEFAULT_PATH_OSX = "/Applications/VLC.app/Contents/MacOS/lib";
	private static final String VLC_DEFAULT_PATH_W32 = "C:\\Program Files(x86)\\VLC\\lib\\";
	private static final String VLC_DEFAULT_PATH_W64 = "C:\\Program Files(x86)\\VLC\\lib\\";
	
	private static String mediaPath;
	private static String vlcPath;
	private static float tempo = 120f;
	
	/*
	 * Singleton Constructor Methods
	 */
	
	protected VidcherooConfig() {
		// TODO: Read these values from settings or open file picker dialogue.
		mediaPath = "/Users/michel/Projekte/VidcherooOld/feed";
		
		// TODO: Find VLC without help.
		if (vlcPath == null) {
			switch (Engine.getOs()) {
			case LIN:
				vlcPath = VLC_DEFAULT_PATH_LIN;
				break;
			case OSX:
				vlcPath = VLC_DEFAULT_PATH_OSX;
				break;
			case W32:
				vlcPath = VLC_DEFAULT_PATH_W32;
				break;
			case W64:
				vlcPath = VLC_DEFAULT_PATH_W64;
				break;
			default:
				break;
			}
		}
		
		// TODO: Check if libs were found at VLC path.
		// TODO: Open picker dialog.
	}

	public static VidcherooConfig getInstance() {
		if (instance == null) {
			instance = new VidcherooConfig();
		}
		return instance;
	}
	
	/*
	 * Public Getter/Setter Methods
	 */
	
	public String getMediaPath() {
		return mediaPath;
	}
	
	public static float getTempo() {
		return tempo;
	}
	
	public static String getVlcPath() {
		return vlcPath;
	}
	
	private static void setMediaPath(String mediaPath) {
		VidcherooConfig.mediaPath = mediaPath;
		MediaFileParser.parseMediaPath(mediaPath);
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
			Engine.blinkStatusText(MIN_TEMPO + " < Tempo < " + MAX_TEMPO + "!");
		}
		
		Engine.updateTempo();
	}
	
	/*
	 * Properties File
	 */

	private static final String CONFIG_PROPERTIES_FILE	= "config.properties";
	private static final String CONFIG_KEY_MEDIA_PATH	= "media_path";
	private static final String CONFIG_KEY_VLC_PATH		= "vlc_path";
	private static final String CONFIG_KEY_TEMPO		= "tempo";

	public static void readConfigProperties() {
		System.out.println("Reading configuration.");
		
		vlcPath = VidcherooConfig.VLC_DEFAULT_PATH_OSX;
		
		// TODO: Read these values from settings or open file picker dialogue.
		setMediaPath("/Users/michel/Projekte/VidcherooOld/feed");
		
		// TODO: Determine OS.
		// TODO: Find VLC without help.
	}
	
	private static void storeConfigProperties() {
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
