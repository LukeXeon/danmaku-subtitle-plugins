package org.kexie.android.danmakux.format;

import android.support.annotation.RestrictTo;

import org.kexie.android.danmakux.model.Section;
import org.kexie.android.danmakux.model.Subtitle;
import org.kexie.android.danmakux.model.Time;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


/**
 * This class represents the .SRT subtitle format
 * <br><br>
 * Copyright (c) 2012 J. David Requejo <br>
 * j[dot]david[dot]requejo[at] Gmail
 * <br><br>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 * <br><br>
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 * <br><br>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 * 
 * @author J. David Requejo
 *
 */

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class SRTFormat extends Format {


	public Subtitle parse(String fileName, InputStream input, Charset charset) throws IOException {

		Subtitle tto = new Subtitle();
		Section section = new Section();
		int captionNumber = 1;
		boolean allGood;

		//first lets loadFile the file
		InputStreamReader in = new InputStreamReader(input, charset);
		BufferedReader br = new BufferedReader(in);

		//the file name is saved
		tto.fileName = fileName;

		String line = br.readLine();
		line = line.replace("\uFEFF", ""); //remove BOM character
		int lineCounter = 0;
		try {
			while (line != null) {
				line = line.trim();
				lineCounter++;
				//if its a blank line, ignore it, otherwise...
				if (!line.isEmpty()) {
					allGood = false;
					//the first thing should be an increasing number
					try {
						int num = Integer.parseInt(line);
						if (num != captionNumber)
							throw new Exception();
						else {
							captionNumber++;
							allGood = true;
						}
					} catch (Exception e) {
						tto.warnings += captionNumber + " expected at line " + lineCounter;
						tto.warnings += "\n skipping to next line\n\n";
					}
					if (allGood) {
						//we go to next line, here the begin and end time should be found
						try {
							lineCounter++;
							line = br.readLine().trim();
							String start = line.substring(0, 12);
							String end = line.substring(line.length() - 12);
							Time time = new Time("hh:mm:ss,ms", start);
							section.start = time;
							time = new Time("hh:mm:ss,ms", end);
							section.end = time;
						} catch (Exception e) {
							tto.warnings += "incorrect time format at line " + lineCounter;
							allGood = false;
						}
					}
					if (allGood) {
						//we go to next line where the caption text starts
						lineCounter++;
						line = br.readLine().trim();
						StringBuilder text = new StringBuilder();
						while (!line.isEmpty()) {
							text.append(line).append("<br />");
							line = br.readLine().trim();
							lineCounter++;
						}
						section.content = text.toString();
						int key = section.start.milliseconds;
						//in case the key is already there, we increase it by a millisecond, since no duplicates are allowed
						while (tto.captions.containsKey(key)) key++;
						if (key != section.start.milliseconds)
							tto.warnings += "caption with same start time found...\n\n";
						//we add the caption.
						tto.captions.put(key, section);
					}
					//we go to next blank
					while (!line.isEmpty()) {
						line = br.readLine().trim();
						lineCounter++;
					}
					section = new Section();
				}
				line = br.readLine();
			}

		} catch (NullPointerException e) {
			tto.warnings += "unexpected end of file, maybe last caption is not complete.\n\n";
		} finally {
			//we close the reader
			input.close();
		}

		tto.built = true;
		return tto;
	}


	public String[] transform(Subtitle tto) {

		//first we check if the TimedText had been built, otherwise...
		if (!tto.built)
			return null;

		//we will write the lines in an ArrayList,
		int index = 0;
		//the minimum size of the file is 4*number of captions, so we'll take some extra space.
		ArrayList<String> file = new ArrayList<>(5 * tto.captions.size());
		//we iterate over our captions collection, they are ordered since they come from a TreeMap
		Collection<Section> c = tto.captions.values();
		Iterator<Section> itr = c.iterator();
		int captionNumber = 1;

		while (itr.hasNext()) {
			//new caption
			Section current = itr.next();
			//number is written
			file.add(index++, Integer.toString(captionNumber++));
			//we check for offset value:
			if (tto.offset != 0) {
				current.start.milliseconds += tto.offset;
				current.end.milliseconds += tto.offset;
			}
			//time is written
			file.add(index++, current.start.getTime("hh:mm:ss,ms") + " --> " + current.end.getTime("hh:mm:ss,ms"));
			//offset is undone
			if (tto.offset != 0) {
				current.start.milliseconds -= tto.offset;
				current.end.milliseconds -= tto.offset;
			}
			//text is added
			String[] lines = cleanTextForSRT(current);
			int i = 0;
			while (i < lines.length)
				file.add(index++, "" + lines[i++]);
			//we add the next blank line
			file.add(index++, "");
		}

		String[] toReturn = new String[file.size()];
		for (int i = 0; i < toReturn.length; i++) {
			toReturn[i] = file.get(i);
		}
		return toReturn;
	}


	/* PRIVATE METHODS */

	/**
	 * This method cleans caption.content of XML and parses line breaks.
	 */
	private String[] cleanTextForSRT(Section current) {
		String[] lines;
		String text = current.content;
		//add line breaks
		lines = text.split("\\<br[ ]*/\\>");
		//clean XML
		for (int i = 0; i < lines.length; i++) {
			//this will destroy all remaining XML tags
			lines[i] = lines[i].replaceAll("\\<.*?\\>", "");
		}
		return lines;
	}
}
