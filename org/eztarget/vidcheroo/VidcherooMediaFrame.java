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

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.lang.reflect.Method;

import javax.swing.JFrame;

import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;

public class VidcherooMediaFrame extends JFrame {
	private static final long serialVersionUID = 201408251912L;
		    
	private int frameX		= 300;
	private int frameY		= 40;
	private int frameWidth, frameHeight;
		
	private EmbeddedMediaPlayerComponent mediaPlayerComponent;
	//private EmbeddedMediaPlayer mediaPlayer;
	
	public VidcherooMediaFrame() {
		this(
			(int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() * 0.7f),
			(int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() * 0.7f),
			"Vidcheroo"
			);
	}
	
	public VidcherooMediaFrame(int width, int height, String title) {
		this.frameWidth = width;
		this.frameHeight = height;
		setTitle(title);

		System.out.println("Initialising Media Frame.");
		
		setBounds(frameX, frameY, frameWidth, frameHeight);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(true);
		//setUndecorated(true);
		
		if (VidcherooConfig.getVlcPath() == null) {
			System.err.println("ERROR: VLC path is not set.");
			return;
		}
		
		mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
		mediaPlayerComponent.getMediaPlayer().setVolume(0);
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
	
	public void playMediaFilePath(String mediaPath, long startTime, boolean repeat) {
		//TODO: Mute permanently.
		String vlcOptions = ":quiet :no-video-title-show :start-time=" + startTime;
		mediaPlayerComponent.getMediaPlayer().playMedia(mediaPath, vlcOptions);
    	mediaPlayerComponent.getMediaPlayer().setVolume(0);
    	if (repeat) mediaPlayerComponent.getMediaPlayer().setRepeat(true);
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
		setVisible(false);
		dispose();
		
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

		    if (getGraphicsConfiguration().getDevice().isFullScreenSupported()) {
		    	// Resize the window, then mark it as full-screen.

		    	getGraphicsConfiguration().getDevice().setFullScreenWindow(this);
		    	setUndecorated(true);
		    	
		    } else {
		    	setWindowed(true);
		    }
		} else {
			System.out.println("Going into windowed mode.");
			
	    	getGraphicsConfiguration().getDevice().setFullScreenWindow(null);
			setResizable(true);
			setBounds(frameX, frameY, frameWidth, frameHeight);
			setUndecorated(false);
		}
		pack();
		setVisible(true);
	}

	public void setMediaTime(long time) {
		mediaPlayerComponent.getMediaPlayer().setTime(time);
	}

	public long getMediaLength(String mediaFilePath) {
		MediaPlayer player = mediaPlayerComponent.getMediaPlayer();
		player.playMedia(mediaFilePath);
		player.parseMedia();
		
		long length = player.getLength();
		System.out.println(mediaFilePath + " t: " + player.getVideoTrack());

		player.stop();
		
		
		System.out.println(mediaFilePath + " length: " + length);
		
		if (length <= 0) return VidcherooMediaFile.LENGTH_INDETERMINABLE;
		else return length;
	}
}
