package com.stockman.mlkitgesturetalk;
//inspiration for this method of making a class to store the informations came from
//https://www.youtube.com/watch?v=94rCjYxvzEE&t=917s
public class MyModel {
    Integer icons;
    String words;

    public MyModel(Integer icons, String words) {
        this.icons = icons;
        this.words = words;
    }

    public Integer getIcons() {
        return icons;
    }

    public String getWords() {
        return words;
    }
}
