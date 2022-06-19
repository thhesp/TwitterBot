package de.ths.twitterbot.data;

public class CSVEntryDTO {
    private final String message;
    private final String source;
    private final String hashtags;

    public CSVEntryDTO(String message, String source, String hashtags) {
        this.message = message;
        this.source = source;
        this.hashtags = hashtags;
    }

    public String getMessage() {
        return message;
    }

    public String getSource() {
        return source;
    }


    public String getHashtags() {
        return hashtags;
    }

}
