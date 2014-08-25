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

import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.JFrame;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

public class VidcherooMediaFrame extends JFrame {
	
	private static final long serialVersionUID = 201408251912L;
	
	private int frameX		= 300;
	private int frameY		= 40;
	private int frameWidth	= (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() * 0.7f);
	private int frameHeight = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() * 0.7f);
		
	private EmbeddedMediaPlayerComponent mediaPlayerComponent;
	
	public VidcherooMediaFrame() {
		System.out.println("Initialising Media Frame.");
		
		setBounds(frameX, frameY, frameWidth, frameHeight);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Vidcheroo");
		setResizable(true);
		setUndecorated(true);
		
		loadVlcLibraries("/Applications/VLC.app/Contents/MacOS/lib");
		mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
    	mediaPlayerComponent.getMediaPlayer().setRepeat(true);
        setContentPane(mediaPlayerComponent);
        
        setVisible(true);
	}
	
	public void playMediaFile(String mediaPath) {
		//TODO: Figure out if using one instance for all methods or getMediaPlayer() for each is better.
    	mediaPlayerComponent.getMediaPlayer().setVolume(0);
    	//mediaPlayerComponent.getMediaPlayer().setFullScreen(true);
    	mediaPlayerComponent.getMediaPlayer().playMedia(mediaPath);
	}
	
	private void loadVlcLibraries(String searchPath) {
		System.out.println("Searching for VLC libraries at " + searchPath);
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), searchPath);
        Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
	}

	public void pause() {
		if (mediaPlayerComponent.getMediaPlayer().isPlaying()) {
			System.out.println("Media frame pauses playback.");
			mediaPlayerComponent.getMediaPlayer().pause();
		}
	}

	public void stop() {
		mediaPlayerComponent.getMediaPlayer().stop();
	}

	/**
	 * 
	 * @param windowed
	 */
	public void setWindowed(boolean windowed) {
		if (!windowed) {
			// Store settings before resizing.
			Rectangle windowBounds = getBounds();
			frameX		= windowBounds.x;
			frameY		= windowBounds.y;
			frameWidth	= (int) windowBounds.getWidth();
			frameHeight = (int) windowBounds.getHeight();

		    if (getGraphicsConfiguration().getDevice().isFullScreenSupported()) {
		    	// Resize the window, then mark it as full-screen.
		    	Rectangle screenBounds = getGraphicsConfiguration().getBounds();
		    	screenBounds.y -= 20;
		    	screenBounds.height += 20;
		    	setBounds(screenBounds);
		    	//getGraphicsConfiguration().getDevice().setFullScreenWindow(this);
		  
		        setResizable(false);
		    } else {
		    	setWindowed(true);
		    }
		} else {
			setResizable(true);
			setBounds(frameX, frameY, frameWidth, frameHeight);
		}
	}
}
