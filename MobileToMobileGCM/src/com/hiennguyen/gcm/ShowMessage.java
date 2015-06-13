package com.hiennguyen.gcm;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.hiennguyen.gcm.EmoticonsGridAdapter.KeyClickListener;

public class ShowMessage extends FragmentActivity implements KeyClickListener {

	ImageButton btnSend;
	final Context context = this;
	TableLayout tlMessage;
	Controller aController;
	String name, reply;
	String urlSendMessage = Config.SERVER_URL + "sendpush.php";
	String urlGetConversation = Config.SERVER_URL
			+ "get_conversation_reply.php";

	// ----------------------------EMO------------------------------
	private static final int NO_OF_EMOTICONS = 54;

	private View popUpView;

	private LinearLayout emoticonsCover;
	private PopupWindow popupWindow;

	private int keyboardHeight;
	private EditText etSmsContent;

	private LinearLayout parentLayout;

	private boolean isKeyBoardVisible;

	public static Bitmap[] emoticons;
	// ------------EMO--------------------
	String chatType = "";
	ArrayList<UserData> groupUserSelected = new ArrayList<UserData>();
	UserData senderData = new UserData();
	UserData receiverData = new UserData();
	String groupName = "";
	String groupId = "";
	SessionManager sm;

	// LocalBroadcastManager broadcaster;
	// public static final String IF_SHOW_MESSAGE = "NOTI_SHOW_MESSAGE";

