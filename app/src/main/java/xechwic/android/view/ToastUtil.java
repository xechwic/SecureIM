package xechwic.android.view;


import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import ydx.securephone.R;

/**
 * 提示工具类
 * @author luman
 *
 */
public class ToastUtil {

	private static final String TAG = ToastUtil.class.getSimpleName();

	private Context context = null;

	// 位置
	private int gravity = android.view.Gravity.CENTER;

	private int backgroundResourceID = R.drawable.tools_prompt;
	private int imageViewID = 0;

	private LayoutParams paramWrap = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

	private int textSize = 20;
	private int textColor = Color.WHITE;
	private String showText = null;

	private TextView textView = null;
	private ImageView imageView = null;
	private LinearLayout linearShow = null;

	//	private int duration = Toast.LENGTH_LONG;
	private int duration = Toast.LENGTH_LONG;

	private Toast toast = null;

	public ToastUtil(Context context) {
		init(context);
	}
	public static ToastUtil getInstance(Context context) {
		return new ToastUtil(context);
	}
	/**
	 * 初始化
	 * 
	 * @param context
	 */
	private void init(Context context) {
		if (context == null) {
			throw new NullPointerException("context == null");
		}
		this.context = context;
	}

	/**
	 * 判断字符串是否为空，等于null或者长度不大于零都视为空字符串
	 * 
	 * @param src
	 * @return
	 */
	private static boolean isEmptyString(String src) {
		if (src == null) {
			return true;
		}

		return src.length() <= 0;

	}

	/**
	 * 设置背景图片资源ID
	 * 
	 * mLinearShow.setBackgroundResource(R.drawable.toast);
	 * 
	 * @param resid
	 */
	public void setBackgroundResource(int resid) {
		this.backgroundResourceID = resid;
	}

	/**
	 * 设置图片资源ID
	 * 
	 * @param resId
	 */
	public void setIcon(int resId) {
		imageViewID = resId;
	}

	/**
	 * 设置文字大小
	 * 
	 * @param size
	 */
	public void setTextSize(int size) {
		textSize = size;
	}

	/**
	 * 设置文字色彩
	 * 
	 * @param color
	 */
	public void setTextColor(int color) {
		textColor = color;
	}

	/**
	 * 设置文本
	 * 
	 * @param text
	 */
	public void setText(String text) {
		this.showText = text;
	}

	/**
	 * 设置文本
	 * 
	 * @param resId
	 */
	public void setText(int resId) {
		String text = context.getString(resId);
		setText(text);
	}

	/**
	 * 设置持续时间
	 * 
	 * @param duration
	 */
	public void setDuration(int duration) {
		this.duration = duration;
	}

	/**
	 * 设置位置
	 * android.view.Gravity.CENTER
	 */
	public void setGravity(int _gravity) {
		this.gravity = _gravity;
	}

	/**
	 * 显示Toast
	 */
	public void show() {

		Log.d(TAG, "Prompt.show()");

		// 图片
		if (imageView == null) {
			imageView = new ImageView(context);
			imageView.setLayoutParams(paramWrap);
		}
		imageView.setBackgroundResource(imageViewID);

		// 文字
		if (textView == null) {
			textView = new TextView(context);
			paramWrap.gravity = this.gravity; // 居中
			textView.setLayoutParams(paramWrap);
		}
		textView.setTextSize(textSize);
		textView.setTextColor(textColor);
		textView.setText(this.showText);

		// 布局
		if (linearShow == null) {
			linearShow = new LinearLayout(context);
			linearShow.setLayoutParams(paramWrap);
			linearShow.setOrientation(LinearLayout.VERTICAL);
		}
		linearShow.setBackgroundResource(backgroundResourceID);

		// 添加
		if (linearShow.getChildCount() <= 0) {
			linearShow.addView(imageView);
			linearShow.addView(textView);
		}

		// 显示
		if (toast == null) {
			toast = new Toast(context);
			toast.setGravity(this.gravity, 0, 0); // 居中
			toast.setView(linearShow);
		}
		toast.setDuration(duration);

		// 显示
		toast.show();
	}
	/**
	 * 显示Toast

	 */
	public void show(String text) {

		Log.d(TAG, "Prompt.show()");


		// 文字
		if (textView == null) {
			textView = new TextView(context);
			paramWrap.gravity = this.gravity; // 居中
			textView.setLayoutParams(paramWrap);
		}
		textView.setTextSize(textSize);
		textView.setTextColor(textColor);
		textView.setText(text);

		// 布局
		if (linearShow == null) {
			linearShow = new LinearLayout(context);
			linearShow.setLayoutParams(paramWrap);
			linearShow.setOrientation(LinearLayout.VERTICAL);
		}
		linearShow.setBackgroundResource(backgroundResourceID);

		// 添加
		if (linearShow.getChildCount() <= 0) {
			linearShow.addView(textView);
		}

		// 显示
		if (toast == null) {
			toast = new Toast(context);
			toast.setGravity(this.gravity, 0, 0); // 居中
			toast.setView(linearShow);
		}
		toast.setDuration(duration);

		// 显示
		toast.show();
	}
	
	/**
	 * 显示Toast
	 */
	public void show(int  imageViewID,String text) {

		Log.d(TAG, "Prompt.show()");

		// 图片
		if (imageView == null) {
			imageView = new ImageView(context);
			imageView.setLayoutParams(paramWrap);
		}
		imageView.setBackgroundResource(imageViewID);

		// 文字
		if (textView == null) {
			textView = new TextView(context);
			paramWrap.gravity = this.gravity; // 居中
			textView.setLayoutParams(paramWrap);
		}
		textView.setTextSize(textSize);
		textView.setTextColor(textColor);
		textView.setText(text);

		// 布局
		if (linearShow == null) {
			linearShow = new LinearLayout(context);
			linearShow.setLayoutParams(paramWrap);
			linearShow.setOrientation(LinearLayout.VERTICAL);
		}
		linearShow.setBackgroundResource(backgroundResourceID);

		// 添加
		if (linearShow.getChildCount() <= 0) {
			linearShow.addView(imageView);
			linearShow.addView(textView);
		}

		// 显示
		if (toast == null) {
			toast = new Toast(context);
			toast.setGravity(this.gravity, 0, 0); // 居中
			toast.setView(linearShow);
		}
		toast.setDuration(duration);

		// 显示
		toast.show();
	}
	
	/**
	 *  关闭Toast 
	 */
	public void cancel() {
		if (toast == null) {
			return;
		}
		toast.cancel();
	}

	/**
	 * 显示Toast
	 *
	 * @param context
	 * @param text
	 */
	public static void showToast(Context context, String text) {
		if (context == null) {
			new NullPointerException("context == null").printStackTrace();
			return;
		} 
		if (isEmptyString(text)) {
			new NullPointerException("text == null").printStackTrace();
			return;
		}

		Toast.makeText(context, text, Toast.LENGTH_LONG).show();
	}

	/**
	 * 显示Toast
	 * 
	 * @param context
	 * @param resid
	 */
	public static void showToast(Context context, int resid) {
		if (context == null) {
			new NullPointerException("context == null").printStackTrace();
			return;
		}
		String text = context.getString(resid);
		showToast(context, text);
	}

}
