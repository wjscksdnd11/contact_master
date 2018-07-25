package com.tenqube.contactmaster;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

/**
 * Created by tenqube on 2018. 7. 25..
 */

public class MMS {
    private Context mContext;
    private ContentResolver contentResolver;

    public MMS(Context context) {

        this.mContext = context;

        contentResolver = mContext.getApplicationContext().getContentResolver();
    }

    public void ScanMMS() {
        System.out.println("==============================ScanMMS()==============================");
        //Initialize Box
        Uri uri = Uri.parse("content://mms-sms/conversations/");
        String[] proj = {"*"};
        ContentResolver cr = mContext.getContentResolver();

        Cursor c = cr.query(uri, proj, null, null, null);

        if (c != null && c.moveToFirst()) {
            do {
                    /*String[] col = c.getColumnNames();
                    String str = "";
                    for(int i = 0; i < col.length; i++) {
                        str = str + col[i] + ": " + c.getString(i) + ", ";
                    }
                    System.out.println(str);*/
                //System.out.println("--------------------MMS------------------");


                Log.i("MMS", Arrays.toString(c.getColumnNames()));
                c.getString(c.getColumnIndex("_id"));
                c.getString(c.getColumnIndex("thread_id"));
                c.getString(c.getColumnIndex("date"));
                Log.i("MMS", "c.getString(c.getColumnIndex(\"_id\")) :" + c.getString(c.getColumnIndex("_id")));
                Log.i("MMS", "c.getString(c.getColumnIndex(\"thread_id\"))" + c.getString(c.getColumnIndex("thread_id")));
                Log.i("MMS", "c.getString(c.getColumnIndex(\"date\"))" + c.getString(c.getColumnIndex("date")));
                Log.i("MMS", "c.getString(c.getColumnIndex(\"ct_t\"))" + c.getString(c.getColumnIndex("ct_t")));
                //System.out.println(msg);
            } while (c.moveToNext());
        }

        assert c != null;
        c.close();

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void smsMMS() {

        String methodTag = "SMS_MMS";
        Log.i("smsMMS", "READ_START");
        String uris = "content://mms-sms/conversations?simple=true";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Cursor c = contentResolver.query(Uri.parse(uris), null, null, null, null, null);
            if (c == null) Log.i("smsMMS", "CURSOR NULL");
            else {
                while (c.moveToNext()) {
                    String _id = c.getString(c.getColumnIndex("_id"));
                    String groupSnippet = c.getString(c.getColumnIndex("group_snippet"));
                    String recepientIds = c.getString(c.getColumnIndex("recipient_ids"));
                    Log.i(methodTag, "_id : " + _id);
                    Log.i(methodTag, "group_snippet : " + groupSnippet);
                    Log.i(methodTag, "recepientIds : " + recepientIds);
                    StringBuilder name = new StringBuilder();
                    if (recepientIds.contains(" ")) {
                        ArrayList<String> al = new ArrayList<>(Arrays.asList(recepientIds.split(" ")));
                        for (int i = 0; i < al.size(); i++) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                                al.set(i, getContactByRecipientId((long) Float.parseFloat(al.get(i))));
                        }
                        for (String alstr : al) {
                            Log.i(methodTag, "al : " + alstr);
                            name.append(" , ").append(alstr);
                        }
                        name = new StringBuilder(name.substring(1));
                    }else name = new StringBuilder(getContactByRecipientId((long)Float.parseFloat(recepientIds)));

                    Log.i(methodTag," name : " +name);
                    long dateL = c.getLong(c.getColumnIndex("date"));
                    String date = new SimpleDateFormat("yy-MM-dd hh:mm:ss", Locale.getDefault()).format(dateL);
                    Log.i(methodTag,"date : "+ date);


                }
            }

            if (c != null) {
                c.close();
            }

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private String getContactByRecipientId(long recipientId) {
        String contact = "";
        Uri uri = ContentUris.withAppendedId(Uri.parse("content://mms-sms/canonical-address"), recipientId);

        try (Cursor c = contentResolver.query(uri, null, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                Log.i("getContactByPhoneNumber", c.getString(0));

                contact = getContactByPhoneNumber(c.getString(0));
            }
        }

        return contact;
    }

    private String getContactByPhoneNumber(String phoneNumber) {

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            String[] projection = {ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup.NORMALIZED_NUMBER};
            String name = null;
            String tempPhoneNum = phoneNumber;
            Cursor c = mContext.getContentResolver().query(uri, projection, null, null, null, null);
            if (c == null) {
                Log.i("getContactByPhoneNumber", "CURSOR NULL");
            } else {
                if (c.getCount()>0) {
                    c.moveToFirst();
                    Log.i("getContactByPhoneNumber", Arrays.toString(c.getColumnNames()));
                    tempPhoneNum = c.getString(c.getColumnIndex(ContactsContract.PhoneLookup.NORMALIZED_NUMBER));
                    name = c.getString(c.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));

                    Log.i("getContactByPhoneNumber", "tempPhoneNum : " + tempPhoneNum + " name : " + name);
                }
                c.close();
            }

            return name == null ? phoneNumber : name;
        }

        return null;
    }

}

