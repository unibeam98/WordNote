package com.example.word;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.xml.sax.InputSource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;

public class Dictionary {
    public Context context = null;
    public String tableName = null;
    private Database dbHelper = null;
    private SQLiteDatabase dbR = null,dbW = null;

    public Dictionary(Context context,String tableName){
        this.context = context;
        this.tableName = tableName;
        dbHelper = new Database(context,tableName);
        //这里要用到前面的DataBaseHelper类，在Dict的构造方法中实例化该类，
        //并且调用下面两个方法获得dbR和dbW,用于完成对数据库的增删改查操作。
        //这里吧dbR dbW作为成员变量目的是避免反复实例化dbR  dbW造成数据库指针泄露。

        dbR = dbHelper.getReadableDatabase();
        dbW = dbHelper.getWritableDatabase();
    }

    //释放dbR,dbW
    @Override
    protected void finalize() throws Throwable {
        if(dbR != null && dbW != null){
            dbR.close();
            dbW.close();
            dbHelper.close();
        }
        super.finalize();
    }

    //将包含单词信息的WordValue对象添加进数据库
    //这里使用了dbW的insert方法，需要创建一个ContentValue对象存放键值对
    public void insertWordToDict(WordValue w,boolean isOverWrite){
        if(w == null){
            return;
        }
        Cursor cursor = null;
        try{
            ContentValues values = new ContentValues();
            values.put("word",w.getWord() );
            values.put("pse", w.getPsE());
            values.put("prone",w.getPronE());
            values.put("psa", w.getPsA());
            values.put("prona", w.getPronA());
            values.put("interpret",w.getInterpret());
            values.put("sentorig", w.getSentOrig());
            values.put("senttrans", w.getSentTrans());
            cursor=dbR.query(tableName, new String[]{"word"}, "word=?", new String[]{w.getWord()}, null, null, null);
            if(cursor.getCount() > 0){
                if(isOverWrite == false)
                    return;
                //首先看看数据库中有没有这个单词，若词典库中已经有了这一个单词，所以不再操作
                else{  //执行更新操作
                    dbW.update(tableName,values,"word=?",new String[]{ w.getWord()});
                }
            }else{
                dbW.insert(tableName,null,values);
                //可能会发生空指针异常
            }
        }catch (Exception e){

        }finally {
            if(cursor != null)
                cursor.close();
        }
    }

    //判读数据库中是否有这个单词
    public boolean isWordExist(String word){
        Cursor cursor = null;
        try{
            cursor=dbR.query(tableName,new String[]{"word"},"word=?",new String[]{word},null,null,null);
            if(cursor.getCount() > 0){
                cursor.close();
                return true;
            }
            else{
                cursor.close();
                return false;
            }
        }finally {
            if(cursor != null)
                cursor.close();
        }
    }

    //从单词库中获得某个单词的信息，如果没有单词，则返回null
    public WordValue getWordFromDict(String searchedWord){
        WordValue w = new WordValue();//防止空指针
        w = null;
        String[] columns = new String[]{"word",
                "pse","prone","psa","prona","interpret","sentorig","senttrans"};
        String[] strArray = new String[8];
        Cursor cursor = dbR.query(tableName,columns,"word=?",new String[]{searchedWord},null,null,null);
        while(cursor.moveToNext()){
            for(int i=0;i < strArray.length;i++){
                strArray[i] = cursor.getString(cursor.getColumnIndex(columns[i]));
            }
            w = new WordValue(strArray[0],strArray[1],strArray[2],strArray[3],strArray[4],strArray[5],strArray[6],strArray[7]);
        }
        cursor.close();
        return w;
    }

    //从网络中查词，并返回WordValue对象
    public WordValue getWordFromInternet(String searchedWord){
        WordValue wordValue = null;
        String tempWord = searchedWord;
        if(tempWord == null && tempWord.equals(""))
            return null;
        char[] array = tempWord.toCharArray();
        if(array[0] > 256)  //不是英语的语言检测，并转换
            tempWord = "_"+ URLEncoder.encode(tempWord);
        InputStream in = null;
        String str = null;
        try{
            String tempUrl = NetworkConnect.iCiBaURL1+tempWord+NetworkConnect.iCiBaURL2;
            in = NetworkConnect.getInputStreamByUrl(tempUrl);
            if(in != null){
                JinShanXMLparser xmlParser=new JinShanXMLparser();
                InputStreamReader reader=new InputStreamReader(in,"utf-8");
                JinShanContentHandler contentHandler=new JinShanContentHandler();
                xmlParser.parseJinShanXml(contentHandler, new InputSource(reader));
                wordValue=contentHandler.getWordValue();
                wordValue.setWord(searchedWord);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return wordValue;
    }


    //以下几个方法都是获得某个单词的某一项信息，基本的思路还是先获得全部信息WordValue然后调用WordValue的get方法获得具体的信息。
    //获取发音文件地址
    public String getPronEngUrl(String searchedWord){
        Cursor cursor=dbR.query(tableName, new String[]{"prone"}, "word=?", new String[]{searchedWord}, null, null, null);
        if(cursor.moveToNext()==false){
            cursor.close();
            return null;
        }
        String str=cursor.getString(cursor.getColumnIndex("prone"));
        cursor.close();
        return str;

    }

    public String getPronUSAUrl(String searchedWord){
        Cursor cursor=dbR.query(tableName, new String[]{"prona"}, "word=?", new String[]{searchedWord}, null, null, null);
        if(cursor.moveToNext()==false){
            cursor.close();
            return null;
        }
        String str=cursor.getString(cursor.getColumnIndex("prona"));
        cursor.close();
        return str;
    }

    //获取音标
    public String getPsEng(String searchedWord){
        Cursor cursor=dbR.query(tableName, new String[]{"pse"}, "word=?", new String[]{searchedWord}, null, null, null);
        if(cursor.moveToNext()==false){
            cursor.close();
            return null;
        }
        String str=cursor.getString(cursor.getColumnIndex("pse"));
        cursor.close();
        return str;
    }

    public String getPsUSA(String searchedWord){
        Cursor cursor=dbR.query(tableName, new String[]{"psa"}, "word=?", new String[]{searchedWord}, null, null, null);
        if(cursor.moveToNext()==false){
            cursor.close();
            return null;
        }
        String str=cursor.getString(cursor.getColumnIndex("psa"));
        cursor.close();
        return str;
    }

    public String getInterpret(String searchedWord){
        Cursor cursor=dbR.query(tableName, new String[]{"interpret"}, "word=?", new String[]{searchedWord}, null, null, null);
        if(cursor.moveToNext()==false){
            cursor.close();
            return null;
        }
        String str=cursor.getString(cursor.getColumnIndex("interpret"));
        cursor.close();
        return str;

    }
}
