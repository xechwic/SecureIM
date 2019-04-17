package xechwic.android.bean;

import java.util.List;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;


/**
 * bean操作类
 *
 */
public class BeanOperate {

	private static String TAG=BeanOperate.class.getSimpleName();

	/**根据json获取头像list
	 * @param content
	 * @return
	 */
	public static List<HeadBean> getHeadBeanList(String content){
		if(content==null||content.length()<1){
			return null;
		}
		List<HeadBean> list=null;
//		try {
//			JSONObject jsonObj = new JSONObject(content);
//			if(jsonObj!=null){
//				JSONArray jsonArray = jsonObj.getJSONArray(HeadBean.data);
//				if(jsonArray!=null){
//					jsonObj.
//				}
//			}
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		//content json格式化
		if(content.contains("<script>")){
			String temp=content.substring(content.indexOf("<script>"));
			content=content.replace(temp, "");
			Log.e(TAG,"content:"+content+",to json:"+content);
		}
		Gson gson = new Gson();
		JsonParser parser = new JsonParser();
		JsonObject jsonObject = parser.parse(content).getAsJsonObject();
		JsonArray jsonArray = jsonObject.getAsJsonArray(HeadBean.data);
		if (jsonArray != null) {
			Log.e(TAG, "jsonArray size:"+jsonArray.size());
			list = gson.fromJson(jsonArray, new TypeToken<List<HeadBean>>(){}.getType());
			if(list!=null){
				Log.e(TAG, "jsonArray to object:"+list.size());
			}
			
		}else{
			Log.e(TAG,"jsonArray is null:"+content);
		}
		return list;
	}
}
