package com.example.word;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class WorddetailsActivity extends AppCompatActivity {

    TextView tvWord;
    TextView tvSymbolUS;
    TextView tvSymbolUK;
    EditText etWordmean;
    EditText etSentenceCHS;
    EditText etSentence;
    ImageButton btnSave;
    ImageButton btnDel;

    Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acttivity_worddetails);

        tvWord = findViewById(R.id.tv_word);
        tvSymbolUK = findViewById(R.id.tv_symbolUK);
        tvSymbolUS = findViewById(R.id.tv_symbolUS);
        etWordmean = findViewById(R.id.et_wordmean);
        etSentence = findViewById(R.id.et_sentence);
        etSentenceCHS = findViewById(R.id.et_sentenceCHS);
        btnSave = findViewById(R.id.btn_save);
        btnDel = findViewById(R.id.btn_del);

        //获取javabean
        final WordValue wordValue = (WordValue)getIntent().getSerializableExtra("bean");

        database = new Database(this,"dict",null,2);
        final SQLiteDatabase db = database.getWritableDatabase();

        //为各个textView设置文本
        tvWord.setText(wordValue.getWord());
        etWordmean.setText(wordValue.getInterpret());
        tvSymbolUK.setText("英音["+wordValue.getPsE()+"]");
        tvSymbolUS.setText("美音["+wordValue.getPsA()+"]");
        etSentence.setText(wordValue.getSentOrig());
        etSentenceCHS.setText(wordValue.getSentTrans());

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentValues values = new ContentValues();
                values.put("interpret",etWordmean.getText().toString());
                values.put("sentorig",etSentence.getText().toString());
                values.put("senttrans",etSentenceCHS.getText().toString());
                int i = db.update("dict",values,"word=?",new String[]{tvWord.getText().toString()});
                if(i > 0){
                    Toast.makeText(WorddetailsActivity.this,"更新成功",Toast.LENGTH_SHORT).show();
                }else
                    Toast.makeText(WorddetailsActivity.this,"更新失败",Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(WorddetailsActivity.this,WordnoteActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //从数据库中删除当前项并跳转回activity
                int i = db.delete("dict","word=?",new String[]{wordValue.getWord()});
                if(i > 0){
                    Toast.makeText(WorddetailsActivity.this,"删除成功",Toast.LENGTH_SHORT).show();
                }else
                    Toast.makeText(WorddetailsActivity.this,"删除失败",Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(WorddetailsActivity.this,WordnoteActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}
