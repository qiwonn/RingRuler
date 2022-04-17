package com.wqd.widget;

import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import android.os.Handler;
import android.content.Context;
import android.os.Message;

public enum TestUIManager
{
	INSTANCE {

		@Override
		public void initContext(Context context,Handler handler)
		{
			// TODO: Implement this method
			mContext = context;
			mHandler = handler;
		}

		@Override
		public void hideSoftInput()
		{
			// TODO: Implement this method
			mHandler.sendEmptyMessage(4627);
			/*InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
			 if(imm.isActive()){
			 imm.hideSoftInputFromWindow(((GeometricThinkingActivity)mContext).getWindow().getDecorView().getWindowToken(), 0);
			 }*/
		}

		@Override
		public void showToast(String s)
		{
			// TODO: Implement this method
			Toast.makeText(mContext,s,Toast.LENGTH_SHORT).show();
		}

		@Override
		public void sendEmptyMessage(int what)
		{
			// TODO: Implement this method
			mHandler.sendEmptyMessage(what);
		}

		@Override
		public void sendMessage(int what, Object obj)
		{
			// TODO: Implement this method
			Message m = new Message();
			m.what = what;
			m.obj = obj;
			mHandler.sendMessage(m);
		}


	};

	private static Context mContext;
	private static Handler mHandler;
	public abstract void initContext(Context context,Handler handler);
	public abstract void hideSoftInput();
	public abstract void showToast(String s);
	public abstract void sendEmptyMessage(int what);
	public abstract void sendMessage(int what,Object obj);
}
