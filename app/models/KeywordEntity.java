package models;

public class KeywordEntity {
    protected String keywordRepresentation;
    protected String uri;
    protected Boolean isBuzzword;

    public KeywordEntity() {
        this.isBuzzword = Boolean.FALSE;
    }

    public String getKeywordRepresentation() {
        return keywordRepresentation;
    }

    public void setKeywordRepresentation(String keywordRepresentation) {
        this.keywordRepresentation = keywordRepresentation;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Boolean isBuzzword() {
        return isBuzzword;
    }

    public void setBuzzword(Boolean buzzword) {
        isBuzzword = buzzword;
    }

    @Override
    public boolean equals(Object k) {
        if (k instanceof KeywordEntity)
            return this.getUri().equalsIgnoreCase(((KeywordEntity)k).getUri());
        else
            return false;
    }
}
