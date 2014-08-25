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

import java.awt.Toolkit;

import javax.swing.JFrame;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

public class VidcherooMediaFrame extends JFrame {
	
	private static final long serialVersionUID = 201408251015L;
	
	private static int FRAME_INITIAL_X		= 300;
	private static int FRAME_INITIAL_Y		= 40;
	private static int FRAME_INITIAL_WIDTH	= (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() * 0.7f);
	private static int FRAME_INITIAL_HEIGHT = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() * 0.7f);
	
	private EmbeddedMediaPlayerComponent mediaPlayerComponent;
	
	public VidcherooMediaFrame() {
		System.out.println("Initialising Media Frame.");
		
		setBounds(FRAME_INITIAL_X, FRAME_INITIAL_Y, FRAME_INITIAL_WIDTH, FRAME_INITIAL_HEIGHT);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Vidcheroo");
		
		loadVlcLibraries("/Applications/VLC.app/Contents/MacOS/lib");
		mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        setContentPane(mediaPlayerComponent);
	}
	
	public void playMediaFile(String mediaPath) {
		//TODO: Figure out if using one instance for all methods or getMediaPlayer() for each is better.
    	mediaPlayerComponent.getMediaPlayer().playMedia(mediaPath);
    	mediaPlayerComponent.getMediaPlayer().setVolume(0);
	}
	
	private void loadVlcLibraries(String searchPath) {
		System.out.println("Searching for VLC libraries at " + searchPath);
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), searchPath);
        Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
	}

	public void pause() {
		// TODO Auto-generated method stub
		
	}
}
