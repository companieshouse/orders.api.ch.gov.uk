package uk.gov.companieshouse.orders.api.kafka;

import org.apache.avro.Schema;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class AvroSchemaHelper {
	
	/**
	 * Retrieve the Avro schema from the Confluent schema registry
	 * 
	 * @param url
	 * @return schema
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static Schema getSchema(String url) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		
		if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			try(InputStream is = connection.getInputStream()) {
				try(BufferedReader in = new BufferedReader(new InputStreamReader(is))) {
					StringBuilder response = new StringBuilder();
					String line;
					while ((line = in.readLine()) != null) {
						response.append(line);
					}
					
					return convertResponseToSchema(response);
				}
			}
		}
		
		return null;
	}

	private static Schema convertResponseToSchema(StringBuilder response) {
		if(response != null && response.length() > 0) {
			String schemaString = response.toString();
			JSONObject schemaJson = new JSONObject(schemaString);
			String schema = schemaJson.getString("schema");			
			
			Schema.Parser parser = new Schema.Parser();
			return parser.parse(schema);
		}
		
		return null;
	}
}