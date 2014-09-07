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

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Launcher {
	
	public static final String ICON_PATH = "org/eztarget/vidcheroo/resources/icon.png";
	
	public static void main(String[] args) {
		String osNameProperty = System.getProperty("os.name");
		System.out.println("OS Name: " + osNameProperty);
		if (osNameProperty.contains("Mac")) {
			Engine.setOs(SupportedOperatingSystems.OSX);
		    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Vidcheroo");
		    System.setProperty("apple.laf.useScreenMenuBar", "true");
		    System.setProperty("apple.laf.useScreenMenuBar", "true");
		} else if (osNameProperty.contains("Windows")) {
			Engine.setOs(SupportedOperatingSystems.WIN);
		} else {
			System.err.println("WARNING: OS unknown: " + osNameProperty);
		}
		
		// Set System L&F.
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException e) {
			System.err.println("ERROR: Unspported L&F:" + UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		// Initialise the configuration.
		VidcherooConfig.getInstance();
		
		// The GUI is running in its own thread.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				
				System.out.println("Starting Vidcheroo GUI.");
				VidcherooControlFrame controlFrame = new VidcherooControlFrame();

				Engine.setControlFrame(controlFrame);
				
				// Initialise the Engine.
				Engine.getInstance();
	         }
		});
	}
}
