package com.ugene.postdownload.app;

public class StringHelpers
{
    public static String declOfNum(int number, String[] titles)
    {
        Integer[] cases = new Integer[] { 2, 0, 1, 1, 1, 2 };
        int position;

        if (number % 100 > 4 && number % 100 < 20)
        {
            position = 2;
        }
        else
        {
            position = cases[Math.min(number % 10, 5)];
        }

        return number + " " + titles[position];
    }
}
