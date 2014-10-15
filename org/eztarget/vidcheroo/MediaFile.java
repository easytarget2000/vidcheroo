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

/**
 * All files opened by the parser and player are of this class.
 * 
 * @author michel@easy-target.org
 *
 */
public class MediaFile {
	/**
	 * Error code value for files that have not been parsed yet
	 */
	public static final long NOT_PARSED = -1l;
	
	/**
	 * Error code value for files with an indeterminable length
	 */
	public static final long LENGTH_INDETERMINABLE = -2l;
	
	/**
	 * Error code value for files that are not a valid video file
	 */
	public static final long NOT_VIDEO_FILE = -3l;
	
	/**
	 * Identification number
	 */
	public int id;
	
	/**
	 * Absolute path to this file
	 */
	public String path;
	
	/**
	 * Length of this media file in milliseconds.
	 */
	public long length;
}
