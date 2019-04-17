package xechwic.android;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmileyParser {
    private Pattern mPattern;   
    public final static String[] mSmileyTexts=new String[]{
            "smiley_0,"+":)",
            "smiley_1,"+":d",
            "smiley_2,"+";)",
            "smiley_3,"+":o",
            "smiley_4,"+":p",
            "smiley_5,"+"(h)",
            "smiley_6,"+":@",
            "smiley_7,"+":s",
            "smiley_8,"+":$",
            "smiley_9,"+":(",
            "smiley_10,"+":'(",
            "smiley_11,"+":|" ,
            "smiley_12,"+"(a)",
            "smiley_13,"+"Bo|",
            "smiley_14,"+"B-|",
            "smiley_15,"+"+o(",
            "smiley_16,"+":b" ,
            "smiley_17,"+"|-)",
            "smiley_18,"+"*-)",
            "smiley_19,"+":-#",
            "smiley_20,"+":-*",
            "smiley_21,"+"^o)",
            "smiley_22,"+"B-)",
            "smiley_23,"+"(6)",
            "smiley_24,"+":^)",
            "smiley_25,"+"(v)",
            "smiley_26,"+"(@)",
            "smiley_27,"+"(&)",
            "smiley_28,"+"(sn)",
            "smiley_29,"+"(bah)",
            "smiley_30,"+"(S)",
            "smiley_31,"+"(*)" ,
            "smiley_32,"+"(#)" ,
            "smiley_33,"+"(r)" ,
            "smiley_34,"+"([)" ,
            "smiley_35,"+"(])" ,
            "smiley_36,"+"(k)" ,
            "smiley_37,"+"(f)" ,
            "smiley_38,"+"(w)" ,
            "smiley_39,"+"(o)",
            "smiley_40,"+"(g)" ,
            "smiley_41,"+"(^)" ,
            "smiley_42,"+"(p)" ,
            "smiley_43,"+"(i)" ,
            "smiley_44,"+"(c)" ,
            "smiley_45,"+"(t)" ,
            "smiley_46,"+"(mp)" ,
            "smiley_47,"+"(au)" ,
            "smiley_48,"+"(ap)" ,
            "smiley_49,"+"(co)",
            "smiley_50,"+"(mo)" ,
            "smiley_51,"+"(~)" ,
            "smiley_52,"+"(8)" ,
            "smiley_53,"+"(pi)" ,
            "smiley_54,"+"(so)" ,
            "smiley_55,"+"(e)" ,
            "smiley_56,"+"(z)" ,
            "smiley_57,"+"(x)" ,
            "smiley_58,"+"(ip)" ,
            "smiley_59,"+"(um)",
            "smiley_60,"+":[" ,
            "smiley_61,"+"(n)" ,
            "smiley_62,"+"(y)" ,
            "smiley_63,"+"(ba)" ,
            "smiley_64,"+"(st)" ,
            "smiley_65,"+"(li)" ,
            "smiley_66,"+"(L)" ,
            "smiley_67,"+"(u)" ,
            "smiley_68,"+"($)" ,
            "smiley_69,"+"(he)",
            "icon_file,"+"(:file)",
            "icon_picture,"+"(:image)",
            "icon_voice,"+"(:voice)",
            "icon_video,"+"(:video)"};
    private HashMap<String, String> mSmileyToRes;
//    public static final String[] DEFAULT_SMILEY_RES_IDS = {
//        "smiley_0","smiley_1","smiley_2",smiley_3,smiley_4,smiley_5,smiley_6,smiley_7,smiley_8,smiley_9,
//        smiley_10,smiley_11,smiley_12,smiley_13,smiley_14,smiley_15,smiley_16,smiley_17,smiley_18,smiley_19,
//        smiley_20,smiley_21,smiley_22,smiley_23,smiley_24,smiley_25,smiley_26,smiley_27,smiley_28,smiley_29,
//        smiley_30,smiley_31,smiley_32,smiley_33,smiley_34,smiley_35,smiley_36,smiley_37,smiley_38,smiley_39,
//        smiley_40,smiley_41,smiley_42,smiley_43,smiley_44,smiley_45,smiley_46,smiley_47,smiley_48,smiley_49,
//        smiley_50,smiley_51,smiley_52,smiley_53,smiley_54,smiley_55,smiley_56,smiley_57,smiley_58,smiley_59,
//        smiley_60,smiley_61,smiley_62,smiley_63,smiley_64,smiley_65,smiley_66,smiley_67,smiley_68,smiley_69,
//        icon_file,icon_picture,icon_voice,
//    };
    
    public SmileyParser() {   
        mSmileyToRes = buildSmileyToRes();   
        mPattern = buildPattern();   
    }     
 
    private HashMap<String, String> buildSmileyToRes() {
//        if (DEFAULT_SMILEY_RES_IDS.length != mSmileyTexts.length) {
////          Log.w("SmileyParser", "Smiley resource ID/text mismatch");
//            //表情的数量需要和数组定义的长度一致！
//            throw new IllegalStateException("Smiley resource ID/text mismatch");
//        }
  
        HashMap<String, String> smileyToRes = new HashMap<>(mSmileyTexts.length);
        for (int i = 0; i < mSmileyTexts.length; i++) {
            String[] strs=mSmileyTexts[i].split(",");
            smileyToRes.put(strs[1], strs[0]);//用表情字符作为key
        }   
  
        return smileyToRes;   
    }   
    //构建正则表达式   
    private Pattern buildPattern() {   
        StringBuilder patternString = new StringBuilder(mSmileyTexts.length * 3);   
        patternString.append('(');
        Iterator iterator=mSmileyToRes.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry entry=(Map.Entry)iterator.next();
            String s=entry.getKey().toString();
            patternString.append(Pattern.quote(s));
            patternString.append('|');
        }
//        for (String s : mSmileyTexts) {
//            patternString.append(Pattern.quote(s));
//            patternString.append('|');
//        }
        patternString.replace(patternString.length() - 1, patternString.length(), ")");   
  
        return Pattern.compile(patternString.toString());   
    }   
    //根据文本替换成图片   
    public CharSequence replace(CharSequence text,Context context) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);   
        Matcher matcher = mPattern.matcher(text);   
        while (matcher.find()) {   
            String resStr = mSmileyToRes.get(matcher.group());
            Log.e("main","resStr:"+resStr);
            int resId=context.getResources().getIdentifier(resStr, "drawable", context.getPackageName());
            builder.setSpan(new ImageSpan(context, resId),matcher.start(), matcher.end(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);   
        }   
        return builder;   
    }   
}  
