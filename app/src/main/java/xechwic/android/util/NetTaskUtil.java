package xechwic.android.util;

import com.lzy.okgo.OkGo;

import okhttp3.Response;
import xechwic.android.act.ServerConfig;

/**
 * 网络任务
 * @author luman
 *
 */
public class NetTaskUtil {



	/**以get方式同步获取接口数据
	 */
	public static String getDataTaskSync(final String url){
		if(url==null){
			return null;
		}
		try
		{
			///////////2017-12-11,使用https替换http!!!!!!!!!!
			String httpsurl=url.replace("http://","https://");
			httpsurl=httpsurl.replace(":"+ ServerConfig.XIM_SERVER_PORT,"");

			////android.util.Log.e("NetTaskUtil","OkGo.get:"+httpsurl);
			Response response = OkGo.get(httpsurl)
					.setCertificates()
					.tag("okgo")//
					.execute();
			if(response!=null&&response.body()!=null){
				String ret= new String(response.body().bytes(),"gbk");
				/////android.util.Log.e("NetTaskUtil","OkGo.get ret:"+ret);
				return ret;
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();

		}
		return null;
	}

}
