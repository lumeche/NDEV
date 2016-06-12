package ndev.util;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ndev.Main;
import ndev.config.Config;

public class ConnectionDelegate {
	private Logger logger = LoggerFactory.getLogger(ConnectionDelegate.class);
	
	private String nmaid;
	private String appKey;
	
	public ConnectionDelegate(String nmaid,String appKey){
		this.nmaid=nmaid;
		this.appKey=appKey;
	}
	public HttpClient buildHTTPClient(int port) throws KeyManagementException, NoSuchAlgorithmException {
		HttpClient httpClient = new DefaultHttpClient();
		Scheme schema = buildHttpSchema(this.buildHttpParams(), port);
		httpClient.getConnectionManager().getSchemeRegistry().register(schema);
		return httpClient;
	}

	private HttpParams buildHttpParams() {
		HttpParams httpParams = new BasicHttpParams();
		HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setHttpElementCharset(httpParams, "utf-8");
		HttpProtocolParams.setUseExpectContinue(httpParams, false);
		return httpParams;
	}

	private Scheme buildHttpSchema(HttpParams params, int port)
			throws NoSuchAlgorithmException, KeyManagementException {

		TrustManager trustManager = new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

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
	
	public List<NameValuePair> getAppParams() {
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		logger.info("NMAID:{}",this.nmaid);
		logger.info("APP_KEYl {}",this.appKey);
		qparams.add(new BasicNameValuePair("appId", this.nmaid));
		qparams.add(new BasicNameValuePair("appKey", this.appKey));
		qparams.add(new BasicNameValuePair("id", Config.DEVICE_ID));
		return qparams;
	}
}
