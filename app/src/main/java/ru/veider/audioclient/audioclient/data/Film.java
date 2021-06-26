package ru.veider.audioclient.audioclient.data;

public class Film {
  public VolumeInfo volumeInfo;


  public static class VolumeInfo {
    public Links imageLinks;

    public static class Links {
      public String medium;
    }

  }
}
