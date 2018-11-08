package org.right_brothers.agents.utils;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class InputParser<T> {
	private String filePath;
	
	public InputParser(String filePath) {
		this.filePath = filePath;
	}
	public T parse() {
		InputStream inputStream = this.getClass().getResourceAsStream(filePath);
		if ( inputStream != null ) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				T data = mapper.readValue(inputStream, new TypeReference<T>(){});
				return data;
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		
		throw new IllegalArgumentException(String.format("Ivalid file path: %s", filePath));
	}
}
