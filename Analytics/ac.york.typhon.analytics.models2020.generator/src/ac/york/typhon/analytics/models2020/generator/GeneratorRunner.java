package ac.york.typhon.analytics.models2020.generator;

import java.util.ArrayList;
import java.util.UUID;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import sun.misc.BASE64Encoder;

public class GeneratorRunner {
	
	final static int NUM_OF_PRODUCTS = 50;
	final static int NUM_OF_THREADS = 100;

	public static void main(String[] args) {

		String url = "http://localhost:8080/api/query";
		String name = "admin";
		String password = "admin1@";
		String authString = name + ":" + password;
		String authStringEnc = new BASE64Encoder().encode(authString.getBytes());
		Client restClient = Client.create();
		WebResource webResource = restClient.resource(url);
		
		ArrayList<String> productUUIDs = new ArrayList<String>();
		for (int i = 0; i < NUM_OF_PRODUCTS; i++) {
			UUID uuid = UUID.randomUUID();
			productUUIDs.add(uuid.toString());
		}
		
		for (int i = 0; i < NUM_OF_THREADS; i++) {
			QueryThread queryThread = new QueryThread(productUUIDs, webResource, authStringEnc);
			Thread thread = new Thread(queryThread);
			thread.start();
		}
		System.out.println("Finished Thread Creation");

	}

}
