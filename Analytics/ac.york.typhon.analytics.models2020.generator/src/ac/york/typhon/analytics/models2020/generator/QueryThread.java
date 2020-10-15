package ac.york.typhon.analytics.models2020.generator;

import java.util.ArrayList;
import java.util.Random;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import sun.misc.BASE64Encoder;

public class QueryThread implements Runnable {

	final static int NUM_OF_SELECTS = 2;
	ArrayList<String> productUUIDs = new ArrayList<String>();
	WebResource webResource;
	String authStringEnc;
	
	public QueryThread(ArrayList<String> productUUIDs, WebResource webResource, String authStringEnc) {
		this.productUUIDs = productUUIDs;
		this.webResource = webResource;
		this.authStringEnc = authStringEnc;
	}

	@Override
	public void run() {
		
		System.out.println(Thread.currentThread());
		Random randomGenerator = new Random();
		
		for (int i = 0; i < NUM_OF_SELECTS; i++) {
			Query queryObj = new Query();
			String query = "from Product p select p.name where p.@id == #"+ productUUIDs.get(randomGenerator.nextInt(productUUIDs.size()));
			queryObj.setQuery(query);
			ObjectMapper mapper = new ObjectMapper();
			String json = "";
			try {
			  json = mapper.writeValueAsString(queryObj);
			  System.out.println("ResultingJSONstring = " + json);
			  //System.out.println(json);
			} catch (JsonProcessingException e) {
			   e.printStackTrace();
			}
			
			ClientResponse resp = webResource.accept("application/json").header("Authorization", "Basic " + authStringEnc)
					.type("application/json").post(ClientResponse.class, json);
			if (resp.getStatus() != 200) {
				System.err.println("Unable to connect to the server");
			}
			String output = resp.getEntity(String.class);
		}

	}

}
