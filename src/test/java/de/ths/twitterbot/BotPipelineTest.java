package de.ths.twitterbot;

import de.ths.twitterbot.data.CSVEntryDTO;
import de.ths.twitterbot.data.CSVLoader;
import de.ths.twitterbot.twitter.TweetHandler;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class BotPipelineTest {


    @Mock
    private CSVLoader mockCSVLoader;

    @Mock
    private TweetHandler tweetHandler;

    @InjectMocks
    private BotPipeline sut;

    @Test
    public void test_uploadRandomTweet(){
        List<CSVEntryDTO> csvEntries = new ArrayList<>();
        csvEntries.add(new CSVEntryDTO("message", "source", "hashtag"));
        when(mockCSVLoader.loadCSVData()).thenReturn(csvEntries);

        when(tweetHandler.createTweet(any())).thenReturn(true);
        sut.uploadRandomTweet();

        verify(tweetHandler, times(1)).createTweet(any());
        verify(mockCSVLoader, times(1)).loadCSVData();
    }

    @Test
    public void test_uploadRandomTweetWithRetry(){
        List<CSVEntryDTO> csvEntries = new ArrayList<>();
        csvEntries.add(new CSVEntryDTO("message", "source", "hashtag"));
        csvEntries.add(new CSVEntryDTO("message2", "source2", "hashtag2"));
        when(mockCSVLoader.loadCSVData()).thenReturn(csvEntries);

        when(tweetHandler.createTweet(any())).thenReturn(false).thenReturn(true);
        sut.uploadRandomTweet();

        verify(tweetHandler, times(2)).createTweet(any());
        verify(mockCSVLoader, times(1)).loadCSVData();
    }

}