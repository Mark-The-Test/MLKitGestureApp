package com.stockman.mlkitgesturetalk;

import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class MainActivityTest {
private int mPositionTest1,mPositionTest2,mPositionTest3;
private ArrayList<MyModel> mArrayListTest;

    @Before
    public void setUp() throws Exception {

        Integer[] icons = {0,1,2,3,4};
        //String ArrayFor words
        String[] words = {"Help", "Toilet", "No", "Yes", "Food"};
        //setupArrayList
        mArrayListTest= new ArrayList<>();
        for (int i = 0; i < icons.length; i++) {
            MyModel model = new MyModel(icons[i], words[i]);
            mArrayListTest.add(model);
        }
        mPositionTest1=0;
        mPositionTest2=4;
        mPositionTest3=2;

    }

    @After
    public void tearDown() throws Exception {
        mArrayListTest = null;


    }
    @Test
    public void moveRight() {
        if (mPositionTest1 == (mArrayListTest.size() - 1)) {
            mPositionTest1 = 0;
        } else {
            mPositionTest1 = mPositionTest1 + 1;
        }
        assertEquals(1,mPositionTest1);

        if (mPositionTest2 == (mArrayListTest.size() - 1)) {
            mPositionTest2 = 0;
        } else {
            mPositionTest2 = mPositionTest2 + 1;
        }
        assertEquals(0,mPositionTest2);

        if (mPositionTest3 == (mArrayListTest.size() - 1)) {
            mPositionTest3 = 0;
        } else {
            mPositionTest3 = mPositionTest3 + 1;
        }
        assertEquals(3,mPositionTest3);
    }

    @Test
    public void moveLeft() {
        if (mPositionTest1 == 0) {
            mPositionTest1 = mArrayListTest.size() - 1;
        } else {
            mPositionTest1 = mPositionTest1 - 1;
        }
        assertEquals(4,mPositionTest1);

        if (mPositionTest2 == 0) {
            mPositionTest2 = mArrayListTest.size() - 1;
        } else {
            mPositionTest2 = mPositionTest2 - 1;
        }
        assertEquals(3,mPositionTest2);

        if (mPositionTest3 == 0) {
            mPositionTest3 = mArrayListTest.size() - 1;
        } else {
            mPositionTest3 = mPositionTest3 - 1;
        }
        assertEquals(1,mPositionTest3);

    }

    @Test
    public void playWord() {
        //no need to test all preexisting apis etc????
    }
}