package de.ths.twitterbot.twitter;

import de.ths.twitterbot.data.CSVEntryDTO;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class TweetHandler {

    protected final static int TWEET_SIZE = 280;

    private static final Logger LOG = LoggerFactory.getLogger(TweetHandler.class);

    @Autowired
    private TwitterConfigProperties twitterConfigProperties;

    public boolean createTweet(final CSVEntryDTO randomEntry) {
        if(randomEntry.getMessage().length() > TWEET_SIZE){
            LOG.info("Message of tweet was too long, skipped tweeting it: {}", randomEntry.getMessage());
            return false;
        }

        StringEntity initialTweet = buildInitialTweet(randomEntry);
        JSONObject jsonResponse = sentHttpRequest(initialTweet);

        if(jsonResponse.has("status")){
            LOG.warn("Problem during the creating of the initial Tweet: {}", jsonResponse.get("status"));
            return false;
        }

        StringEntity sourceReply = buildSourceReply(randomEntry, jsonResponse);
        JSONObject jsonResponse2 = sentHttpRequest(sourceReply);

        if(jsonResponse2.has("status")){
            LOG.warn("Problem during the creating of the source tweet: {}", jsonResponse.get("status"));
        }

        return true;
    }

    protected StringEntity buildInitialTweet(CSVEntryDTO randomEntry) {
        final String tweetMessageWithHashtags = buildMessageWithHashtags(randomEntry);
        final String json = new Tweet(tweetMessageWithHashtags).generateJson();

        return new StringEntity(
                json,
                ContentType.APPLICATION_JSON);
    }

    protected String buildMessageWithHashtags(CSVEntryDTO randomEntry) {
        final String mergedHashTags = mergeHashtags(randomEntry.getHashtags(), twitterConfigProperties.getBaseHashTags());

        // try all hashtags
        final String completeMsg = new StringBuilder().append(randomEntry.getMessage()).append(" ").append(mergedHashTags).toString();
        if(completeMsg.length() <= TWEET_SIZE){
            return completeMsg;
        }

        // try specific hashtags
        final String specificHashTags = Stream.of(randomEntry.getHashtags().split("\\|")).distinct().collect(Collectors.joining(" "));
        final String msgAndSpecificTags = new StringBuilder().append(randomEntry.getMessage()).append(" ").append(specificHashTags).toString();
        if(msgAndSpecificTags.length() <= TWEET_SIZE){
            return msgAndSpecificTags;
        }

        // base hashtags
        final String onlybaseHashTags = Stream.of(twitterConfigProperties.getBaseHashTags().split("\\|")).distinct().collect(Collectors.joining(" "));
        final String msgAndBaseTags = new StringBuilder().append(randomEntry.getMessage()).append(" ").append(onlybaseHashTags).toString();
        if(msgAndBaseTags.length() <= TWEET_SIZE){
            return msgAndBaseTags;
        }

        return randomEntry.getMessage();
    }

    protected String mergeHashtags(final String entryHashtags, final String baseHashTags){
        final List<String> hashTags = Stream.concat(Stream.of(entryHashtags.split("\\|")), Stream.of(baseHashTags.split("\\|")))
                .toList();

        return hashTags.stream().map(String::trim).distinct().sorted().collect(Collectors.joining(" "));
    }

    protected StringEntity buildSourceReply(CSVEntryDTO randomEntry, JSONObject jsonResponse) {
        final String json = new Tweet(randomEntry.getSource(), jsonResponse.getJSONObject("data").getString("id")).generateJson();

        return new StringEntity(
                json,
                ContentType.APPLICATION_JSON);
    }

    private JSONObject sentHttpRequest(final StringEntity json) {
        OAuthConsumer consumer = new CommonsHttpOAuthConsumer(twitterConfigProperties.getConsumerKey(),
                twitterConfigProperties.getConsumerSecret());
        consumer.setTokenWithSecret(twitterConfigProperties.getAccessToken(), twitterConfigProperties.getAccessTokenSecret());

        try (CloseableHttpClient httpclient = HttpClientBuilder.create().build()) {
            HttpPost httpPost = new HttpPost(twitterConfigProperties.getTweetUri());

            httpPost.setEntity(json);
            consumer.sign(httpPost);
            org.apache.http.HttpResponse response = httpclient.execute(httpPost);
            LOG.info(response.getStatusLine().toString());
            HttpEntity entity = response.getEntity();
            String jsonResponse = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            LOG.info(jsonResponse);

            return new JSONObject(jsonResponse);

        } catch (IOException e) {
            LOG.error("IO Exception during posting of tweet: ", e);
            throw new RuntimeException(e);
        } catch (OAuthMessageSignerException | OAuthCommunicationException | OAuthExpectationFailedException ex) {
            LOG.error("Error during OAuth1 signing: ", ex);
            throw new RuntimeException(ex);
        }
    }
}



