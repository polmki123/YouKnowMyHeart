package com.example.camera3;

import android.util.Log;

public class emotionExpression {

    final private static String TAG = "FOOD";

    double[][] emotion = new double[2][7];
    String sAnalyzeStatement = " ";
    //생성자
    emotionExpression(){};

    public void setEmotion(int index, double emo)
    {
        emotion[0][index] = index;
        emotion[1][index] = emo;
    }

    public String getTopEmotion()
    {
        //내림 차순 정렬이 안되서 수정
        double [][] tmpEmotion = new double[2][7];
        Log.i(TAG,"길이: "+ tmpEmotion[1].length);

        for(int i= 0;i<2;i++)
        {
            for(int j=0;j<7;j++)
            {
                tmpEmotion[i][j] = emotion[i][j];
            }
        }

        for(int i=0; i<tmpEmotion[1].length;i++)
        {
            tmpEmotion[1][i] = Math.floor(tmpEmotion[1][i]);
        }
        for (int i = 0 ; i<tmpEmotion[1].length; i++)
        {
            Log.i(TAG, "정렬전 "+tmpEmotion[1][i]);
            Log.i(TAG, "정렬전2 "+tmpEmotion[0][i]);
        }

        for(int i=0;i<tmpEmotion[1].length;i++)
        {
            for(int j=i+1; j<tmpEmotion[1].length;j++)
            {
                if(tmpEmotion[1][i] < Math.floor(tmpEmotion[1][j]))
                {
                    double tmp = tmpEmotion[1][i];
                    tmpEmotion[1][i] = tmpEmotion[1][j];
                    tmpEmotion[1][j] = tmp;

                    double idx = tmpEmotion[0][i];
                    tmpEmotion[0][i] = tmpEmotion[0][j];
                    tmpEmotion[0][j] = idx;
                }
            }
        }

        for (int i = 0 ; i<tmpEmotion[1].length; i++)
        {
            Log.i(TAG, "정렬결과 "+tmpEmotion[1][i]);
            Log.i(TAG, "정렬결과2 "+tmpEmotion[0][i]);
        }
        // 0 값 채크
        int zeroCount = 0;
        for(int i=0;i<tmpEmotion[1].length;i++)
        {
            if(tmpEmotion[1][i] <= 0.0){ zeroCount++; }
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
                Log.i(TAG, "감정"+i+tmpEmotion[1][i]);
                if(tmpEmotion[0][i] == 0) { em[i] = "분노"; }
                else if(tmpEmotion[0][i] == 1){ em[i] = "혐오";}
                else if(tmpEmotion[0][i] == 2){ em[i] = "공포";}
                else if(tmpEmotion[0][i] == 3){ em[i] = "행복";}
                else if(tmpEmotion[0][i] == 4){ em[i] = "보통";}
                else if(tmpEmotion[0][i] == 5){ em[i] = "슬픔";}
                else { em[i] = "놀람"; }
            }
        }
//
//        sAnalyzeStatement = "현재 당신의 감정은 ";
//        sAnalyzeStatement.concat(em[0]+": ");
//        sAnalyzeStatement.concat(Double.toString(emotion[1][0]*100)+"%, ");
//        sAnalyzeStatement.concat(em[1]+": ");
//        sAnalyzeStatement.concat(Double.toString(emotion[1][1]*100)+"%, ");
//        sAnalyzeStatement.concat(em[2]+": ");
//        sAnalyzeStatement.concat(Double.toString(emotion[1][2]*100)+"% ");
//        sAnalyzeStatement.concat("입니다.");


        sAnalyzeStatement = "현재 당신의 감정은 \n"
                            +em[0]+": "+Math.floor(tmpEmotion[1][0])+"%, \n"
                            +em[1]+": "+Math.floor(tmpEmotion[1][1])+"%, \n"
                            +em[2]+": "+Math.floor(tmpEmotion[1][2])+"% \n"
                            +"입니다.\n";
        Log.i(TAG, sAnalyzeStatement);
        return sAnalyzeStatement;
    }
    // 감정 0~6 : 분노, 혐오, 공포, 행복 , 보통, 슬픔, 놀람

    void makeExpression ()
    {

    }
}
