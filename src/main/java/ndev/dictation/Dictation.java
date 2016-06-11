package ndev.dictation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.FileUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ndev.NDEVTester;
import ndev.config.Config;
import ndev.util.streamer.FileAudioStreamer;

public class Dictation implements NDEVTester {
	Logger logger = LoggerFactory.getLogger(Dictation.class);
	public void runTest() throws Exception {		
		HttpClient client = this.buildHTTPClient(Config.PORT);
		HttpEntity entity = this.buildAudioStream(Config.TTS_FILE, false);
		HttpPost post = this.buildPostRequest(Config.LANGUAGE);
		post.setEntity(entity);
		HttpResponse response = client.execute(post);
		processDictationResponse(response, Config.DICTATION_RESULT_PATH);		
	}

	private HttpPost buildPostRequest(String language) throws URISyntaxException{
		URI uri = this.buildURI();
		HttpPost httppost = new HttpPost(uri);
		httppost.addHeader("Content-Type",  Config.CODEC);
		httppost.addHeader("Content-Language", language);
		httppost.addHeader("Accept-Language", language);
		httppost.addHeader("Accept", Config.RESULTS_FORMAT);
		httppost.addHeader("Accept-Topic", Config.TOPIC);
		return httppost;
	}
	
	private URI buildURI() throws URISyntaxException {
		return URIUtils.createURI("https", Config.HOSTNAME, Config.PORT, Config.DICTATION_SERVER_PATH,
				URLEncodedUtils.format(this.getAppParams(), "utf-8"), null);
	}

	private List<NameValuePair> getAppParams() {
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair("appId", Config.APP_ID));
		qparams.add(new BasicNameValuePair("appKey", Config.APP_KEY));
		qparams.add(new BasicNameValuePair("id", Config.DEVICE_ID));
		return qparams;
	}

	

	private HttpClient buildHTTPClient(int port) throws KeyManagementException, NoSuchAlgorithmException {
		HttpClient httpClient = new DefaultHttpClient();
		Scheme schema = buildHttpSchema(this.buildHttpParams(), port);
		httpClient.getConnectionManager().getSchemeRegistry().register(schema);
		return httpClient;
	}

	private Scheme buildHttpSchema(HttpParams params, int port)
			throws NoSuchAlgorithmException, KeyManagementException {

		TrustManager trustManager = new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {return null;}

			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			}

			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			}
		};
		SSLContext sslContext;
		sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, new TrustManager[] { trustManager }, null);
		SSLSocketFactory sf = new SSLSocketFactory(sslContext);
		sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		return new Scheme("https", sf, port);

	}
	private HttpParams buildHttpParams() {
		HttpParams httpParams = new BasicHttpParams();
		HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setHttpElementCharset(httpParams, "utf-8");
		HttpProtocolParams.setUseExpectContinue(httpParams, false);
		return httpParams;
	}
	private InputStreamEntity buildAudioStream(String filePath, boolean isStreamed) throws Exception {
		File audioFile = new File(filePath);
		if (!audioFile.exists()) {
			throw new FileNotFoundException("File " + filePath + " doesn't exist");
		}
		FileAudioStreamer audioStreamer = new FileAudioStreamer(audioFile.getAbsolutePath(), isStreamed, false,
				Config.SAMPLE_RATE);
		long audioLenght = 0L;
		if (!isStreamed) {
			audioLenght = audioFile.length();
		}
		InputStreamEntity streamEntity = new InputStreamEntity(audioStreamer.getInputStream(), audioLenght);
		audioStreamer.start();
		streamEntity.setContentType(Config.CODEC);
		return streamEntity;
	}

	private void processDictationResponse(HttpResponse response, String outputPath) {
		HttpEntity entinty = response.getEntity();
		getCookie(response);
		try {
			InputStream content = entinty.getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(content, "utf-8"));
			String line;
			File outputFile = new File(outputPath);
			outputFile.delete();
			while ((line = reader.readLine()) != null) {
				logger.info("line: {}",line);
				FileUtils.writeStringToFile(outputFile, line);
			}
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			logger.error("Error",e);
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("Error",e);
		} finally {
			try {
				EntityUtils.consume(entinty);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				logger.error("Error",e);
			}
		}
	}

	private void getCookie(HttpResponse response) {
		try {
			Header cookieHeader = response.getFirstHeader("Set-Cookie");
			String cookie = new StringTokenizer(cookieHeader.getValue(), ";").nextToken().trim();
			logger.info("cookie: {}",cookie);			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
