package com.xsalefter.vionatts;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ivona.services.tts.IvonaSpeechCloudClient;
import com.ivona.services.tts.model.CreateSpeechResult;
import com.ivona.services.tts.model.Voice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class IvonaDAOTest {

	private final Logger logger = LoggerFactory.getLogger(IvonaDAOTest.class);
	private static final String DEFAULT_DOWNLOADED_FILE = "/tmp/ivona_downloaded.mp3";
	private IvonaFacadeDAO ivonaDAO;

	@Before
	public void before() {
		this.ivonaDAO = new IvonaFacadeDAO("ivona.properties");
	}

	@Test
	public void getIvonaClient_returnNonNullValue() {
		final IvonaSpeechCloudClient client = this.ivonaDAO.getIvonaSpeechCloudClient();
		assertThat(client, is(notNullValue()));
		assertThat(client.toString(), is(not("")));
	}

	@Test
	public void listVoice_withEmptyParam_returnValidData() {
		final List<Voice> voices = this.ivonaDAO.listVoices();
		assertThat(voices, is(notNullValue()));

		voices.stream().forEach(v -> {
			logger.info("Available voice: {}", v.getName());
		});
	}

	@Test
	public void listLexiconName_returnValidData() {
		final List<String> result = this.ivonaDAO.listLexiconsName();
		assertThat(result, is(notNullValue()));
		assertThat(result.isEmpty(), is(true)); // We don't have any lexicon, yet.

		result.forEach(r -> {
			logger.info("Available lexion name: {}", r);
		});
	}

	@Test
	public void createInCloudSpeech_returnValidData() {
		final URL url = this.ivonaDAO.createInCloudSpeech("Salli", "This is Salli in the cloud.");
		assertThat(url, is(notNullValue()));

		logger.info("URL created: {}", url);
	}

	@Test
	public void createDownloadedSpeech_returnValidData() {
		final String name = "Salli";
		final String data = "This is Salli in downloadable mode.";
		final CreateSpeechResult result = this.ivonaDAO.createDownloadedSpeech(name, data, r -> {
			try (final InputStream is = r.getBody(); 
				 final FileOutputStream os = new FileOutputStream(DEFAULT_DOWNLOADED_FILE)) {
				final byte[] bytes = new byte[(1024 * 2)];
				int readBytes;
				while ((readBytes = is.read(bytes)) > 0) {
					os.write(bytes, 0, readBytes);
				}
			} catch (IOException e) {
				logger.error("Cannot write to path '{}' because {}", DEFAULT_DOWNLOADED_FILE, e.getMessage());
			}
		});
		assertThat(result, is(notNullValue()));
	}
}
