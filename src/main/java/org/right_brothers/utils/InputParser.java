package org.right_brothers.agents.utils;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class InputParser<T> {
	private String filePath;
	private TypeReference<?> type;
	
	public InputParser(String filePath, TypeReference<?> type) {
		this.filePath = filePath;
		this.type = type;
	}
	public T parse() {
		InputStream inputStream = this.getClass().getResourceAsStream(filePath);
		if ( inputStream != null ) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				T data = mapper.readValue(inputStream, type);
				return data;
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		
		throw new IllegalArgumentException(String.format("Ivalid file path: %s", filePath));
	}
}
