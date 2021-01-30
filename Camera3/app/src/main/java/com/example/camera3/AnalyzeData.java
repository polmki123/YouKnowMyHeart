package com.example.camera3;

public class AnalyzeData {
    private int idx;
    private String foodName;
    private int Anger;
    private int Contempt;
    private int Disgust;
    private int Fear;
    private int Happiness;
    private int Neutral;
    private int Sadness;
    private int Surprise;

    public AnalyzeData(){ }

    public int setIdx(int n){ return idx = n; }
    public String setFoodName(String s){ return foodName = s; }
    public int setAnger(int n){ return Anger = n; }
    public int setContempt(int n){ return Contempt = n; }
    public int setDisgust(int n){ return Disgust = n; }
    public int setFear(int n){ return Fear = n; }
    public int setHappiness(int n){ return Happiness = n; }
    public int setNeutral(int n){ return Neutral = n; }
    public int setSadness(int n){ return Sadness = n; }
    public int setSurprise(int n){ return Surprise = n; }

    public int getIdx(){ return idx; }
    public String getFoodName(){ return foodName; }
    public int getAnger(){ return Anger; }
    public int getContempt(){ return Contempt; }
    public int getDisgust(){ return Disgust; }
    public int getFear(){ return Fear; }
    public int getHappiness(){ return Happiness; }
    public int getNeutral(){ return Neutral; }
    public int getSadness(){ return Sadness; }
    public int getSurprise(){ return Surprise; }
}
