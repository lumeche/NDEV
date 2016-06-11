package ndev.tts;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.net.ssl.SSLEngineResult.Status;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ndev.NDEVTester;
import ndev.config.Config;
import ndev.dictation.Dictation;
import ndev.util.ConnectionDelegate;
/**
 * 
 * @author LuisMiguel
 * Notes: Check how the chunk works with httpServlet
 *
 */
public class TextToSpeech implements NDEVTester {
	Logger logger = LoggerFactory.getLogger(TextToSpeech.class);
	ConnectionDelegate connDelegate = new ConnectionDelegate();

	public void runTest() throws Exception {
		HttpClient httpClient = connDelegate.buildHTTPClient(Config.PORT);
		URI uri = buildUri();
		HttpPost post = getHeader(uri);
		HttpResponse response = httpClient.execute(post);
		processResponse(response);
	}

	private boolean processResponse(HttpResponse response) {
		HttpEntity entity = response.getEntity();
		StatusLine status = response.getStatusLine();
		logger.info("StatusLine: {}", status);
		logger.info("Chunked: {}", entity.isChunked());
		
		Header date = response.getFirstHeader("Date");
		if (date != null){
			logger.info("Date: {}",date.getValue());
		}
		
		Header sessionid = response.getFirstHeader("x-nuance-sessionid");
		if (sessionid != null){
			logger.info("x-nuance-sessionid: {}", sessionid.getValue());			
		}
		if (status.getStatusCode() != 200) {
			logger.error("Error processing request. Response {}. Detail {}", status.getStatusCode(),
					status.getReasonPhrase());
			return false;
		}
		InputStream content = null;
		FileOutputStream audioFile = null;
		try {
			content = entity.getContent();
			audioFile = new FileOutputStream(Config.TTS_FILE);
			byte[] data = new byte[1024 * 16];
			int len=0;
			while ((len=content.read(data, 0, data.length)) > 0) {
				audioFile.write(data,0,len);
			}
			audioFile.close();
			EntityUtils.consume(entity);
			logger.info("Audio obtained");
		} catch (Exception e) {
			logger.error("Error getting audio",e);
		} finally {
			try {
				content.close();
				audioFile.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return true;

	}

	private URI buildUri() throws URISyntaxException {
		List<NameValuePair> appParams = connDelegate.getAppParams();
		appParams.add(new BasicNameValuePair("voice", Config.VOICE));
		URI uri = URIUtils.createURI("https", Config.HOSTNAME, Config.PORT, Config.TTS_SERVER_PATH,
				URLEncodedUtils.format(appParams, "UTF-8"), null);
		return uri;
	}

	private HttpPost getHeader(URI uri) throws UnsupportedEncodingException {
		HttpPost httppost = new HttpPost(uri);
		httppost.addHeader("Content-Type", "text/plain");
		httppost.addHeader("Accept", Config.CODEC);
		HttpEntity entity = new StringEntity(Config.TEXT, "utf-8");
		;
		httppost.setEntity(entity);
		return httppost;
	}

}
