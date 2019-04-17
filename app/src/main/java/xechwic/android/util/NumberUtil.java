package xechwic.android.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class NumberUtil {
    //电话号码规则
    private final static String REGEX_MOBILEPHONE = "^0?1[34578]\\d{9}$";

    //固话
    private final static String REGEX_FIXEDPHONE = "^(010|02\\d|0[3-9]\\d{2})?\\d{6,8}$";

    //地区代码
    private final static String REGEX_ZIPCODE = "^(010|02\\d|0[3-9]\\d{2})\\d{6,8}$";

    private static Pattern PATTERN_MOBILEPHONE;
    private static Pattern PATTERN_FIXEDPHONE;
    private static Pattern PATTERN_ZIPCODE;


    static {
        PATTERN_FIXEDPHONE = Pattern.compile(REGEX_FIXEDPHONE);
        PATTERN_MOBILEPHONE = Pattern.compile(REGEX_MOBILEPHONE);
        PATTERN_ZIPCODE = Pattern.compile(REGEX_ZIPCODE);
    }

    public enum PhoneType {
        CELLPHONE,
        FIXEDPHONE,
        INVALIDPHONE
    }

    public static class Number {
        private PhoneType type;
        private String code;
        private String number;

        public Number(PhoneType _type, String _code, String _number) {
            this.type = _type;
            this.code = _code;
            this.number = _number;
        }

        public PhoneType getType() {
            return type;
        }

        public String getCode() {
            return code;
        }

        public String getNumber() {
            return number;
        }

        public String toString(){
            return String.format("[number:%s, type:%s, code:%s]", number, type.name(), code);
        }
    }


    public static boolean isCellPhone(String number) {
        Matcher match = PATTERN_MOBILEPHONE.matcher(number);
        return match.matches();
    }

    public static boolean isFixedPhone(String number) {
        Matcher match = PATTERN_FIXEDPHONE.matcher(number);
        return match.matches();
    }



    public static String getZipFromHomephone(String strNumber) {
        Matcher matcher = PATTERN_ZIPCODE.matcher(strNumber);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }


    public static String getNumber(String _number){
        String number = _number;
        Number rtNum =null;
        
        
        if (_number==null)
        	return "";        
        try
        {
	        if (_number.startsWith("+86"))
	        {
	        	_number=_number.substring(3);
	        }
	        else  if (_number.startsWith("+"))
	        {
	        	_number=_number.substring(1);
	        }
	        
	        _number=_number.replaceAll(" ", "");
	        _number=_number.replaceAll("-", "");
	        	
	        if(_number.length()>0){
	            if(isCellPhone(_number)){
	                if(_number.charAt(0) == '0'){
                        _number = number.substring(1);
	                }
	                rtNum = new Number(PhoneType.CELLPHONE, _number.substring(0, 3), _number);
	            }else if(isFixedPhone(_number)){
	                String zipCode = getZipFromHomephone(_number);
                    if(zipCode!=null){
                        rtNum = new Number(PhoneType.FIXEDPHONE, zipCode, _number.substring(zipCode.length()));
                    }else{
                        rtNum = new Number(PhoneType.FIXEDPHONE, null, _number);
                    }
	            }else{
	                rtNum = new Number(PhoneType.FIXEDPHONE, null, _number);
	            }
	        }
	        else
	        	return "";
	        
	        String rtNumber="";
            if(rtNum!=null){
                rtNumber=rtNum.number;
            }
	        return rtNumber;
        
        }
        catch(Exception ex)
        {
        	return _number;
        }
    }

    
    /**
     * 将号码数字转出中文
     */
    public static String numberToWord(String number){
    	if(number==null){
    		return "";
    	}
    	number=number.replace("+", "加");
    	number=number.replace("0", "零");
    	number=number.replace("1", "一");
    	number=number.replace("2", "二");
    	number=number.replace("3", "三");
    	number=number.replace("4", "四");
    	number=number.replace("5", "五");
    	number=number.replace("6", "六");
    	number=number.replace("7", "七");
    	number=number.replace("8", "八");
    	number=number.replace("9", "九");
    	return number;
    }
}
 