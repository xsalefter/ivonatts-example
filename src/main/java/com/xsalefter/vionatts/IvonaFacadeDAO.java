package com.xsalefter.vionatts;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.ivona.services.tts.IvonaSpeechCloudClient;
import com.ivona.services.tts.model.CreateSpeechRequest;
import com.ivona.services.tts.model.CreateSpeechResult;
import com.ivona.services.tts.model.GetLexiconRequest;
import com.ivona.services.tts.model.GetLexiconResult;
import com.ivona.services.tts.model.Input;
import com.ivona.services.tts.model.Lexicon;
import com.ivona.services.tts.model.ListLexiconsResult;
import com.ivona.services.tts.model.ListVoicesRequest;
import com.ivona.services.tts.model.ListVoicesResult;
import com.ivona.services.tts.model.Voice;

/**
 * Data Access Object pattern for Ivona API.
 * 
 * @author xsalefter
 */
public class IvonaFacadeDAO {

	private static final Logger logger = LoggerFactory.getLogger(IvonaFacadeDAO.class);
	private static final String DEFAULT_AWS_ENDPOINT = "https://tts.eu-west-1.ivonacloud.com";

	private final IvonaSpeechCloudClient ivonaSpeechCloudClient;


	/**
	 * Create new instance of {@link IvonaFacadeDAO}.
	 * @param configFileLocation the configuration file location.
	 */
	public IvonaFacadeDAO(final String configFileLocation) {
		logger.info("Create new instance of IvonaDAO with configFileLocation: {}", configFileLocation);

		final AWSCredentialsProvider provider = new ClasspathPropertiesFileCredentialsProvider(configFileLocation);
		this.ivonaSpeechCloudClient = new IvonaSpeechCloudClient(provider);
		this.ivonaSpeechCloudClient.setEndpoint(DEFAULT_AWS_ENDPOINT);
	}


	/**
	 * Create new instance of {@link IvonaFacadeDAO}. Use this constructor if 
	 * {@link IvonaSpeechCloudClient} creation need to be externalized.
	 * @param ivonaSpeechCloudClient valid, non-null, {@link IvonaSpeechCloudClient} instance.
	 */
	public IvonaFacadeDAO(final IvonaSpeechCloudClient ivonaSpeechCloudClient) {
		this.ivonaSpeechCloudClient = ivonaSpeechCloudClient;
	}


	/**
	 * List all available voices in Ivona API.
	 * 
	 * @param listVoicesRequests request parameter. Could be empty. If contains more than 1 elements,
	 *   only the first elements that will be considered as valid elements as parameter.
	 * @return Valid, non-null List of Voice.
	 * @throws RuntimeException if {@link IvonaSpeechCloudClient} is null.
	 */
	public List<Voice> listVoices(final ListVoicesRequest... listVoicesRequests) {
		if (this.ivonaSpeechCloudClient != null) {
			final ListVoicesRequest param = handleListVoicesParams(listVoicesRequests);
			final ListVoicesResult result = this.ivonaSpeechCloudClient.listVoices(param);
			return result.getVoices();
		}
		throw new RuntimeException("ivonaSpeechCloudClient is null.");
	}


	/**
	 * List all available lexicon.
	 * @return Valid, non-null List of lexicon name.
	 */
	public List<String> listLexiconsName() {
		if (this.ivonaSpeechCloudClient != null) {
			final ListLexiconsResult result = this.ivonaSpeechCloudClient.listLexicons();
			return result.getLexiconNames();
		}
		throw new RuntimeException("ivonaSpeechCloudClient is null.");
	}


	/**
	 * Get {@link Lexicon} data, based on {@link GetLexiconRequest} in parameter.
	 * @param getLexiconRequests request parameter. Could be empty. If contains more than 1 elements,
	 *   only the first elements that will be considered as valid elements as parameter.
	 * @return Valid, non-null {@link Lexicon} instance.
	 */
	public Lexicon getLexicon(final GetLexiconRequest...getLexiconRequests) {
		if (this.ivonaSpeechCloudClient != null) {
			final GetLexiconRequest param = handleGetLexiconParams(getLexiconRequests);
			final GetLexiconResult result = this.ivonaSpeechCloudClient.getLexicon(param);
			return result.getLexicon();
		}
		throw new RuntimeException("ivonaSpeechCloudClient is null.");
	}


