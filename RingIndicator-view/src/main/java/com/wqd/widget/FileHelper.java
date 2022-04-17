package com.wqd.widget;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileReader;
import android.graphics.*;
import android.os.Environment;
import java.io.FileOutputStream;
import java.io.*;
import java.util.*;

public class FileHelper {

    private static FileHelper fileHelper;

    public static FileHelper getInstance() {
        if (fileHelper == null)
            fileHelper = new FileHelper();
        return fileHelper;
    }


	@SuppressWarnings("unused")
	public void str2File(String result, String outPath) {
		try{
			String folder = Environment.getExternalStorageDirectory().getAbsolutePath();

			File txt = new File(folder + outPath);
			if (!txt.exists()) {
				txt.createNewFile();
			}
			byte bytes[] = new byte[512];
			bytes = result.getBytes();
//		int b = bytes.length; // ???????????????
			FileOutputStream fos = new FileOutputStream(txt);
//		fos.write(bytes, 0, b);
			fos.write(bytes);
			fos.flush();
			fos.close();
		}catch (IOException e){
			e.printStackTrace();
		}

	}

	public String loadTxt(String path){
		String folder = Environment.getExternalStorageDirectory().getAbsolutePath();
		BufferedReader reader = null;
		StringBuilder content = new StringBuilder();
		try{
			File file = new File(folder+path);
			if(null==file){
				return "";
			}
			reader = new BufferedReader(new FileReader(file));
			String line = "";
			while ((line = reader.readLine())!=null){
				content.append(line);
			}
		}catch (IOException e){
			e.printStackTrace();
		}finally {
			try{
				if(reader!=null){
					reader.close();
				}
			}catch (IOException e){
				e.printStackTrace();
			}
		}
		return content.toString();
	}

	public boolean fuckBitmap2Pic(Bitmap bp,String ext) {
		File file = new File(Environment.getExternalStorageDirectory()  
							 .getAbsolutePath() + "/AppProjects/" + System.currentTimeMillis() + ext);// 保存到sdcard根目录下
        //Log.i("CXC", Environment.getExternalStorageDirectory().getPath());  
        FileOutputStream fos = null;  
        try {  
            fos = new FileOutputStream(file);  
            bp.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
			return false;
        } finally{
			try {  
				fos.close();  
			}catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
}
