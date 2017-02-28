package com.example.jpluo.simplexkcdreader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.annotation.IntegerRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private static String XKCD_FRONTPAGE = "https://m.xkcd.com/";
    private String xkcd_Url = XKCD_FRONTPAGE;
    private String imageURL;
    private EditText editText;
    private int imageCounter = 0;
    private Boolean changeSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.imageView);
        editText = (EditText) findViewById(R.id.editText);
        updateImage();
        Picasso.with(getBaseContext()).load(imageURL).into(imageView);
    }

    public void updateImage(){
        try {
            new GetImageUrl().execute().get(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        if (changeSuccess) {
            Picasso.with(getBaseContext()).load(imageURL).into(imageView);
            editText.setText(Integer.toString(imageCounter));
        }
    }

    public void on_prevButton_click(View view) {
        xkcd_Url = XKCD_FRONTPAGE + Integer.toString(--imageCounter);
        updateImage();

        if (!changeSuccess) imageCounter++;
    }

    public void on_nextButton_click(View view) {
        xkcd_Url = XKCD_FRONTPAGE + Integer.toString(++imageCounter);
        updateImage();

        if (!changeSuccess) imageCounter--;
    }

    private class GetImageUrl extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Document doc = Jsoup.connect(xkcd_Url).get();
                Element image = doc.select("img").first();
                imageURL = image.absUrl("src");
                String imageNumberNoFirst = doc.select("a").get(1).attr("href").substring(1);
                imageCounter = Integer.parseInt(imageNumberNoFirst.substring(0, imageNumberNoFirst.length()-1)) + 1;
                Log.d("imageNum", Integer.toString(imageCounter));
                Log.d("URL", imageURL);
                changeSuccess = true;
            } catch (IOException e) {
                e.printStackTrace();
                changeSuccess = false;
            }
            return null;
        }
    }
}
