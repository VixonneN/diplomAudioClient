package ru.veider.audioclient.audioclient.recycler;

public class MediaModel {

    private final String name;
    private final String image;
    private final String uri;

    public MediaModel(String name, String image, String uri) {
        this.name= name;
        this.image = image;
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getUri() {
        return uri;
    }
}