	@SuppressLint("InflateParams")
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_message);
		sm = new SessionManager(getApplicationContext());
		tlMessage = (TableLayout) findViewById(R.id.tlMessage);
		// broadcaster = LocalBroadcastManager.getInstance(this);
		LocalBroadcastManager.getInstance(this).registerReceiver(
				(mReceiver_LoadConversation),
				new IntentFilter(GcmIntentService.INTENT_FILTER));

		Intent it = getIntent();
		senderData = sm.getUserDetails();
		chatType = it.getStringExtra(ListUserFragment.CHAT_TYPE);
		if (chatType.equals("One")) {
			receiverData = (UserData) it
					.getSerializableExtra(ListUserFragment.RECEIVER_DATA);
			loadConversation(false);
		}
		if (chatType.equals("Group")) {
			groupUserSelected = (ArrayList<UserData>) it
					.getSerializableExtra(ListUserFragment.LIST_RECEIVER_DATA);
			groupId = it.getStringExtra(MyNotificationFragment.GROUP_ID);
			groupName = it.getStringExtra(MyNotificationFragment.GROUP_NAME);
			if (groupId == null) {
				showDialogEnterGroupName();
			} else {
				loadConversation(true);
			}
		}
		aController = (Controller) getApplicationContext();
		btnSend = (ImageButton) findViewById(R.id.btnSend);
		btnSend.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (!aController.isConnectingToInternet()) {
					aController.showAppSendSMSOrEnableWifi(ShowMessage.this, etSmsContent
							.getText().toString().trim());
					return;
				} else {
					reply = etSmsContent.getText().toString().trim();
					if (reply.trim().length() < 1) {
						Toast.makeText(
								ShowMessage.this,
								getResources().getString(
										R.string.content_msg_blank),
								Toast.LENGTH_SHORT).show();
						return;
					}

					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("usernameSender",
							senderData.getName()));
					params.add(new BasicNameValuePair("RegIdSender", senderData
							.get_regId()));
					params.add(new BasicNameValuePair("IdSender", senderData
							.getID() + ""));
					params.add(new BasicNameValuePair("reply", reply));

					if (chatType.equals("Group")) {
						if (groupName.length() < 1) {
							showDialogEnterGroupName();
							return;
						}
						params.add(new BasicNameValuePair("regIdReceiver",
								"Group"));
						params.add(new BasicNameValuePair("GroupName",
								groupName));
						JSONArray jsonArrUserRegId = new JSONArray();
						JSONArray jsonArrUserId = new JSONArray();
						JSONArray jsonArrUsername = new JSONArray();
						for (int i = 0; i < groupUserSelected.size(); i++) {
							jsonArrUserRegId.put(groupUserSelected.get(i)
									.get_regId());
							jsonArrUserId.put(groupUserSelected.get(i).getID());
							jsonArrUsername.put(groupUserSelected.get(i)
									.getName());
						}
						params.add(new BasicNameValuePair("ListRegIdReceiver",
								jsonArrUserRegId.toString()));
						params.add(new BasicNameValuePair("ListIdReceiver",
								jsonArrUserId.toString()));
						params.add(new BasicNameValuePair(
								"ListUsernameReceiver", jsonArrUsername
										.toString()));
						if (groupId != null) {
							params.add(new BasicNameValuePair("GroupId",
									groupId));
						}
						new AsyncTaskSendMessage().execute(params);
					} else {
						params.add(new BasicNameValuePair("regIdReceiver",
								receiverData.get_regId()));
						params.add(new BasicNameValuePair("usernameReceiver",
								receiverData.getName()));
						params.add(new BasicNameValuePair("IdReceiver",
								receiverData.getID() + ""));
						new AsyncTaskSendMessage().execute(params);
					}
					// Intent intent = new Intent(ShowMessage.IF_SHOW_MESSAGE);
					// intent.putExtra("SenderId", senderData.getID());
					// broadcaster.sendBroadcast(intent);
					etSmsContent.setText("");
				}
			}
		});
		parentLayout = (LinearLayout) findViewById(R.id.parent_show_message);
		emoticonsCover = (LinearLayout) findViewById(R.id.footer_for_emoticons_show_message);

		popUpView = getLayoutInflater().inflate(R.layout.emoticons_popup, null);

		// Setting adapter for chat list
		// chats = new ArrayList<ConversationReply>();

		// Defining default height of keyboard which is equal to 230 dip
		final float popUpheight = getResources().getDimension(
				R.dimen.keyboard_height);
		changeKeyboardHeight((int) popUpheight);

		// Showing and Dismissing pop up on clicking emoticons button
		ImageView emoticonsButton = (ImageView) findViewById(R.id.emoticons_button);
		emoticonsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!popupWindow.isShowing()) {

					popupWindow.setHeight((int) (keyboardHeight));

					if (isKeyBoardVisible) {
						emoticonsCover.setVisibility(LinearLayout.GONE);
					} else {
						emoticonsCover.setVisibility(LinearLayout.VISIBLE);
					}
					popupWindow.showAtLocation(parentLayout, Gravity.BOTTOM, 0,
							0);

				} else {
					popupWindow.dismiss();
				}

			}
		});
		enableFooterView();
		readEmoticons();
		enablePopUpView();
		checkKeyboardHeight(parentLayout);

	}

	@SuppressLint("InflateParams")
	public void showDialogEnterGroupName() {
		LayoutInflater li = LayoutInflater.from(context);
		View promptsView = li.inflate(R.layout.enter_group_name, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);

		alertDialogBuilder.setView(promptsView);

		final EditText userInput = (EditText) promptsView
				.findViewById(R.id.editTextDialogUserInput);
		String allUserName = "";
		for (int i = 0; i < groupUserSelected.size(); i++) {
			if (i == groupUserSelected.size() - 1) {
				allUserName += groupUserSelected.get(i).getName() + ".";
			} else {
				allUserName += groupUserSelected.get(i).getName() + ", ";
			}
		}
		TextView tvSendToGroup = (TextView) promptsView
				.findViewById(R.id.tvSendToGroup);
		tvSendToGroup.setText(getResources().getString(R.string.send_to_tv)
				+ allUserName);
		alertDialogBuilder
				.setCancelable(false)
				.setPositiveButton(
						getResources().getString(R.string.ok_dialog),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Log.e("editTextDialogUserInput", userInput
										.getText().toString());
								groupName = userInput.getText().toString()
										.trim();
							}
						})
				.setNegativeButton(
						getResources().getString(R.string.cancel_dialog), null);

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

	@SuppressWarnings("unchecked")
	public void loadConversation(boolean loadGroup) {
		if (loadGroup) {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("group_id", groupId));
			new AsyncTaskLoadConversation().execute(params);

		} else {

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("us_id_s", senderData.getID()
					+ ""));
			params.add(new BasicNameValuePair("us_id_r", receiverData.getID()
					+ ""));
			new AsyncTaskLoadConversation().execute(params);
		}
	}

	// Reading all emoticons in local cache
	private void readEmoticons() {

		emoticons = new Bitmap[NO_OF_EMOTICONS];
		for (short i = 0; i < NO_OF_EMOTICONS; i++) {
			// emoticons[i] = getImage((i + 1) + ".png");
			emoticons[i] = getImage(String.format("%02d", i + 1) + ".png");
		}

	}

	// Enabling all content in footer i.e. post window
	private void enableFooterView() {

		etSmsContent = (EditText) findViewById(R.id.etSmsContent);
		etSmsContent.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (popupWindow.isShowing()) {

					popupWindow.dismiss();
				}

			}
		});
	}

	// Overriding onKeyDown for dismissing keyboard on key down
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (popupWindow.isShowing()) {
			popupWindow.dismiss();
			return false;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	// Checking keyboard height and keyboard visibility
	int previousHeightDiffrence = 0;

	private void checkKeyboardHeight(final View parentLayout) {

		parentLayout.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {

					@Override
					public void onGlobalLayout() {

						Rect r = new Rect();
						parentLayout.getWindowVisibleDisplayFrame(r);

						int screenHeight = parentLayout.getRootView()
								.getHeight();
						int heightDifference = screenHeight - (r.bottom);

						if (previousHeightDiffrence - heightDifference > 50) {
							popupWindow.dismiss();
						}

						previousHeightDiffrence = heightDifference;
						if (heightDifference > 100) {

							isKeyBoardVisible = true;
							changeKeyboardHeight(heightDifference);

						} else {

							isKeyBoardVisible = false;

						}

					}
				});

	}

	/**
	 * change height of emoticons keyboard according to height of actual
	 * keyboard
	 * 
	 * @param height
	 *            minimum height by which we can make sure actual keyboard is
	 *            open or not
	 */
	private void changeKeyboardHeight(int height) {

		if (height > 100) {
			keyboardHeight = height;
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, keyboardHeight);
			emoticonsCover.setLayoutParams(params);
		}

	}

	// Defining all components of emoticons keyboard
	private void enablePopUpView() {

		ViewPager pager = (ViewPager) popUpView
				.findViewById(R.id.emoticons_pager);
		pager.setOffscreenPageLimit(3);

		ArrayList<String> paths = new ArrayList<String>();

		for (short i = 1; i <= NO_OF_EMOTICONS; i++) {
			// paths.add(i + ".png");
			paths.add(String.format("%02d", i) + ".png");
		}

		EmoticonsPagerAdapter adapter = new EmoticonsPagerAdapter(
				ShowMessage.this, paths, this);
		pager.setAdapter(adapter);

		// Creating a pop window for emoticons keyboard
		popupWindow = new PopupWindow(popUpView, LayoutParams.MATCH_PARENT,
				(int) keyboardHeight, false);

		ImageButton ibBckspace = (ImageButton) popUpView
				.findViewById(R.id.ibBackspace);
		ibBckspace.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0,
						0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
				etSmsContent.dispatchKeyEvent(event);
			}
		});

		popupWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				emoticonsCover.setVisibility(LinearLayout.GONE);
			}
		});
	}

	// For loading smileys from assets
	private Bitmap getImage(String path) {
		AssetManager mngr = getAssets();
		InputStream in = null;
		try {
			in = mngr.open("emoticons/" + path);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Bitmap temp = BitmapFactory.decodeStream(in, null, null);
		return temp;
	}

	@Override
	public void keyClickedIndex(final String index) {
		StringTokenizer st = new StringTokenizer(index, ".");
		String tam = st.nextToken().toString();
		SpannableString ss = new SpannableString(index + "|");
		Drawable d = new BitmapDrawable(getResources(),
				emoticons[Integer.parseInt(tam) - 1]);
		d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
		ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
		ss.setSpan(span, 0, 7, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		int cursorPosition = etSmsContent.getSelectionStart();
		etSmsContent.getText().insert(cursorPosition, ss);
	}
	boolean receiveFromBroadcast=false;
	BroadcastReceiver mReceiver_LoadConversation = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			receiveFromBroadcast=true;
			boolean fromGroup = intent.getBooleanExtra(
					GcmIntentService.FROM_GROUP, false);
			if (fromGroup) {
				groupId = intent.getStringExtra(GcmIntentService.GROUP_ID);
				loadConversation(true);
			} else {
				loadConversation(false);
			}
		}
	};

	@Override
	protected void onStop() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				mReceiver_LoadConversation);
		super.onStop();
	}

	// --------------------------EMO-------------------

	@SuppressLint("RtlHardcoded")
	public void chuyenThongDiepTinNhan(String message, int User_id_fk,
			boolean fromGroup, String repTime) {
		String[] parts = message.split("\\|");
		TableRow tr1 = new TableRow(getApplicationContext());

		TableLayout.LayoutParams lpForTR = new TableLayout.LayoutParams(
				TableLayout.LayoutParams.MATCH_PARENT,
				TableLayout.LayoutParams.WRAP_CONTENT);
		lpForTR.setMargins(0, 0, 0, 20);
		tr1.setLayoutParams(lpForTR);

		LinearLayout linLayout = new LinearLayout(this);
		linLayout.setOrientation(LinearLayout.HORIZONTAL);
		linLayout.setLayoutParams(new TableRow.LayoutParams(
				TableRow.LayoutParams.MATCH_PARENT,
				TableRow.LayoutParams.WRAP_CONTENT, 1f));

		TextView tvAnyOne = new TextView(this);
		tvAnyOne.setLayoutParams(new TableRow.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		TextView tvReply = new TextView(this);
		tvReply.setLayoutParams(new TableRow.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		TextView tvMe = new TextView(this);
		tvMe.setLayoutParams(new TableRow.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		boolean fromMe = false;
		if (User_id_fk == senderData.getID()) {
			fromMe = true;
			tvMe.append(Html.fromHtml("<b>"
					+ getResources().getString(R.string.sender) + "</b>"));
			tvMe.setTextColor(Color.parseColor("#A901DB"));
			linLayout.addView(tvReply);
			linLayout.addView(tvMe);
			linLayout.setGravity(Gravity.RIGHT);
		} else {
			if (fromGroup) {
				for (int i = 0; i < groupUserSelected.size(); i++) {
					UserData userItem = groupUserSelected.get(i);
					if (userItem.getID() == User_id_fk) {
						tvAnyOne.append(Html.fromHtml("<b>"
								+ userItem.getName() + "</b>"));
					}
				}
			} else {
				tvAnyOne.append(Html.fromHtml("<b>" + receiverData.getName()
						+ "</b>"));
			}
			tvAnyOne.setTextColor(Color.parseColor("#0B0719"));
			linLayout.addView(tvAnyOne);
			linLayout.addView(tvReply);
			linLayout.setGravity(Gravity.LEFT);
		}
		for (int i = 0; i < parts.length; i++) {
			if (parts[i].contains(".png")) {
				String tenAnh = parts[i].substring(parts[i].length() - 6,
						parts[i].length() - 4);

				String truocTenAnh = parts[i].substring(0,
						parts[i].length() - 6);
				tvReply.append(Html.fromHtml("<b>" + truocTenAnh + "</b>"));

				SpannableString ss = new SpannableString(tenAnh + ".png");
				Drawable d = new BitmapDrawable(getResources(),
						emoticons[Integer.parseInt(tenAnh) - 1]);
				d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
				ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
				ss.setSpan(span, 0, 6, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

				tvReply.append(ss);
			} else {
				tvReply.append(Html.fromHtml("<b>" + parts[i] + "</b>"));
			}
			if (fromMe) {
				tvReply.setBackgroundResource(R.drawable.in_message_bg);
			} else {
				tvReply.setBackgroundResource(R.drawable.out_message_bg);
			}
		}
		 String time = "<br/><i>" + repTime + "</i>";
		 tvReply.append(Html.fromHtml(time));
		fromMe = false;

		tr1.addView(linLayout);
		tlMessage.addView(tr1, tlMessage.getChildCount());
		// Enqueue the scrolling to happen after the new row has been layout
		((ScrollView) findViewById(R.id.svTableLayoutMessage))
				.post(new Runnable() {
					public void run() {
						((ScrollView) findViewById(R.id.svTableLayoutMessage))
								.fullScroll(View.FOCUS_DOWN);
					}

				});
	}

	public class AsyncTaskSendMessage extends
			AsyncTask<List<NameValuePair>, Void, JSONObject> {
		private ProgressDialog Dialog = new ProgressDialog(ShowMessage.this);

		protected void onPreExecute() {
			Dialog.setMessage(getResources().getString(R.string.wait_sending));
			Dialog.show();
		}

		@Override
		protected JSONObject doInBackground(List<NameValuePair>... params) {
			JSONParser jsonPaster = new JSONParser();
			JSONObject json = null;
			try {
				json = jsonPaster.performPostCall(urlSendMessage, params[0],
						true);
				return json;
			} catch (Exception e) {
				Log.e("AsyncTaskSendMessage", e.toString());
				return null;
			}
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			Dialog.dismiss();
			if (result != null) {
				Toast.makeText(getBaseContext(),
						"Message sent." + result.toString(), Toast.LENGTH_LONG)
						.show();
			} else {
				Toast.makeText(getBaseContext(),
						getResources().getString(R.string.change_pass_err),
						Toast.LENGTH_LONG).show();
			}
		}
	}

	ArrayList<ConversationReply> arrConversationReps = new ArrayList<ConversationReply>();

	public class AsyncTaskLoadConversation extends
			AsyncTask<List<NameValuePair>, Void, JSONObject> {

		@Override
		protected JSONObject doInBackground(List<NameValuePair>... params) {
			JSONParser jsonPaster = new JSONParser();
			JSONObject json;
			try {
				json = jsonPaster.performPostCall(urlGetConversation,
						params[0], true);
				return json;
			} catch (Exception e) {
				Log.e("AsyncTaskLoadConversationHere doInBackground", e.toString());
				return null;
			}
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			if (result != null) {

				arrConversationReps = parseJson(result);
				tlMessage.removeAllViews();
				if (arrConversationReps != null) {
					for (int i = 0; i < arrConversationReps.size(); i++) {
						ConversationReply temp = arrConversationReps.get(i);
						chuyenThongDiepTinNhan(temp.getReply(),
								temp.getUser_id_fk(), temp.isFromGroup(),
								temp.getTime());
					}
				}
			} else {
				Toast.makeText(ShowMessage.this,
						getResources().getString(R.string.change_pass_err),
						Toast.LENGTH_LONG).show();
			}
		}

		public ArrayList<ConversationReply> parseJson(JSONObject pResult) {
			ArrayList<ConversationReply> arrCVParsed = new ArrayList<ConversationReply>();
			try {
				JSONArray conversationReplyInJson = pResult
						.optJSONArray("ConversationReply");
				for (int i = 0; i < conversationReplyInJson.length(); i++) {
					JSONObject user = conversationReplyInJson.getJSONObject(i);
					int userIdFk = user.optInt("user_id_fk");
					String reply = user.optString("reply").toString();
					int fromGroup = user.optInt("from_group");
					if (reply.equals("null") && userIdFk == 0) {
						return null;
					}
					String repTime = user.optString("time");
					ConversationReply repData = new ConversationReply();
					repData.setUser_id_fk(userIdFk);
					repData.setReply(reply);
					repData.setTime(repTime);
					if (fromGroup == 1) {
						repData.setFromGroup(true);
					} else {
						repData.setFromGroup(false);
					}
					arrCVParsed.add(repData);
				}
				return arrCVParsed;

			} catch (JSONException e) {
				Log.e("AsyncTaskGetConversationReply: onPostExecute hehe",
						e.toString());
				return null;
			}
		}
	}
}
