package com.trace.demo.model.redis;

import org.springframework.data.redis.connection.Message;
import org.springframework.util.SerializationUtils;

public class RedisDefaultMessage implements Message {

    /**
     * 
     */
    private static final long serialVersionUID = 7102998256138463036L;

    private final byte[] channel;

    private final byte[] body;

    private String toString;

    /**
     * @param channel
     * @param body
     */
    public RedisDefaultMessage(byte[] channel, byte[] body) {
        super();
        this.channel = channel;
        this.body = body;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.data.redis.connection.Message#getBody()
     */
    @Override
    public byte[] getBody() {
        return (this.body != null ? this.body.clone() : null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.data.redis.connection.Message#getChannel()
     */
    @Override
    public byte[] getChannel() {
        return (this.channel != null ? this.channel.clone() : null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if (this.toString == null) {
            this.toString = (String) SerializationUtils.deserialize((this.body));
        }
        return this.toString;
    }

}
