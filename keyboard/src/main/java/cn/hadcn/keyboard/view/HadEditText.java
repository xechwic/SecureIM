package cn.hadcn.keyboard.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import cn.hadcn.keyboard.emoticon.util.EmoticonHandler;
import cn.hadcn.keyboard.utils.Utils;

public class HadEditText extends EditText {


    public HadEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    public HadEditText(Context context) {
        super(context);

    }

    public HadEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onTextChanged(CharSequence arg0, int start, int lengthBefore, int after) {
        super.onTextChanged(arg0, start, lengthBefore, after);
        if(onTextChangedInterface != null){
            onTextChangedInterface.onTextChanged(arg0);
        }
        String content = arg0.subSequence(0, start + after).toString();
        EmoticonHandler.getInstance(getContext()).setTextFace(content, getText(), start, Utils.getFontSize(getTextSize()));
    }

    public interface OnTextChangedInterface {
        void onTextChanged(CharSequence argo);
    }

    OnTextChangedInterface onTextChangedInterface;

    public void setOnTextChangedInterface(OnTextChangedInterface i) {
        onTextChangedInterface = i;
    }
}
