package entity.event;

/**
 * Created by Irina
 */
public class TextMessage {
    private String title;
    private String header;
    private String body;

    public TextMessage(String title) {
        this.title = title;
        this.header = title;
        this.body = title;
    }

    public TextMessage setTitle(String title) {
        this.title = title;
        return this;
    }

    public TextMessage setHeader(String header) {
        this.header = header;
        return this;
    }

    public TextMessage setBody(String body) {
        this.body = body;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public String getHeader() {
        return header;
    }

    public String getBody() {
        return body;
    }

}
