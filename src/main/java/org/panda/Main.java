package org.panda;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
//        YTScrapper.scrape("Speak Now");
        System.out.println("Video link: "+YTScrapper.getVideoStreamUrl("https://www.youtube.com/watch?v=xEJHYd6pNaU"));
        System.out.println("Audio link: "+YTScrapper.getAudioStreamUrl("https://www.youtube.com/watch?v=xEJHYd6pNaU"));
    }
}