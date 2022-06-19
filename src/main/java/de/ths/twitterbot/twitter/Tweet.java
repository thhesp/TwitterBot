package de.ths.twitterbot.twitter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tweet {

    private static Logger LOG = LoggerFactory.getLogger(Tweet.class);

    private String message;
    private String replyTo;

    public Tweet(final String message){
        this.message = message;
    }

    public Tweet(final String message, final String replyTo){
        this(message);
        this.replyTo = replyTo;
    }

    public String generateJson(){
        StringBuilder builder = new StringBuilder();

        builder.append("{\"text\":\"").append(message).append("\"");

        if(StringUtils.isNotEmpty(replyTo)){
            builder.append(", \"reply\": {\"in_reply_to_tweet_id\":\"").append(replyTo).append("\"}");
        }
        builder.append("}");

        String json = builder.toString();
        LOG.info("Generated Json: {}", json);
        return json;
    }
}
