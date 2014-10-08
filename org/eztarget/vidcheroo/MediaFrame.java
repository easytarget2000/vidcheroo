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

import java.awt.IllegalComponentStateException;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.lang.reflect.Method;

import javax.swing.JFrame;

import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;

/**
 * Used as the VLC "display" for main video function, as well as, parsing media files.
 * 
 * @author michel@easy-target.org
 *
 */
public class MediaFrame extends JFrame {
	
	/**
	 * Mandatory but not used.
	 */
	private static final long serialVersionUID = 2014091023323915L;
	
	/**
	 * X coordinate of the upper left corner of this frame.
	 */
	private int frameX = 300;
	
	/**
	 * Y coordinate of the upper left corner of this frame.
	 */
	private int frameY = 40;
	
	/**
	 * Width and height of this frame.
	 */
	private int frameWidth, frameHeight;
	
	/**
	 * Contains the VLC media player.
	 */
	private EmbeddedMediaPlayerComponent mediaPlayerComponent;
	
	/**
	 * Default constructor
	 * Calls specific constructor method with width and height at 70% of the screen resolution.
	 */
	public MediaFrame() {
		this(
			(int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() * 0.7f),
			(int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() * 0.7f),
			"Vidcheroo"
			);
	}
	
	/**
	 * Specific constructor
	 * Sets the given bounds and various other GUI settings.
	 * @param width	Frame width
	 * @param height Frame height
	 * @param title Frame title
	 */
	public MediaFrame(int width, int height, String title) {
		this.frameWidth = width;
		this.frameHeight = height;
		setTitle(title);

		System.out.println("Initialising Media Frame.");
		
		setBounds(frameX, frameY, frameWidth, frameHeight);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(true);
		//setUndecorated(true);
		
		if (ConfigurationHandler.getVlcPath() == null) {
			System.err.println("ERROR: VLC path is not set.");
			return;
		}
		
		mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        setContentPane(mediaPlayerComponent);
        
        if (Engine.getOs() == SupportedOperatingSystems.OSX) {
            enableOSXFullscreen();
		}
        
		/*
		 * Application Icon
		 */
        
		java.net.URL url = ClassLoader.getSystemResource(Launcher.ICON_PATH);
		Toolkit kit = Toolkit.getDefaultToolkit();
		Image image = kit.createImage(url);
		setIconImage(image);
        
        setVisible(true);
	}
	
	/**
	 * Enables the native MacOS X full-screen button.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	private void enableOSXFullscreen() {
		try {
			Class util = Class.forName("com.apple.eawt.FullScreenUtilities");
			Class params[] = new Class[]{Window.class, Boolean.TYPE};
			Method method = util.getMethod("setWindowCanFullScreen", params);
			method.invoke(util, this, true);
		}  catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * FLOW CONTROL
	 */
	
	//private static final String[] VLC_OPTIONS = {":quiet", ":no-audio", ":no-video-title-show", ":repeat"};
	
	/**
	 * Sets VLC options and plays the given file starting at a given time.
	 * @param mediaPath Absolute path to a media file
	 * @param startTime Value in seconds at which to skip to when the playback begins. Seems to create a slight visible delay if not 0.
	 */
	public void playMediaFilePath(String mediaPath, long startTime) {
		String[] vlcOptions = {":quiet", ":no-audio", ":no-video-title-show", ":start-time=" + startTime / 1000};
		mediaPlayerComponent.getMediaPlayer().playMedia(mediaPath, vlcOptions);
		//mediaPlayerComponent.getMediaPlayer().skipPosition(startTime);
	}

	/**
	 * If the media player is playing, it is paused.
	 */
	public void pause() {
		if (mediaPlayerComponent.getMediaPlayer().isPlaying()) {
			System.out.println("Media frame pauses playback.");
			mediaPlayerComponent.getMediaPlayer().pause();
		}
	}

	/**
	 * Always dead stops the media player.
	 */
	public void stop() {
		mediaPlayerComponent.getMediaPlayer().stop();
	}
	
	public void setMediaTime(long time) {
		mediaPlayerComponent.getMediaPlayer().setTime(time);
		System.out.println("Skipped to " + time);
	}

	/**
	 * Switches between windowed and full-screen mode.
	 * Only to be used on OS other than OSX.
	 * @param windowed Switch to full-screen mode if false.
	 */
	public void setWindowed(boolean windowed) {
		//setVisible(false);
		dispose();
		
		setResizable(windowed);
		try {
			setUndecorated(!windowed);
		} catch (IllegalComponentStateException e) {
			System.out.println("Changing decorations exception occured.");
		}
		
		if (!windowed) {
			System.out.println("Leaving windowed mode.");
			
			// Store settings before resizing.
			Rectangle windowBounds = getBounds();
			frameX		= windowBounds.x;
			frameY		= windowBounds.y;
			frameWidth	= (int) windowBounds.getWidth();
			frameHeight = (int) windowBounds.getHeight();
	    	
	    	Rectangle screenBounds = getGraphicsConfiguration().getBounds();
	    	setBounds(screenBounds);

//		    if (getGraphicsConfiguration().getDevice().isFullScreenSupported()) {
//		    	// Resize the window, then mark it as full-screen.
//
//		    	getGraphicsConfiguration().getDevice().setFullScreenWindow(this);
//		    	//setUndecorated(true);
//		    	
//		    } else {
//		    	setWindowed(true);
//		    }
		} else {
			System.out.println("Going into windowed mode.");
			
	    	getGraphicsConfiguration().getDevice().setFullScreenWindow(null);
			setBounds(frameX, frameY, frameWidth, frameHeight);
			//setUndecorated(false);
		}
		//pack();
		setVisible(true);
		//repaint();
	}

	/**
	 * Used for parsing files.
	 * @param mediaFilePath Absolute path to the file that is to be analysed.
	 * @return The length in ms (or error code value) of the media file.
	 */
	public long getMediaLength(String mediaFilePath) {
		MediaPlayer player = mediaPlayerComponent.getMediaPlayer();
		player.playMedia(mediaFilePath);
		player.parseMedia();
		
		long length = player.getMediaMeta().getLength();
		if (length <= 0) {
			System.err.println("WARNING: " + mediaFilePath + " is not a valid video file.");
			length = MediaFile.NOT_VIDEO_FILE;
		} else {
			System.out.println(mediaFilePath + " " + length);
			//System.out.println(player.getAspectRatio() + " " + player.getAudioDelay());
		}
		
		player.stop();
		return length;
	}
}
