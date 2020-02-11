package com.lemberg.connfa.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.lemberg.connfa.R;

public class WebViewUtils {

	public static String getHtml(AppCompatActivity activity, String body) {
		String css = activity.getString(R.string.css);
		return String.format("<html><header>%s</header><body>%s</body></html>", css, body);
	}

	public static void openUrl(@NonNull Context context, @NonNull String url) {
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			context.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(context, R.string.no_default_browser_found, Toast.LENGTH_SHORT).show();
		}
	}
}
