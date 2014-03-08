package com.example.meikobb.manager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;

import com.example.meikobb.activity.SettingActivity;
import com.example.meikobb.model.BBItemBody;
import com.example.meikobb.model.BBItemHead;
import com.example.meikobb.sqlite.DatabaseHelper;

public class BBManager {
	static BBHandler sBBHandler;
	static DatabaseHelper sDatabaseHelper;
	
	/* コンストラクタ */
	public BBManager(Context context) {
		initialize(context);
	}
	
	
	/**
	 * BBManager の初期化
	 * @param context コンテキスト
	 */
	public static void initialize(Context context) {
		sBBHandler = new BBHandler();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String authID = prefs.getString(SettingActivity.PREF_KEY_AUTH_ID, "");
		String authPW = prefs.getString(SettingActivity.PREF_KEY_AUTH_PW, "");
		sBBHandler.initialize(authID, authPW);

		sDatabaseHelper = DatabaseHelper.getInstance(context);
	}
	
	
	/**
	 * DB 内の記事一覧を取得
	 * @param filter concat(記事タイトル, 著者) LIKE %filter%
	 * @param orderBy 出力する順番をSQL文で指定
	 * @param limit 出力数の制限をSQL文で指定
	 * @return DB から取得した記事のヘッダ情報のリスト
	 */
	public static List<BBItemHead> getHeads(String filter, String orderBy, String limit) {
		List<BBItemHead> list = new ArrayList<BBItemHead>();

		Cursor cursor = sDatabaseHelper.getReadableDatabase().query(
				DatabaseHelper.BBItemHead_TABLE_NAME,
				null,
				"lower(" + DatabaseHelper.BBItemHead_COL_TITLE + ")||lower("
						+ DatabaseHelper.BBItemHead_COL_AUTHOR + ")"
						+ " LIKE ?", new String[] { "%" + filter + "%" }, null,
				null, orderBy, limit);
		try {
			while( cursor.moveToNext() ) {
				list.add(DatabaseHelper.BBItemHead_cursorToObject(cursor));
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} finally {
			cursor.close();
		}
		
		return list;
	}
	
	
	/**
	 * DB 内の記事一覧を取得
	 * @param orderBy 出力する順番をSQL文で指定
	 * @param limit 出力数の制限をSQL文で指定
	 * @return DB から取得した記事のヘッダ情報のリスト
	 */
	public static List<BBItemHead> getHeads(String orderBy, String limit) {
		return getHeads("", orderBy, limit);
	}
	
	
	/**
	 * DB 内の記事一覧を取得
	 * @param limit 出力数の制限をSQL文で指定
	 * @return DB から取得した記事のヘッダ情報のリスト
	 */
	public static List<BBItemHead> getHeads(String limit) {
		return getHeads("", sDatabaseHelper.BBItemHead_COL_ID_DATE, limit);
	}
	
	
	/**
	 * WEB 上から記事一覧を再取得
	 * @return 増えた記事の数, 取得に失敗した場合は (-1)
	 */
	public static int reloadHeads() {
		int count = sDatabaseHelper.BBItemHead_getCounts();
		
		List<BBItemHead> obtained = sBBHandler.getAllBBItems();
		if( obtained == null ) {
			// 取得に失敗
			return (-1);
		}
		
		Iterator<BBItemHead> it = obtained.iterator();
		
		while (it.hasNext()) {
			BBItemHead item = it.next();
			if (sDatabaseHelper.BBItemHead_findById(item.getIdDate(),
					item.getIdIndex()) == null) {
				sDatabaseHelper.BBItemHead_insert(item);
			} else {
				sDatabaseHelper.BBItemHead_update(item);
			}
		}

		return (sDatabaseHelper.BBItemHead_getCounts() - count);
	}
	
	
	/**
	 * 記事の内容を取得
	 * @param itemHead 記事のヘッダ情報
	 * @return WEB から取得した記事の内容
	 */
	public static BBItemBody getBody(BBItemHead itemHead) {
		BBItemBody itemBody = sDatabaseHelper.BBItemBody_findById(itemHead.getIdDate(), itemHead.getIdIndex());
		
		// DB 未挿入の場合は新規インスタンスを作成
		if( itemBody == null ) {
			itemBody = new BBItemBody(itemHead.getIdDate(), itemHead.getIdIndex(), null, false);
			sDatabaseHelper.BBItemBody_insert(itemBody);
		}
		
		// 内容未取得の場合は WEB から取得
		if( !itemBody.getIsLoaded() ) {
			itemBody = sBBHandler.getBBItemBody(itemHead);
			sDatabaseHelper.BBItemBody_update(itemBody);
		}
		
		return itemBody;
	}
	
	
	/**
	 * 記事の内容の再取得
	 * @param itemHead 記事のヘッダ情報
	 * @return WEB から取得した記事の内容
	 */
	public static BBItemBody reloadBody(BBItemHead itemHead) {
		BBItemBody itemBody = sDatabaseHelper.BBItemBody_findById(itemHead.getIdDate(), itemHead.getIdIndex());

		// DB 未挿入の場合は新規インスタンスを作成
		if( itemBody == null ) {
			itemBody = new BBItemBody(itemHead.getIdDate(), itemHead.getIdIndex(), null, false);
			sDatabaseHelper.BBItemBody_insert(itemBody);
		}

		// 内容未取得の場合は WEB から取得
		itemBody = sBBHandler.getBBItemBody(itemHead);
		sDatabaseHelper.BBItemBody_update(itemBody);
		
		return itemBody;
	}
	
	
	/**
	 * 記事の内容が取得済みか確認
	 * @param itemHead 記事のヘッダ情報
	 * @return 記事の内容が取得済みの場合 true, それ以外の場合 false
	 */
	public static boolean isBodyLoaded(BBItemHead itemHead) {
		BBItemBody itemBody = sDatabaseHelper.BBItemBody_findById(itemHead.getIdDate(), itemHead.getIdIndex());
		return ((itemBody != null) ? itemBody.getIsLoaded() : false);
	}
	
	
}
