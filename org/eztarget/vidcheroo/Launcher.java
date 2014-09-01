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

public class Launcher {
	
	public static void main(String[] args) {
		String osNameProperty = System.getProperty("os.name");
		System.out.println("OS Name: " + osNameProperty);
		if (osNameProperty.equals("Mac OS X")) {
			Engine.setOs(SupportedOperatingSystems.OSX);
		    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Vidcheroo");
		} else {
			System.err.println("WARNING: OS unknown: " + osNameProperty);
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
