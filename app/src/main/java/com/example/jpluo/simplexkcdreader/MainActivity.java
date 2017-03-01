package com.example.jpluo.simplexkcdreader;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class MainActivity extends AppCompatActivity {

    private PhotoView mPhotoView;
    private ImageButton mNextButton;
    private ImageButton mPrevButton;
    private static final String XKCD_FRONTPAGE = "https://m.xkcd.com/";
    static int LAST_PAGE = -1;
    private String mXkcd_url = XKCD_FRONTPAGE;
    private String mImageURL;
    private EditText mEditText;
    private int mImageCounter = 0;
    private Boolean mChangeSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setup the nextButton, editText and PhotoView
        mPhotoView = (PhotoView) findViewById(R.id.photoView);
        mEditText = (EditText) findViewById(R.id.editText);
        mNextButton = (ImageButton) findViewById(R.id.nextButton);

        //Get the starting image and hide nextButton.
        updateImage();
        Picasso.with(getBaseContext()).load(mImageURL).into(mPhotoView);
        LAST_PAGE = mImageCounter;
        mNextButton.setVisibility(View.INVISIBLE);

        //setup listener for editText enter key.
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event){
                mXkcd_url = XKCD_FRONTPAGE + mEditText.getText().toString();
                updateImage();
                return false;
            }
        });

        //setup listener for the photoview swipe.
        mPhotoView.setOnSingleFlingListener(new PhotoViewAttacher.OnSingleFlingListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float x1 = e1.getX();
                float x2 = e2.getX();

                if ((x1 < x2) && (x2-x1 > 35.00)){
                    on_prevButton_click(findViewById(R.id.prevButton));
                } else if ((x2 < x1) && (x1 - x2 > 35.00)){
                    on_nextButton_click(findViewById(R.id.nextButton));
                }
                return false;
            }
        });
    }


    public void updateImage(){
        try {
            //Go to asynctask for getting image url, and wait for up to 5 seconds
            new GetImageUrl().execute().get(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        if (mChangeSuccess) {
            //if getting image was successful, load the image onto Image view.
            Picasso.with(getBaseContext()).load(mImageURL).into(mPhotoView);
            //and set Edit text to match current page.
            mEditText.setText(Integer.toString(mImageCounter));
        }

        //if on newest page, hide the next button.
        if (mImageCounter == LAST_PAGE) {
            mNextButton.setVisibility(View.INVISIBLE);
        } else {
            mNextButton.setVisibility(View.VISIBLE);
        }
    }


    public void on_prevButton_click(View view) {
        //On previous button click, update the url for the next page.
        mXkcd_url = XKCD_FRONTPAGE + Integer.toString(--mImageCounter);
        updateImage();

        //if getting the url failed, don't change the image counter.
        if (!mChangeSuccess) mImageCounter++;
    }

    public void on_nextButton_click(View view) {
        //On next button click, update the url for the next page.
        mXkcd_url = XKCD_FRONTPAGE + Integer.toString(++mImageCounter);
        updateImage();

        //if getting the url failed, don't change the image counter.
        if (!mChangeSuccess) mImageCounter--;
    }

    private class GetImageUrl extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                //Parse the HTML for image's url
                Document doc = Jsoup.connect(mXkcd_url).get();
                Element image = doc.select("img").first();
                mImageURL = image.absUrl("src");
                //Now the image url is something like this: "/1805/". We must remove the first char
                String imageNumberNoFirst = doc.select("a").get(1).attr("href").substring(1);
                //and the last char.
                mImageCounter = Integer.parseInt(imageNumberNoFirst.substring(0, imageNumberNoFirst.length()-1)) + 1;
                //The image url fetching was a success.
                mChangeSuccess = true;
            } catch (IOException e) {
                //Couldn't fetch image url. No internet or incorrect page number.
                //TODO: print error to user (via toast?)
                e.printStackTrace();
                mChangeSuccess = false;
            }
            return null;
        }
    }

}

