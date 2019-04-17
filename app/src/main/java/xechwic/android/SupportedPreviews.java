package xechwic.android;

import java.lang.reflect.Method;
import java.util.List;

import android.hardware.Camera;
import android.util.Log;
public class SupportedPreviews {
    private static Method Parameters_getSupportedPreviewSizes = null;     
    private static Method Parameters_getSupportedPreviewFrameRates = null;
    private static Method Parameters_getSupportedPreviewFormats = null;
    static {         
    	initCompatibility();     
	}
	private static void initCompatibility(){         
		try {             
			Parameters_getSupportedPreviewSizes = Camera.Parameters.class.getMethod("getSupportedPreviewSizes", new Class[] {});
			Parameters_getSupportedPreviewFrameRates = Camera.Parameters.class.getMethod("getSupportedPreviewFrameRates", new Class[] {});
			Parameters_getSupportedPreviewFormats = Camera.Parameters.class.getMethod("getSupportedPreviewFormats", new Class[] {});
		}catch(NoSuchMethodException nsme){             
			//nsme.printStackTrace();             
			Parameters_getSupportedPreviewSizes = Parameters_getSupportedPreviewFrameRates = Parameters_getSupportedPreviewFormats = null;         
		}     
	}       
	/*** Android 2.1之后有效* @param p * @return Android1.x返回null*/    
	public static List getSupportedPreviewSizes(Camera.Parameters p) {         
		return getSupportedSizes(p, Parameters_getSupportedPreviewSizes);     
	}       
	public static List getSupportedPreviewFrameRates(Camera.Parameters p){         
		return getSupportedSizes(p, Parameters_getSupportedPreviewFrameRates);     
	}  
	public static List getSupportedPreviewFormats(Camera.Parameters p){         
		return getSupportedSizes(p, Parameters_getSupportedPreviewFormats);     
	}
	private static List getSupportedSizes(Camera.Parameters p, Method method){         
		try{             
			if(method!=null){                 
				return (List)method.invoke(p);             
			}else{                 
				return null;             
			}
		}catch(Exception e){
			return null;
		}
//		}catch(InvocationTargetException ite){             
//			Throwable cause = ite.getCause();             
//			if(cause instanceof RuntimeException){                 
//				throw (RuntimeException) cause;             
//			}else if (cause instanceof Error){                 
//				throw (Error) cause;             
//			}else{                 
//				throw new RuntimeException(ite);             
//			}         
//		}catch(IllegalAccessException ie){             
//			return null;         
//		}     
	}
}
