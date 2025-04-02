package com.example.ac1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private EditText editTitle, editAuthor;
    private Spinner spinnerCategory;
    private CheckBox checkRead;
    private Button btnSave;
    private ListView listViewBooks;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> bookList;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTitle = findViewById(R.id.editTitle);
        editAuthor = findViewById(R.id.editAuthor);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        checkRead = findViewById(R.id.checkRead);
        btnSave = findViewById(R.id.btnSave);
        listViewBooks = findViewById(R.id.listViewBooks);

        dbHelper = new DatabaseHelper(this);
        bookList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, bookList);
        listViewBooks.setAdapter(adapter);

        // Configurar o Spinner com as categorias do strings.xml
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.book_categories)));
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);

        loadBooks();

        btnSave.setOnClickListener(view -> saveBook());

        listViewBooks.setOnItemLongClickListener((parent, view, position, id) -> {
            deleteBook(position);
            return true;
        });
    }

    private void saveBook() {
        String title = editTitle.getText().toString().trim();
        String author = editAuthor.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem() != null ? spinnerCategory.getSelectedItem().toString() : "";
        boolean isRead = checkRead.isChecked();

        if (title.isEmpty() || author.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        dbHelper.insertBook(title, author, category, isRead);
        loadBooks();
        editTitle.setText("");
        editAuthor.setText("");
        checkRead.setChecked(false);
    }

    private void loadBooks() {
        bookList.clear();
        Cursor cursor = dbHelper.getAllBooks();
        while (cursor.moveToNext()) {
            String book = cursor.getString(1) + " - " + cursor.getString(2) + " (" + cursor.getString(3) + ")";
            bookList.add(book);
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }

    private void deleteBook(int position) {
        dbHelper.deleteBook(bookList.get(position).split(" - ")[0]);
        loadBooks();
        Toast.makeText(this, getString(R.string.delete_hint), Toast.LENGTH_SHORT).show();
    }

    static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "books.db";
        private static final int DATABASE_VERSION = 1;

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE books (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, author TEXT, category TEXT, read INTEGER)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS books");
            onCreate(db);
        }

        void insertBook(String title, String author, String category, boolean isRead) {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("title", title);
            values.put("author", author);
            values.put("category", category);
            values.put("read", isRead ? 1 : 0);
            db.insert("books", null, values);
        }

        Cursor getAllBooks() {
            SQLiteDatabase db = getReadableDatabase();
            return db.rawQuery("SELECT * FROM books", null);
        }

        void deleteBook(String title) {
            SQLiteDatabase db = getWritableDatabase();
            db.delete("books", "title = ?", new String[]{title});
        }
    }
}