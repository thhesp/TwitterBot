package de.ths.twitterbot.data;

public class CSVEntryDTO {
    private String message;
    private String source;
    private String hashtags;

    public CSVEntryDTO(String message, String source, String hashtags) {
        this.message = message;
        this.source = source;
        this.hashtags = hashtags;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getHashtags() {
        return hashtags;
    }

    public void setHashtags(String hashtags) {
        this.hashtags = hashtags;
    }

}
