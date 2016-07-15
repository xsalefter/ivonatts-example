package com.xsalefter.vionatts;

import com.ivona.services.tts.model.CreateSpeechResult;

/**
 * Functional interface to handle downloaded speech by 
 * {@link IvonaFacadeDAO#createDownloadedSpeech(String, String, DownloadedSpeechHandler)}.
 * @author xsalefter
 */
@FunctionalInterface
public interface DownloadedSpeechHandler {

	/**
	 * Default functional interface implementation. The return value of this method 
	 * just simply the parameter it self.
	 * 
	 * @param result {@link CreateSpeechResult} instance comes from Ivona API.
	 */
	void handleDownloadedSpeech(final CreateSpeechResult result);
}
