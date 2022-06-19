package de.ths.twitterbot;

import de.ths.twitterbot.data.CSVEntryDTO;
import de.ths.twitterbot.data.CSVLoader;
import de.ths.twitterbot.twitter.TweetHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
public class BotPipeline {

    private static final int MAX_RETRIES = 5;
    private static final Logger LOG = LoggerFactory.getLogger(BotPipeline.class);
    @Autowired
    private CSVLoader csvLoader;
    @Autowired
    private TweetHandler tweetHandler;

    public void uploadRandomTweet(){
        List<CSVEntryDTO> entries = csvLoader.loadCSVData();

        CSVEntryDTO randomEntry;

        for(int i = 0; i < MAX_RETRIES; i++){
            randomEntry = getRandomEntry(entries);
            if(tweetHandler.createTweet(randomEntry)){
                LOG.info("Sucessfully created a tweet. Shutting down again.");
                break;
            }
        }

    }

    private CSVEntryDTO getRandomEntry(List<CSVEntryDTO> entries) {
        Random rand = new Random();
        return entries.get(rand.nextInt(entries.size()));
    }
}
