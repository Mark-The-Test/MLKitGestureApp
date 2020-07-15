package com.stockman.mlkitgesturetalk;

import android.view.View;

import androidx.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.*;

public class MainActivityTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule =
            new ActivityTestRule<MainActivity>(MainActivity.class);
    private MainActivity mActivity = null;



    @Before
    public void setUp() throws Exception {
        mActivity= mActivityTestRule.getActivity();
    }
    @After
    public void tearDown() throws Exception {
        mActivity= null;
    }

    //this test is to check if the activity is launched, if the view by id
    // function works its a good assumption  that the app launches
    @Test
    public void testAppLaunch(){
        View view = mActivity.findViewById(R.id.word_icon);
        assertNotNull(view);
    }

    @Test
    public void onClickTests() {
        onView(withId(R.id.word_icon)).perform(click());

    }

}