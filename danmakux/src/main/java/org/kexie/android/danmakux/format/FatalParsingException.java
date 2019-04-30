package org.kexie.android.danmakux.format;

/**
 * This class represents problems that may arise during the parsing of a subttile file.
 * 
 * @author J. David
 *
 */

public class FatalParsingException extends Exception {

	private static final long serialVersionUID = 6798827566637277804L;

	private String parsingError;

	FatalParsingException(String parsingError) {
		super(parsingError);
		this.parsingError = parsingError;
	}

	@Override
	public String getLocalizedMessage() {
		return parsingError;
	}
}
