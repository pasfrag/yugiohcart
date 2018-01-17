package com.example.pasca.yugiohcart;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class MySQLiteHandler extends SQLiteOpenHelper {
	/*This class is used for all the database operations*/

	private Context context;

	// Database Version
	private static final int DATABASE_VERSION = 2;

	// Database Name
	private static final String DATABASE_NAME = "yugioh.db";

	// Cart Table Name
	public static final String TABLE_CART = "cart";

	// Cart Table Columns
	private static final String COLUMN_ID = "id";
	private static final String COLUMN_CARD_TITLE = "title";
	private static final String COLUMN_CARD_QUANTITY = "quantity";
	private static final String COLUMN_CARD_TYPE = "type";
	private static final String COLUMN_CARD_CONDITION = "condition";
	private static final String COLUMN_CARD_RARITY = "rarity";
	private static final String COLUMN_CARD_PRICE = "price";
	private static final String COLUMN_CARD_CURRENCY = "currency";

	//Table cart constructor
	private String CREATE_CART_TABLE = "CREATE TABLE " + TABLE_CART + " (" + COLUMN_ID +
			" INTEGER PRIMARY KEY, " + COLUMN_CARD_TITLE + " TEXT, " + COLUMN_CARD_QUANTITY +
			" INTEGER, " + COLUMN_CARD_TYPE + " TEXT, " + COLUMN_CARD_CONDITION +
			" TEXT, "  + COLUMN_CARD_RARITY + " TEXT, "  + COLUMN_CARD_PRICE + " REAL, "  +
			COLUMN_CARD_CURRENCY + " TEXT" +")";

	//Cards Table Name
	static final String TABLE_CARDS = "cards";

	//Cards Table Columns
	static final String COLUMN_TITLE = "title";

	//Table cards constructor
	private String CREATE_CARDS = "CREATE TABLE " + TABLE_CARDS + " (" + COLUMN_CARD_TITLE +
			" TEXT)";

	//Table collection columns
	public final String TABLE_COLLECTION = "collection";

	//Table collection constructor
	private String CREATE_COLLECTION_TABLE = "CREATE TABLE " + TABLE_COLLECTION + " (" + COLUMN_ID +
			" INTEGER PRIMARY KEY, " + COLUMN_CARD_TITLE + " TEXT, " + COLUMN_CARD_QUANTITY +
			" INTEGER, " + COLUMN_CARD_TYPE + " TEXT, " + COLUMN_CARD_CONDITION +
			" TEXT, "  + COLUMN_CARD_RARITY + " TEXT, "  + COLUMN_CARD_PRICE + " REAL, "  +
			COLUMN_CARD_CURRENCY + " TEXT" +")";

	//Class constructor
	public MySQLiteHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL(CREATE_CART_TABLE);
		db.execSQL(CREATE_CARDS);
		db.execSQL(CREATE_COLLECTION_TABLE);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CARDS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_COLLECTION);

		onCreate(db);

	}

	//Methods for cart and collection tables
	public void addACard(Card card , String tableName){

		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();

		values.put(COLUMN_CARD_TITLE, card.getTitle());
		values.put(COLUMN_CARD_QUANTITY, card.getQuantity());
		values.put(COLUMN_CARD_TYPE, card.getType());
		values.put(COLUMN_CARD_CONDITION, card.getCondition());
		values.put(COLUMN_CARD_RARITY, card.getRarity());
		values.put(COLUMN_CARD_PRICE, card.getPrice());
		values.put(COLUMN_CARD_CURRENCY, card.getCurrency());

		database.insert(tableName, null, values);

		database.close();

	}

	public Card getCard(int id , String tableName){

		SQLiteDatabase database = this.getReadableDatabase();
		Cursor cursor = database.query(tableName, new String[]{COLUMN_ID, COLUMN_CARD_TITLE,
				COLUMN_CARD_QUANTITY, COLUMN_CARD_TYPE, COLUMN_CARD_CONDITION, COLUMN_CARD_RARITY,
				COLUMN_CARD_PRICE, COLUMN_CARD_CURRENCY }, COLUMN_ID + "=?",
				new String[]{String.valueOf(id)},null, null, null);

		if (cursor != null){
			cursor.moveToFirst();
		}else {
			return null;
		}

		Card card = new Card();

		card.setId(cursor.getInt(0));
		card.setTitle(cursor.getString(1));
		card.setQuantity(cursor.getInt(2));
		card.setType(cursor.getString(3));
		card.setCondition(cursor.getString(4));
		card.setRarity(cursor.getString(5));
		card.setPrice(cursor.getDouble(6));
		card.setCurrency(cursor.getString(7));

		cursor.close();

		database.close();

		return card;
	}

	public List<Card> getAllCards(String tableName){
		List<Card> cardList = new ArrayList<>();

		SQLiteDatabase database = this.getReadableDatabase();
		Cursor cursor = database.rawQuery("SELECT * FROM " + tableName,null);

		if (cursor.moveToFirst()){
			do {

				Card card = new Card();

				card.setId(cursor.getInt(0));
				card.setTitle(cursor.getString(1));
				card.setQuantity(cursor.getInt(2));
				card.setType(cursor.getString(3));
				card.setCondition(cursor.getString(4));
				card.setRarity(cursor.getString(5));
				card.setPrice(cursor.getDouble(6));
				card.setCurrency(cursor.getString(7));

				cardList.add(card);

			}while (cursor.moveToNext());
		}

		cursor.close();

		return cardList;

	}

	public int updateCard(Card card, String tableName){
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();

		values.put(COLUMN_CARD_TITLE, card.getTitle());
		values.put(COLUMN_CARD_QUANTITY, card.getQuantity());
		values.put(COLUMN_CARD_TYPE, card.getType());
		values.put(COLUMN_CARD_CONDITION, card.getCondition());
		values.put(COLUMN_CARD_RARITY, card.getRarity());
		values.put(COLUMN_CARD_PRICE, card.getPrice());
		values.put(COLUMN_CARD_CURRENCY, card.getCurrency());

		int retValue = database.update(tableName, values, COLUMN_ID + " = ? ", new String[]{String.valueOf(card.getId())});
		database.close();
		return retValue;
	}

	public void deleteCard(Card card, String tableName){

		SQLiteDatabase database = this.getWritableDatabase();

		database.delete(tableName, COLUMN_ID + " = ? ", new String[]{String.valueOf(card.getId())});

		database.close();

	}

	public void deleteAll(String tableName){

		SQLiteDatabase database = this.getWritableDatabase();
		database.delete(tableName, null, null);
		database.close();
	}

	public int getTitleCount(String tableName){

		SQLiteDatabase database = this.getReadableDatabase();
		Cursor cursor = database.rawQuery("SELECT * FROM " + tableName, null);

		int count = cursor.getCount();

		cursor.close();
		database.close();

		return  count;

	}

	public int getOrderCount(String tableName){

		SQLiteDatabase database = this.getReadableDatabase();
		Cursor cursor = database.rawQuery("SELECT * FROM " + tableName, null);

		int count = 0;

		if(cursor.moveToFirst()){

			do {
				count += cursor.getInt(2);
			}while(cursor.moveToNext());

		}

		cursor.close();
		database.close();

		return  count;

	}

	//............................................................................................//
	//Methods for card table
	public void addCardName(String card){

		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();

		values.put(COLUMN_TITLE, card);
		database.insert(TABLE_CARDS, null, values);
		database.close();

	}

	public void deleteAllCards(){

		SQLiteDatabase database = this.getWritableDatabase();
		database.delete(TABLE_CARDS, null, null);
		database.close();

	}

	public List<String> getAllCardNames(){

		List<String> cardNames = new ArrayList<>();
		SQLiteDatabase database = this.getReadableDatabase();
		Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_CARDS, null);

		if(cursor.moveToFirst()){
			do {
				cardNames.add(cursor.getString(0));
			}while (cursor.moveToNext());
		}

		cursor.close();
		database.close();

		return cardNames;

	}

	public int getCardCount(){

		SQLiteDatabase database = this.getReadableDatabase();
		Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_CARDS, null);

		int count = cursor.getCount();

		cursor.close();

		return  count;

	}



}
