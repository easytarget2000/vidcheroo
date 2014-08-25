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

public class FileCrawler {

	private static FileCrawler instance = null;
		
	private ArrayList<String> filePathList = new ArrayList<String>();
	//TODO: Read this value from stored settings.
	private String mediaPath = "/Users/michel/Projekte/VidcherooOld/feed";
		
	protected FileCrawler() {
		
	}
	
	public static FileCrawler getInstance() {
		if(instance == null) {
			instance = new FileCrawler();
		}
		return instance;
	}
	
	public void loadFileList() {
		// Empty the ArrayList.
		for(int i=0; i < filePathList.size(); i++) filePathList.remove(i);
		
		System.out.println("Looking for media files in " + mediaPath);
		
		if(mediaPath.length() > 1) {
			File fileDirectory = new File(mediaPath);
			
			if (fileDirectory.length() > 0) {
				for (final File fileEntry : fileDirectory.listFiles()) {
					if (!fileEntry.isDirectory()) {
						//TODO: Only load possible media files.
						filePathList.add(mediaPath + "/" + fileEntry.getName());
			            //System.out.println(fileEntry.getName());
					}
				}
			}
			
			System.out.println("Number of found files: " + filePathList.size());
		}
	}
	
	public String getRandomMediaPath() {		
		if(filePathList.size() > 0) {
			int nextMediaIndex = (int) (Math.random() * filePathList.size());
			return filePathList.get(nextMediaIndex);
		} else {
			//TODO: Return placeholder video file.
			return "";
		}
	}
	
	public int getFileListLength() {
		return filePathList.size();
	}
}

