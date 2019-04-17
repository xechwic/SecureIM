package xechwic.android.view;

import ydx.securephone.R;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.LinearLayout;
import android.widget.TextView;





/**
 * 加载进度框
 * @author luman
 *
 */
public class LoadingDialog extends Dialog    {

	private TextView tv;
    private String title=null;
    
    DialogInterface.OnKeyListener keylistener = new DialogInterface.OnKeyListener(){
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            /*&&event.getRepeatCount()==0*/
			return keyCode == KeyEvent.KEYCODE_BACK;
        }
    } ;
    
    
	public LoadingDialog(Context context) {
		super(context,R.style.loadingDialogStyle);
	}
	
	public LoadingDialog(Context context,String title) {
		super(context,R.style.loadingDialogStyle);
		this.title=title;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_loading);
		
		try
		{
		tv = (TextView)findViewById(R.id.tv);
		if(title!=null){
			tv.setText(""+title);
		}
		
		  LinearLayout linearLayout = (LinearLayout)this.findViewById(R.id.LinearLayout);  
	      linearLayout.getBackground().setAlpha(210);  
	     
		}
		catch(Exception ex)
		{
			
		}
	}

	public void setText(String text){
		if(text!=null){
			tv.setText(""+text);
		}
	}
	
	public void disableBackPress(){
	    setOnKeyListener(keylistener);
	}
	
	public void enableBackPress(){
		setOnKeyListener(null);
	}
}