	/**
	 * Create downloadable speech in the cloud. The {@link URL} object returned 
	 * for get actual speech voice data.
	 * @param name the voice name. See {@link #listVoices(ListVoicesRequest...)}.
	 * @param data text/value to speech.
	 * @return {@link URL} object to get actual speech data.
	 * @see #createDownloadedSpeech(String, String, DownloadedSpeechHandler)
	 */
	public URL createInCloudSpeech(final String name, final String data) {
		if (this.ivonaSpeechCloudClient != null) {
			final CreateSpeechRequest request = newSpeechRequest(name, data);
			try {
				return this.ivonaSpeechCloudClient.getCreateSpeechUrl(request);
			} catch (UnsupportedEncodingException e) {
				throw new IvonaFacadeDAOException("Cannot create speech URL. Encoding is not supported.", e);
			}
		}
		throw new RuntimeException("ivonaSpeechCloudClient is null.");
	}


	/**
	 * Create downloadable speech in Ivona and process the file locally. Use functional 
	 * interface {@link DownloadedSpeechHandler} to process the data. 
	 * @param name the voice name. See all data in {@link #listVoices(ListVoicesRequest...)}.
	 * @param data text/value to speech.
	 * @param downloadedSpeechHandler functional interface to process {@link CreateSpeechResult}.
	 * @return Valid, non-null, {@link CreateSpeechResult} object.
	 */
	public CreateSpeechResult createDownloadedSpeech(
			final String name, 
			final String data, 
			final DownloadedSpeechHandler downloadedSpeechHandler) {
		if (this.ivonaSpeechCloudClient != null) {
			final CreateSpeechRequest request = newSpeechRequest(name, data);
			final CreateSpeechResult result = this.ivonaSpeechCloudClient.createSpeech(request);
			downloadedSpeechHandler.handleDownloadedSpeech(result);
			return result;
		}
		throw new RuntimeException("ivonaSpeechCloudClient is null.");
	}


	/** @VisibleForTesting */
	public final IvonaSpeechCloudClient getIvonaSpeechCloudClient() {
		return this.ivonaSpeechCloudClient;
	}


	/**
	 * Create new instance of {@link CreateSpeechRequest}.
	 * @param speakerName the speaker name. See {@link #listVoices(ListVoicesRequest...)}.
	 * @param data in String to speech.
	 * @return Valid, non-null {@link CreateSpeechRequest} instance.
	 */
	public static CreateSpeechRequest newSpeechRequest(final String speakerName, final String data) {
		// TODO: Validation?
		final Voice voice = new Voice().withName(speakerName);
		final Input input = new Input().withData(data);

		final CreateSpeechRequest request = new CreateSpeechRequest();
		request.setVoice(voice);
		request.setInput(input);

		return request;
	}


	protected static ListVoicesRequest handleListVoicesParams(final ListVoicesRequest... listVoicesRequests) {
		if (listVoicesRequests == null || listVoicesRequests.length == 0) {
			logger.info("#handleListVoicesParams() method receipt no args. Will return new ListVoicesRequest object.");
			return new ListVoicesRequest();
		}
		if (listVoicesRequests.length > 1) {
			logger.warn("handleListVoicesParams() receipt more than 1 args. Will return only the 1st object and ignoring the rest.");
			return listVoicesRequests[0];
		}
		return listVoicesRequests[0];
	}


	protected static GetLexiconRequest handleGetLexiconParams(final GetLexiconRequest... getLexiconRequests) {
		if (getLexiconRequests == null || getLexiconRequests.length == 0) {
			logger.info("#handleGetLexiconParams() method receipt no args. Will return new ListVoicesRequest object.");
			return new GetLexiconRequest();
		}
		if (getLexiconRequests.length > 1) {
			logger.warn("handleGetLexiconParams() receipt more than 1 args. Will return only the 1st object and ignoring the rest.");
			return getLexiconRequests[0];
		}
		return getLexiconRequests[0];
	}
}
