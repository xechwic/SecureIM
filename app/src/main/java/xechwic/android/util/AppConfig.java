package xechwic.android.util;
/**
 * 
 *配置类
 */
public class AppConfig {


	private static final int DEBUG=0;//应用调试版本
	private static final int RELEASE=1;  //正式版(默认设置）
	private static final int RELEASE_TF=2;  //正式版（TF加密版）
	private static final int RELEASE_NOTF=3;  //正式版（NO TF加密）
	private static final int SETUP_TEST_TF=4;//安装测试版(有TF）
	private static final int SETUP_TEST_NOTF=5;//安装测试版(没TF）

	/////应用默认设置
	public static boolean SD_ENCODE=true;//SD卡加密,true为要加密卡,false不用
	public static boolean IP_CONFIG=false;//IP可配置
	public static boolean REG_CONFIG=true;//可注册
	public static boolean UPDATE_CONFIG=true;//是否可升级


/////////////////////////
	public static final int APP_TYPE=RELEASE_NOTF; //当前应用类型（需要根据不同版本设置）


	public  static void init(){
		init(APP_TYPE);
	}

	////初始化版本设置（根据版本进行不同配置)
	public  static void init(int apptype){
		switch (apptype){
			case DEBUG:///调试
				SD_ENCODE=false;
				IP_CONFIG=true;
				UPDATE_CONFIG=true;
				REG_CONFIG=true;
				break;
			case RELEASE:///正式版（默认设置）
				SD_ENCODE=true;
				IP_CONFIG=false;
				UPDATE_CONFIG=true;
				REG_CONFIG=true;
				break;
			case RELEASE_TF:///正式版（TF加密）
				SD_ENCODE=true;
				IP_CONFIG=false;
				UPDATE_CONFIG=true;
				REG_CONFIG=true;
				break;
			case RELEASE_NOTF:///正式版（NO TF加密）
				SD_ENCODE=false;
				IP_CONFIG=false;
				UPDATE_CONFIG=true;
				REG_CONFIG=true;
				break;
			case SETUP_TEST_TF:////安装测试(有TF)
				SD_ENCODE=true;
				IP_CONFIG=true;
				UPDATE_CONFIG=true;
				REG_CONFIG=false;
				break;
			case SETUP_TEST_NOTF:////安装测试(没TF）
				SD_ENCODE=false;
				IP_CONFIG=true;
				UPDATE_CONFIG=true;
				REG_CONFIG=false;
				break;
		}
	}

	/**
	 * 项目类型
	 *
	 */
	private static Version version =Version.TW;
	
	public enum Version{
		TW,//台湾项目
		BJ,//北京项目
	}

	public static Version getVersion() {
		return version;
	}

	public static void setVersion(Version version) {
		AppConfig.version = version;
	}
	
	
}
