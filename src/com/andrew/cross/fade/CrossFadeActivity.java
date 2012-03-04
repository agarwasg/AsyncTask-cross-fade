package com.andrew.cross.fade;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class CrossFadeActivity extends Activity implements OnClickListener {

	private LinearLayout mMain;
	private ImageView mCrossFade;
	private Drawable mDrawable;

	private String[] mArtists;
	private String mArtistImageURL;
	private final Random mRandom = new Random();
	private Bitmap mBitmap;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mMain = (LinearLayout) findViewById(R.id.main);
		mCrossFade = (ImageView) findViewById(R.id.crossFade);

		mCrossFade.setOnClickListener(this);

		new crossFade().execute();

	}

	@Override
	public void onClick(View v) {
		new crossFade().execute();
	}

	// This helps fit the images nicely
	static void setArtistBackground(View v, Bitmap bm) {

		if (bm == null) {
			v.setBackgroundResource(0);
			return;
		}

		int vwidth = v.getWidth();
		int vheight = v.getHeight();
		int bwidth = bm.getWidth();
		int bheight = bm.getHeight();
		float scalex = (float) vwidth / bwidth;
		float scaley = (float) vheight / bheight;
		float scale = Math.max(scalex, scaley) * 1.0f;

		Bitmap.Config config = Bitmap.Config.ARGB_8888;
		Bitmap bg = Bitmap.createBitmap(vwidth, vheight, config);
		Canvas c = new Canvas(bg);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		ColorMatrix darkmatrix = new ColorMatrix();
		darkmatrix.setScale(.5f, .5f, .5f, 1.0f);
		ColorFilter filter = new ColorMatrixColorFilter(darkmatrix);
		paint.setColorFilter(filter);
		Matrix matrix = new Matrix();
		matrix.setTranslate(-bwidth / 2, -bheight / 2);
		matrix.postScale(scale, scale);
		matrix.postTranslate(vwidth / 2, vheight / 2);
		c.drawBitmap(bm, matrix, paint);
		v.setBackgroundDrawable(new BitmapDrawable(bg));
	}

	// Convert URL to Bitmap
	public static Bitmap getBitmapFromURL(String src) {
		try {
			URL url = new URL(src);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap myBitmap = BitmapFactory.decodeStream(input);
			return myBitmap;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public class crossFade extends AsyncTask<String, Integer, Bitmap> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mCrossFade.startAnimation(AnimationUtils.loadAnimation(
					getBaseContext(), R.anim.fade_out));
			// This is the trick
			mDrawable = mCrossFade.getBackground();
			mMain.setBackgroundDrawable(mDrawable);
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			mArtists = getResources().getStringArray(R.array.artists);
			mArtistImageURL = mArtists[mRandom.nextInt(mArtists.length)];
			mBitmap = getBitmapFromURL(mArtistImageURL);
			return mBitmap;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			setArtistBackground(mCrossFade, result);
			mCrossFade.startAnimation(AnimationUtils.loadAnimation(
					getBaseContext(), R.anim.fade_in));
		}
	}
}