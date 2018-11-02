package com.trace.demo.model.redis;

import java.io.Serializable;
import java.util.Map;

public class RedisMessage implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 4249438952737587740L;

    private String contentType;

    private Map<String, String> header;

    private Object message;

    /**
     * 
     */
    public RedisMessage() {
        super();
    }

    /**
     * @param contentType
     * @param header
     * @param message
     */
    public RedisMessage(String contentType, Map<String, String> header, Object message) {
        super();
        this.contentType = contentType;
        this.header = header;
        this.message = message;
    }

    /**
     * @return the contentType
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * @return the header
     */
    public Map<String, String> getHeader() {
        return header;
    }

    /**
     * @param header the header to set
     */
    public void setHeader(Map<String, String> header) {
        this.header = header;
    }

    /**
     * @return the message
     */
    public Object getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(Object message) {
        this.message = message;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("RedisMessage { contentType= %s , header= %s , message= %s }", contentType, header, message);
    }

}
