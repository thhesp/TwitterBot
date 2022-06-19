package de.ths.twitterbot.twitter;


import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import de.ths.twitterbot.data.CSVEntryDTO;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static de.ths.twitterbot.twitter.TweetHandler.TWEET_SIZE;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
public class TweetHandlerTest {

    WireMockServer wireMockServer;

    @BeforeEach
    public void setup () {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
    }

    @AfterEach
    public void teardown () {
        wireMockServer.stop();
    }

    @Mock
    private TwitterConfigProperties configProperties;

    @InjectMocks
    private TweetHandler sut;

    @Test
    public void test_mergeHashtags_noCollisions(){
        final String hashtagsOne = "#first|#fourth";
        final String hashtagsTwo = "#second|#third";
        final String merged = sut.mergeHashtags(hashtagsOne, hashtagsTwo);

        assertTrue(merged.contains("#first"));
        assertTrue(merged.contains("#second"));
        assertTrue(merged.contains("#third"));
        assertTrue(merged.contains("#fourth"));
    }

    @Test
    public void test_mergeHashtags_multipleCollisions(){
        final String hashtagsOne = "#first|#fourth|#third";
        final String hashtagsTwo = "#second|#third|#fifth|#first";
        final String merged = sut.mergeHashtags(hashtagsOne, hashtagsTwo);

        assertTrue(merged.contains("#first"));
        assertTrue(merged.contains("#second"));
        assertTrue(merged.contains("#third"));
        assertTrue(merged.contains("#fourth"));
        assertTrue(merged.contains("#fifth"));
        assertEquals(1, StringUtils.countMatches(merged, "#first"));
        assertEquals(1, StringUtils.countMatches(merged, "#third"));
    }

    @Test
    public void test_mergeHashtags_inOrder(){
        final String hashtagsOne = "#a|#c";
        final String hashtagsTwo = "#b|#d";
        final String merged = sut.mergeHashtags(hashtagsOne, hashtagsTwo);

        assertEquals("#a #b #c #d", merged);
    }

    @Test
    public void test_buildMessageWithHashtagsAllTags(){
        when(configProperties.getBaseHashTags()).thenReturn("#basetag");
        CSVEntryDTO entry = new CSVEntryDTO("message", "source", "#testhashtag");
        final String message = sut.buildMessageWithHashtags(entry);

        assertTrue(message.length() <= TWEET_SIZE);
        assertEquals("message #basetag #testhashtag", message);
    }

    @Test
    public void test_buildOnlySpecificHashtag(){
        when(configProperties.getBaseHashTags()).thenReturn("#basetag");
        CSVEntryDTO entry = new CSVEntryDTO(RandomStringUtils.randomAlphanumeric(TWEET_SIZE - " #testhashtag".length()), "source", "#testhashtag");
        final String message = sut.buildMessageWithHashtags(entry);

        assertTrue(message.length() <= TWEET_SIZE);
        assertEquals(entry.getMessage() + " #testhashtag", message);
    }

    @Test
    public void test_buildOnlyBasetag(){
        when(configProperties.getBaseHashTags()).thenReturn("#basetag");
        CSVEntryDTO entry = new CSVEntryDTO(RandomStringUtils.randomAlphanumeric(TWEET_SIZE - " #basetag".length()), "source", "#testhashtag");
        final String message = sut.buildMessageWithHashtags(entry);

        assertTrue(message.length() <= TWEET_SIZE);
        assertEquals(entry.getMessage() + " #basetag", message);
    }

    @Test
    public void test_buildInitialTweet() throws IOException {
        when(configProperties.getBaseHashTags()).thenReturn("#basetag");
        CSVEntryDTO entry = new CSVEntryDTO("message", "source", "#testhashtag");

        StringEntity stringEntity = sut.buildInitialTweet(entry);
        assertEquals("{\"text\":\"message #basetag #testhashtag\"}", EntityUtils.toString(stringEntity));
    }

    @Test
    public void test_buildSourceReply() throws IOException, JSONException {
        when(configProperties.getBaseHashTags()).thenReturn("#basetag");
        CSVEntryDTO entry = new CSVEntryDTO("message", "source", "#testhashtag");

        JSONObject jsonobject = new JSONObject();
        JSONObject internalObject = new JSONObject();
        internalObject.put("id", "123");
        jsonobject.put("data", internalObject);

        StringEntity stringEntity = sut.buildSourceReply(entry, jsonobject);
        assertEquals("{\"text\":\"source\", \"reply\": {\"in_reply_to_tweet_id\":\"123\"}}", EntityUtils.toString(stringEntity));
    }

    @Test
    public void test_createTweetTooLongMessage() {
        CSVEntryDTO entry = new CSVEntryDTO(RandomStringUtils.randomAlphanumeric(TWEET_SIZE + 1), "source", "#testhashtag");

        assertFalse(sut.createTweet(entry));
    }

    @Test
    public void test_createTweetErrorDuringInitialTweet() {
        when(configProperties.getBaseHashTags()).thenReturn("");
        when(configProperties.getTweetUri()).thenReturn("http://localhost:8089/tweet");
        when(configProperties.getConsumerKey()).thenReturn("consumerKey");
        when(configProperties.getConsumerSecret()).thenReturn("consumerSecret");
        when(configProperties.getAccessToken()).thenReturn("acessToken");
        when(configProperties.getAccessTokenSecret()).thenReturn("accessTokenSecret");
        CSVEntryDTO entry = new CSVEntryDTO("message", "source", "#testhashtag");

        wireMockServer.stubFor(post(urlEqualTo("/tweet"))
                .willReturn(aResponse()
                        .withBody("{\"detail\":\"You are not allowed to create a Tweet with duplicate content.\",\"type\":\"about:blank\",\"title\":\"Forbidden\",\"status\":403}")
                        .withStatus(403)));

        assertFalse(sut.createTweet(entry));
    }

    @Test
    public void test_createTweetSuccess() {
        when(configProperties.getBaseHashTags()).thenReturn("");
        when(configProperties.getTweetUri()).thenReturn("http://localhost:8089/tweet");
        when(configProperties.getConsumerKey()).thenReturn("consumerKey");
        when(configProperties.getConsumerSecret()).thenReturn("consumerSecret");
        when(configProperties.getAccessToken()).thenReturn("acessToken");
        when(configProperties.getAccessTokenSecret()).thenReturn("accessTokenSecret");
        CSVEntryDTO entry = new CSVEntryDTO("message", "source", "#testhashtag");

        wireMockServer.stubFor(post(urlEqualTo("/tweet"))
                .willReturn(aResponse()
                        .withBody("{\"data\":{\"id\":\"1538590645165621248\",\"text\":\"testmessage #climate #test\"}}")
                        .withStatus(201)));

        assertTrue(sut.createTweet(entry));

        wireMockServer.verify(2, anyRequestedFor(urlEqualTo("/tweet")));
    }
}