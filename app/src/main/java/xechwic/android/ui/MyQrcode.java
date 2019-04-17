package xechwic.android.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import ydx.securephone.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.util.Hashtable;
import xechwic.android.XWDataCenter;

public class MyQrcode extends Activity implements OnClickListener{

	String TAG=MyQrcode.class.getSimpleName();
	int QR_WIDTH=300;
	int QR_HEIGHT=300;
	ImageView qr_image=null;//二维码图片
	ImageView myqr_back;//返回
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_myqrcode);
	
        initView();
        ////if(XWDataCenter.fni!=null)
        {
        	String text="xw,"+XWDataCenter.xwDC.loginName;
        	createImage(text);
        }
        
	}

	
	void initView(){
		myqr_back=(ImageView)findViewById(R.id.myqr_back);
		qr_image=(ImageView)findViewById(R.id.img_myqrcode);
		
		myqr_back.setOnClickListener(this);
		qr_image.setOnClickListener(this);
	}
	
	// 生成QR图
    private void createImage(String text) {
        Log.e(TAG, "生成的文本：" + text);
        if (text == null || "".equals(text) || text.length() < 1) {
            return;
        }
        try {
            // 需要引入core包
            QRCodeWriter writer = new QRCodeWriter();

            // 把输入的文本转为二维码
            BitMatrix martix = writer.encode(text, BarcodeFormat.QR_CODE,
                    QR_WIDTH, QR_HEIGHT);

            System.out.println("w:" + martix.getWidth() + "h:"
                    + martix.getHeight());

            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            BitMatrix bitMatrix = new QRCodeWriter().encode(text,
                    BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
            int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
            for (int y = 0; y < QR_HEIGHT; y++) {
                for (int x = 0; x < QR_WIDTH; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * QR_WIDTH + x] = 0xff000000;
                    } else {
                        pixels[y * QR_WIDTH + x] = 0xffffffff;
                    }

                }
            }

            Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT,
                    Bitmap.Config.ARGB_8888);

            bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
            qr_image.setImageBitmap(bitmap);

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }


	@Override
	public void onClick(View arg0) {
		switch(arg0.getId()){
		case R.id.myqr_back:
			MyQrcode.this.finish();
			break;
		default:
			break;
		}
		
	}
	
}
