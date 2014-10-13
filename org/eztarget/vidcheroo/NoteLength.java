package org.eztarget.vidcheroo;

/**
 * Stores available beat options and their multiplier counterparts.
 * Tempo is measured in quarter notes (1/4th), hence the length of one quarter note is 1 tempo unit.
 * The tempo is multiplied by 4 (sped up) to get the length of one 1/16th note, as 16/4 = 4.
 * The tempo is divided by 4 (slowed down to 25%) to get the length of one full note, as 1/4 = 0.25.
 * 
 * @author michel@easy-target.org
 *
 */
public class NoteLength {
	public static String[] readableNoteLengths = {"1/16", "1/8", "1/4", "1/2", "1/1", "4/1"};
	public static float[] tempoMultipliers = {4.0f, 2.0f, 1.0f, 0.5f, 0.25f, 0.0625f};
}
