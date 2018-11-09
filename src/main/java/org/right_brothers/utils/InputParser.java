package org.right_brothers.utils;

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
        String file = this.getClass().getResource(filePath).getPath();
        System.out.println(file);
        if ( inputStream != null ) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                T data = mapper.readValue(inputStream, type);
                return data;
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        throw new IllegalArgumentException(String.format("Invalid file path: %s", filePath));
    }
}
