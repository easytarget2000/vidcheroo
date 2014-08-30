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

import java.io.File;
import java.util.ArrayList;

public class MediaFileParser {
	
	private static final boolean PARSE_FILES = true;
	private static final int PARSE_FRAME_WIDTH = 300;
	private static final int PARSE_FRAME_HEIGHT = 200;

	private static MediaFileParser instance = null;
			
	private static ArrayList<VidcherooMediaFile> mediaFiles = new ArrayList<VidcherooMediaFile>();
	//TODO: Read this value from stored settings.
	private static String mediaPath = "/Users/michel/Projekte/VidcherooOld/feed";
		
	protected MediaFileParser() {
		
	}
	
	public static MediaFileParser getInstance() {
		if(instance == null) {
			instance = new MediaFileParser();
		}
		return instance;
	}
	
	public static void parseFileList() {
		Engine.setStatus(VidcherooStatus.PARSING);
		
		Thread parseThread = new Thread() {
			public void run() {
				mediaFiles = new ArrayList<VidcherooMediaFile>();
				VidcherooMediaFrame parseFrame = new VidcherooMediaFrame(PARSE_FRAME_WIDTH, PARSE_FRAME_HEIGHT);
			
				//EmbeddedMediaPlayerComponent mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
				
				System.out.println("Looking for media files in " + mediaPath);
				
				if(mediaPath.length() > 1) {
					File fileDirectory = new File(mediaPath);
					
					if (fileDirectory.length() > 0) {
						for (final File fileEntry : fileDirectory.listFiles()) {
							if (!fileEntry.isDirectory()) {
								String mediaFilePath = mediaPath + "/" + fileEntry.getName();
								//TODO: Only load possible media files.
								VidcherooMediaFile file = new VidcherooMediaFile();
								file.path = mediaFilePath;
								
								if (PARSE_FILES) {
									file.length = parseFrame.getMediaLength(mediaFilePath);
								} else {
									file.length = VidcherooMediaFile.NOT_PARSED;
								}
								
								file.id = mediaFiles.size();
								mediaFiles.add(file);
							}
						}				
					}
					
					parseFrame.setVisible(false);
					parseFrame = null;
					System.out.println("Number of found files: " + mediaFiles.size());
				}
				
				// Enable the Engine by setting the resulting status.
				if (mediaFiles.size() > 1) Engine.setStatus(VidcherooStatus.READY);
				else Engine.setStatus(VidcherooStatus.NOFILES);
			}
		};
		
		// Call-back to run():
		parseThread.start();
	}
	
	public static VidcherooMediaFile getRandomMediaFile() {
		if(mediaFiles.size() > 0) {
			int randomMediaIndex = (int) (Math.random() * mediaFiles.size());
			return mediaFiles.get(randomMediaIndex);
		} else {
			//TODO: Return placeholder video file.
			return null;
		}
	}
	 
	public static int getFileListLength() {
		return mediaFiles.size();
	}
}
