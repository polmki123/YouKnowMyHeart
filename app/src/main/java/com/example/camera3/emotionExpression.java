package com.example.camera3;

import android.util.Log;

import java.util.Random;

public class emotionExpression {

    final private static String TAG = "FOOD";

    double[][] emotion = new double[2][7];
    String[] analyzeStatement = new String[2];
    String mainEmotionStatement = " ", subEmotionStatement = " ";
    //생성자
    emotionExpression(){};

    public void setEmotion(int index, double emo)
    {
        emotion[0][index] = index;
        emotion[1][index] = emo;
    }

    public String[] getTopEmotion()
    {
        Log.i(TAG,"행 길이: "+ emotion.length);
        Log.i(TAG,"열 길이: "+ emotion[1].length);

        for(int i=0;i<emotion[1].length;i++)
        {
            for(int j=i+1; j<emotion[1].length;j++)
            {
                if(emotion[1][i] < emotion[1][j])
                {
                    double tmp = emotion[1][i];
                    emotion[1][i] = emotion[1][j];
                    emotion[1][j] = tmp;

                    double idx = emotion[0][i];
                    emotion[0][i] = emotion[0][j];
                    emotion[0][j] = idx;
                }
            }
        }

        for (int i = 0 ; i<emotion[1].length; i++)
        {
            Log.i(TAG, "정렬결과 :"+emotion[1][i]);
            Log.i(TAG, "정렬결과 값 :"+emotion[0][i]);
        }
        // 0 값 채크
        int zeroCount = 0;
        for(int i=0;i<emotion[1].length;i++)
        {
            if(emotion[1][i] <= 0.0){ zeroCount++; }
        }
        if(zeroCount <= 3){ zeroCount = 3;}
        else if (zeroCount > 3 && zeroCount <6) { zeroCount = zeroCount-3;}
        else { zeroCount =0;}

        Log.i(TAG,"0갯수: "+ zeroCount);
        String[] em = new String[zeroCount];
        if (zeroCount >0)
        {
            for(int i=0;i<em.length;i++)
            {
                Log.i(TAG, "감정"+i+emotion[1][i]);
                if     (emotion[0][i] == 0) { em[i] = "분노";}
                else if(emotion[0][i] == 1) { em[i] = "혐오";}
                else if(emotion[0][i] == 2) { em[i] = "공포";}
                else if(emotion[0][i] == 3) { em[i] = "행복";}
                else if(emotion[0][i] == 4) { em[i] = "보통";}
                else if(emotion[0][i] == 5) { em[i] = "슬픔";}
                else                        { em[i] = "놀람"; }
            }
        }


        String[][] mainEmotion = {// 10개씩 맞추기
                            //화난 감정 표현
                            {"가슴속에 불꽃을 표출하고 싶은", "감정이 일탈하고 싶어하는", "딥빡이 스치고간",
                            "딥빡친", "흥분한","북받친", "적색경보", "터져나오는", "분노의 화신인", "폭발한"},
                            //혐오 감정 표현
                            {"하기싫은 무언가를 마주한 것 같은", "데스노트를 원하고 있는", "싫어한느 먹기 직전의 감정을 지닌",
                            "마냥 싫은", "심술난", "앙칼진", "흥칫뿡", "예민보스", "썩은 미소를 짓고 있는", "ㅡㅠㅡ"},
                            //공포 감정 표현
                            {"등뒤에 서늘함을 느끼는", "혼자서 밥먹으면 무서워할", "근심과 걱정을 아직 내려놓지 못한",
                            "겁쟁이", "두려워하는", "쫄보", "ㄷㄷㄷ", "후덜덜 거리는", ":-(", "으아악"},
                            //행복 감정 표현
                            {"오늘이 최고의 날인것 같은", "웃음이 끊이지 않는", "복권이라도 당첨된듯한", "마냥좋은",
                            "설레는", "명랑한", "사랑가득한", "가슴벅찬", "므흣한", "황홀한"},
                            //보통 감정 표현
                            {"그 무언가에 휘둘리지 않는 평온한", "부처님도 울고 갈 법한 평정심을 지닌", "잔잔한 겨울바다에 있는 듯한",
                            "도도한", "공허한", "평범한", "멍한", "편안한", "마른 감성의", "권태로운"},
                            //슬픔 감정 표현
                            {"사슴 눈망울이 되어 슬픈 영화를 보고싶은", "헤어진 애인이 생각나는 듯한", "슬픈 일들이 생각나는",
                            "울적한", "센치한", "아련한", "고독한", "처량한", "ㅠ_ㅠ", "OTL"},
                            //놀람 감정 표현
                            {"살다가 처음 신기한 것을 본듯한", "세상 처음 겪는 일을 겪은 듯한", "재밌는 일을 겪기 직전 같은",
                            "전율이 온", "두려운", "섬찟한", "깜놀한", "+_+", "!_!", "@_@"}
        };

        String [][] subEmotion = {// 같은 값이 나올 경우는 없음
                                    //분노+@
                                    {"아주 화가난", "한바탕 폭풍이 휘몰아 치고 난 뒤 같은", "심통나고 오싹한 기분의", "빡치면서도 자유의 시간을 기다리고 있는",
                                    "조금 화나지만 참을만 한", "조금 화나면서 서러운", "어멋 부들부들, 어멋 부들부들 하는"},
                                    //혐오+@
                                    {"한바탕 폭풍이 휘몰아 치고 난 뒤 같은", "아주 혐오하고 있는", "하기 싫은 일을 해야할 시간이 다가옴을 느끼는",
                                    "예민 보스지만 행복한", "심술을 숨긴 보통날의", "예민 보스가 생각나서 먼지가 되고 싶은", "마냥 싫지만 좀 신기해 하는"},
                                    //공포+@
                                    {"심통나고 오싹한 기분의", "하기 싫은 일을 해야할 시간이 다가옴을 느끼는", "아주 두려워 하는",
                                    "썩소를 머금은", "살짝 겁먹은 고양이 같은", "무서워서 눈물 나는", "토끼눈이 된듯한"},
                                    //행복+@
                                    {"빡치면서도 자유의 시간을 기다리고 있는", "예민 보스지만 행복한", "썩소를 머금은", "아주 행복한",
                                    "내심 기분 좋은 그런 날", "사랑하는 사람과 눈물나게 웃긴 영화를 보고 싶은", "신나고 싶은데 눈치 보이는"},
                                    //보통+@
                                    {"조금 화나짐나 참을만 한", "심술을 숨긴 보통날의", "살짝 겁먹은 고양이 같은", "내심 기분 좋은 그런 날",
                                    "아주 보통의", "걍 조금은 애잔한", "약간 심쿵한"},
                                    //슬픔+@
                                    {"조금 화나면서 서러운", "예민 보스가 생각나서 먼지가 되고 싶은", "무서워서 눈물나는",
                                    "사랑하는 사람과 눈물나게 웃긴 영화를 보고 싶은", "걍 조금 애잔한", "아주 슬픈", "어멋 눈물이 날 것만 같은"},
                                    //놀람+@
                                    {"어멋 부들부들, 어멋 부들부들 하는", "마냥 싫지만 좀 신기한", "토끼눈이 된듯한", "신나고 싶은데 눈치 보이는",
                                    "약간 심쿵한", "어멋 눈물이 날 것만 같은", "아주 놀란"}

        };
        Random random = new Random();
        int idxMainEmotion = (int)emotion[0][0];
        int ranIdxMainEmotion = random.nextInt(10);
        mainEmotionStatement = mainEmotion[idxMainEmotion][ranIdxMainEmotion];

        int idxSubEmotion1 = (int)emotion[0][1];
        int idxSubEmotion2 = (int)emotion[0][2];
        subEmotionStatement = subEmotion[idxSubEmotion1][idxSubEmotion2];

        analyzeStatement[0] = subEmotionStatement;
        analyzeStatement[1] = mainEmotionStatement;
//
//        for (int i=0; i<zeroCount; i++) {
//            if (zeroCount - i == 1) {
//                sAnalyzeStatement = sAnalyzeStatement + em[i] + ": " + Math.floor(emotion[1][i]) + "%\n";
//            } else {
//                sAnalyzeStatement = sAnalyzeStatement + em[i] + ": " + Math.floor(emotion[1][i]) + "%, \n";
//            }
//        }
        Log.i(TAG, analyzeStatement[0]+"/"+analyzeStatement[1]);
        return analyzeStatement;
    }
    // 감정 0~6 : 분노, 혐오, 공포, 행복 , 보통, 슬픔, 놀람

    void makeExpression ()
    {

    }
}
