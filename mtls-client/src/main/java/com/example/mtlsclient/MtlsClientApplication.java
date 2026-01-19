package com.example.mtlsclient;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.KeyStore;

@SpringBootApplication
public class MtlsClientApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(MtlsClientApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		String p12Password = "password";

		KeyStore keyStore = KeyStore.getInstance("PKCS12");
		keyStore.load(new ClassPathResource("certs/client.p12").getInputStream(), p12Password.toCharArray());

		KeyStore trustStore = KeyStore.getInstance("JKS");
		trustStore.load(new ClassPathResource("certs/ca-truststore.jks").getInputStream(), p12Password.toCharArray());

		SSLContext sslContext = SSLContextBuilder.create()
				.loadKeyMaterial(keyStore, p12Password.toCharArray())
				.loadTrustMaterial(trustStore, new org.apache.hc.client5.http.ssl.TrustSelfSignedStrategy())
				.build();

		HttpClient httpClient = HttpClients.custom()
				.setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
						.setTlsSocketStrategy(new DefaultClientTlsStrategy(sslContext))
						.build())
				.build();

		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
		RestTemplate restTemplate = new RestTemplate(factory);

		System.out.println("--- Starting mTLS PATCH Request ---");
		try {
			ResponseEntity<String> response = restTemplate.exchange(
					"https://localhost:8443/heartbeat",
					HttpMethod.PATCH,
					null,
					String.class);
			System.out.println("Success: " + response.getBody());
		} catch (HttpClientErrorException e) {
			System.out.println("Server returned an error: " + e.getStatusCode());
			System.out.println("Message: " + e.getResponseBodyAsString());
		} catch (Exception e) {
			System.out.println("Connection Error: " + e.getMessage());
		}
	}
}