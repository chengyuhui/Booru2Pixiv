package moe.reimu.booru2pixiv;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ShareHandlerActivity extends Activity{
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

		final Uri data = getIntent().getData();

		OkHttpClient client = new OkHttpClient();

		Request request = new Request.Builder()
				.url(cleanupUri(data).buildUpon()
						.appendQueryParameter("login",
								sharedPref.getString(SettingsActivity.KEY_PREF_LOGIN_NAME, ""))
						.appendQueryParameter("api_key",
								sharedPref.getString(SettingsActivity.KEY_PREF_API_KEY, ""))
						.build().toString()
				).build();

		Log.d("B2P", request.url().toString());
		client.newCall(request).enqueue(new Callback(){

			@Override
			public void onFailure(Call call, IOException e) {
				showToast("API Request Failed");
				finish();
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				String jsonData = response.body().string();
				String pid = null;
				Log.d("B2P", jsonData);
				try	{
					JSONObject Jobject = new JSONObject(jsonData);
					if (!Jobject.getString("tag_string").contains("bad_id")) {
						pid = String.valueOf(Jobject.getInt("pixiv_id"));
					}
				} catch(Exception e) {
					Log.e("B2P", "JSON Parse failed");
				}

				if (pid == null){
					showToast("Pixiv ID not found / bad_id detected.");
					openLinkBrowser(data);
					finish();
					return;
				}


				Log.d("B2P", "PID=".concat(pid));
				showToast("Redirecting to Pixiv: ".concat(pid));
				openLinkApp(Uri.parse("http://www.pixiv.net/member_illust.php?mode=medium&illust_id=".concat(pid)));
				finish();
			}
		});
	}

	private Uri cleanupUri(Uri url){
		String ret = url.buildUpon().clearQuery().build().toString().concat(".json");
		return Uri.parse(ret);
	}

	private void openLinkBrowser(Uri url){
		Intent browserIntent = new Intent(Intent.ACTION_VIEW);
		browserIntent.setDataAndType(url, "text/html");
		browserIntent.addCategory(Intent.CATEGORY_BROWSABLE);
		startActivity(browserIntent);
	}

	private void openLinkApp(Uri url){
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, url);
		startActivity(browserIntent);
	}

	private void showToast(final String text) {
		this.runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
			}
		});
	}

}
