package xechwic.android;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;



/////////////////////////
/////////////////////Usage:
////  First Step 
////
////  Find the other .java files Chinese words.  etc "请选择..." in FriendManGroup.java .
///   Change it to be     xechwic.android.XWCodeTrans.doTrans("请选择...")
////                   ie    add
////                     xechwic.android.XWCodeTrans.doTrans(  prior the "请选择..." ,
////                       and 
////                      )  after the "请选择..."
//////////////////////////////////////////
////
////   Second Step
////  Add the  mapping words pair into  this file following  the 
////                   {"请选择...","Please choose"},   
////                     请选择... is the Chinese ,  Please choose is the target English .


public class XWCodeTrans {
	
	/////static public XWCodeTrans XWTrans=null;
	static private Hashtable htTrans=null;
	
	static String sCurLan=""; /////当前语言
	
		/////////////////////////////////说明
	private static String[][] TransDims =
		{
		    
		    {"要翻译的中文","English translated"},         ////////////////////// first is Chinese word, second is English translated
		    
		    /////////////////////////////////////////////////Add your 
		    {"请选择...","Please choose"},   ///////////////////////Example translation pair 
		    /////////////////////////////////// Add other translation mapping pairs from here......
		    {"正在运行","Running"},   
		    {"对方已挂断","Peer hangup"},   		    
		    {"对不起,打开视频失败","Failed to open video"},   		
		    {"数据传输中,请稍候...","Data transferring..."},   	
		    {"电话记录:","Time:"},   			    
		    {"当前余额:","Money:"},   	
		    {"视频发送:","Video:"},   			    
		    {"音频发送:","Audio:"}, 
		    {"音视频设置:","Audio&Video config:"}, 		    
		    {"确定","OK"}, 		   
		    {"取消","Cancel"}, 			    
		    {"是否退出本次会话","Exit talking"}, 
		    {"通讯出错","Communication error"}, 		    
		    {"对方忙","Busy"}, 			 
		    {"对方拒绝","Refused"}, 	
		    {"号码错误","Error number"}, 			    
		    {"余额不足","No enough money"},	
		    {"对方挂断","Peer hangup"},			    
		    {"男","M"},		  
		    {"女","F"},			    
		    {"岁","Years"},		
		    {"请选择分组","Choose group"},			    
		    {"留言:","Notice"},		
		    {"对不起,该用户不接受任何人添加","Refuse to add"},
		    {"对不起,该用户已经是您的好友了","Already added"},		    
		    {"对不起,不能加自己为好友","Can not add yourself"},		
		    
		    
		    {"对不起,不能加自己为好友","Can not add yourself"},	
		    
		    //////////
		    {"请输入分组","GroupBean name"},
		    {"该分组已存在","GroupBean existing"},
		    {"添加失败","Add failed"},		    
		    {"更新失败,请检查网络","Network problem"},	
		    {"信息提示","Information"},	
		    {"确定要删除","To delete"},	
		    {"分组","GroupBean"},
		    {"删除","delete"},		
		    {"取消","Cancel"},			    
		    
		    ///////////////////////////////////////////////////////////////////End. do not modify!!!!
		    {"我的好友","My Friends"},
		    
		    /////sysstatus
		    {"联机","Online"},    
		    {"忙碌","Busy"},  
		    {"马上回来","Come back Soon"},  
		    {"离开","Away"},  		    
		    {"接听电话","Phone"},  
		    {"外出就餐","Lunch"},  		    
		    {"参加会议","Meeting"},  
		    {"其它...","Other"},
		    {"显示为脱机","Display as Offline"},		
		    
		    {"未接通","Unaccepted"},			    
		    {"通话时长","Duration"},	
		    
		    {"给您微信留言","Sent voice message."},
		    
		    {"对方停止","Stopped."},			    
		    {"发送出错","Error."},				    
		    {"文件不存在","File not exits"},	
		    {"文件打开失败","File open failed"},			    
		    {"没有存储卡","No sd card"},			
		    {"源文件目录或是目标文件目录为空","Invalid source path"},	
		    {"源文件不存在","Source file not exists"},			    
		    {"手机","Mobile"},	
		    {"返回根目录..","Return to root dir"},			    
		    {"返回上一层..","Return to parent dir"},		
		    
		    {"有新版本","New version found"},				    
		    {"是否更新?","Download now?"},		
		    
		    
		    {"暂无聊天记录","No record"},		
			{"正在登陆...","Logining..."},	 	  
			{"男","Male"},	
			{"女","Female"},		
			{"请输入","Input"},					
			{"确定","OK"},			
			{"输入不能为空","Can not be empty"},		
			{"取消","Cancel"},	
			{"未知联系人","Unkown name"},				
			
		    ///////////sysstatus input
		    /*{"Online","联机"},    
		    {"Busy","忙碌"},  
		    {"Come back Soon","马上回来"},  
		    {"Away","离开"},  		    
		    {"Phone","接听电话"},  
		    {"Lunch","外出就餐"},  		    
		    {"Meeting","参加会议"},  
		    {"Other","其它..."},
		    {"Display as Offline","显示为脱机"},		*/
		    
	    
		    
		    /////////////////////////////////////////////////
		    /////////////////////////////////////////////////
		    {"",""},		
		    ///////////////////////////////////////////////////////////////////End. do not modify!!!!
		};
	
	
	
	static public void Init()
	{
		htTrans=new Hashtable();
		
		int i=0;
		while (true) 
		{
			if ( ("".equals(TransDims[i][0])) &&  ("".equals(TransDims[i][1])))
				break;
			htTrans.put(TransDims[i][0], TransDims[i][1]);
			
			i++;
		}
	};
	
	
	
	static public String doTrans(String totrans)
	{
		if (totrans==null)
			return "";
		
		if (htTrans==null)
		{
			Init();
		}
		
		try
		{
			///////////////////////如果中文不需转换
			if (XWDataCenter.xwDC.xwApp.getResources().getConfiguration().locale.getCountry().equals("CN"))
			{
				return totrans;
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return totrans;
		}
		
		String sret=null;
		
		try
		{
		sret=(String)htTrans.get(totrans.trim());
		}
		catch(Exception e)
		{
			
		}
		
		if ((sret==null)||(sret.length()==0))
		{
			return totrans;
		}
		else
			return sret;
	};

	
	/////////////////////////2014-06-12
	static public void InitFromAssetsFile()
	{
		boolean bIsInputBegin=false;
		String sLang="";
	    try
		{
	    	sLang=XWDataCenter.xwDC.xwApp.getResources().getConfiguration().locale.getCountry();
	    	if (sLang==null)
	    		sLang="";
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	    
	    //////////////2014-07-29,大写字母
	    sLang=sLang.toUpperCase();
		if (!sCurLan.equals(sLang))  //////重置翻译对
		{
			htTransInput=null;
			htTrans=null;
			sCurLan=sLang;
		}

		if (htTrans==null)
		{
			Init();
		}
		if (htTransInput==null)
		{
			InitInput();
		}		
		
		//////android.util.Log.v("XIM","multilanguae InitFromAssetsFile 2");
		if ((htTrans==null)  || (htTransInput==null))
			return;
		
		///////android.util.Log.v("XIM","multilanguae InitFromAssetsFile 3");
		
		//////////////
		String sLanguageAssetFile="";
		try
		{
			sLanguageAssetFile="Languages_"+XWDataCenter.xwDC.xwApp.getResources().getConfiguration().locale.getCountry().toUpperCase()+".txt";
			InputStream is=null;
			
			try
			{
				////中文环境使用默认配置  yangj20160709
				if(!sLanguageAssetFile.equals("Languages_CN.txt"))
				is=XWDataCenter.xwDC.xwApp.getResources().getAssets().open(sLanguageAssetFile);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				is=null;
			}
			
			if (is==null) ////文件不存在,则默认使用英语
			{				
				/*if ("HK".equals(sCurLan))  //////除了香港，也用台湾语言
					sLanguageAssetFile="Languages_"+"TW"+".txt";
				else*/
				    sLanguageAssetFile="Languages_"+"EN"+".txt";
				
				try
				{
					is=XWDataCenter.xwDC.xwApp.getResources().getAssets().open(sLanguageAssetFile);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
					is=null;
				}
			}
			
			if (is==null)
				return;			
			
			InputStreamReader inputReader = new InputStreamReader(is ,"UTF-8"); 
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line="";
            String Result="";
            boolean bFileBegin=true;
	        while((line = bufReader.readLine()) != null)
	        {
	        	  String param="",value="";
	        	  
	        	  if (bFileBegin && line.length()>3 &&  line.substring(0, 3).equals("\0xef\0xbb\0xbf")) ////去除utf-8文件头
	        	  {
	        		  line=line.substring(3);
	        		  /////Log.v("XIM","InitFromAssetsFile remove utf-8 head");
	        	  }
	        	  bFileBegin=false;
	        	  
	        	 //// android.util.Log.v("XIM","multilanguae InitFromAssetsFile "+line);
	        	  
	        	  if (!bIsInputBegin)
	        	  {
	        	      if (line.indexOf("[Translation_Input]")>=0)  //////后面是对输入进行翻译!
	        	      {
	        	    	  bIsInputBegin=true;
	        	    	  
	        	    	  /////Log.v("XIM","[Translation_Input] found.");
	        	    	  continue;
	        	      }
	        	  }
	        	    
	        	  int ipos=line.indexOf("=");
	        	  if (ipos>0)
	        	  {
	        		  param=line.substring(0, ipos).trim();
	        		  value=line.substring(ipos+1).trim();
	        		  
	        		  ///////Log.v("XIM","XWCodeTrans:"+param+" "+value);
	        		  
	        		  try
	        		  {
	        			  if (!bIsInputBegin)
	        			  {
	        		          htTrans.remove(param);
	        		          htTrans.put(param, value);
	        			  }
	        			  else  //////输入翻译
	        			  {
	        		          htTransInput.remove(param);
	        		          htTransInput.put(param, value);	  
	        		          
		        	    	  /////Log.v("XIM","htTransInput item "+ param+ " "+value);
	        			  }
	        		  }
	        		  catch(Exception ex2)
	        		  {
	        			  
	        		  }
	        	  }
	        };
	        bufReader.close();
	        inputReader.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace(); 
		}
		


		//////////////////////
	};
		
	
	
	
	///////////////////transfer input
	
	/////static public XWCodeTrans XWTrans=null;
	static private Hashtable htTransInput=null;
		/////////////////////////////////说明
	private static String[][] TransDimsInput =
		{
		    ///////////sysstatus input
		    {"Online","联机"},    
		    {"Busy","忙碌"},  
		    {"Come back Soon","马上回来"},  
		    {"Away","离开"},  		    
		    {"Phone","接听电话"},  
		    {"Lunch","外出就餐"},  		    
		    {"Meeting","参加会议"},  
		    {"Other","其它..."},
		    {"Display as Offline","显示为脱机"},		

		    /////////////////////////////////////////////////
		    /////////////////////////////////////////////////
		    {"",""},	
		    ///////////////////////////////////////////////////////////////////End. do not modify!!!!
		};
	
	

	static public void InitInput()
	{
		htTransInput=new Hashtable();
		
		int i=0;
		while (true) 
		{
			if ( ("".equals(TransDimsInput[i][0])) &&  ("".equals(TransDimsInput[i][1])))
				break;
			htTransInput.put(TransDimsInput[i][0], TransDimsInput[i][1]);
			
			i++;
		}
	};
	
	

	static public String doTransInput(String totrans)
	{
		if (totrans==null)
			return "";
		if (htTransInput==null)
		{
			InitInput();
		}
		
		try
		{
			///////////////////////如果中文不需转换
			if (XWDataCenter.xwDC.xwApp.getResources().getConfiguration().locale.getCountry().equals("CN"))
			{
				return totrans;
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return totrans;
		}
		
		String sret=null;
		
		try
		{
		    sret=(String)htTransInput.get(totrans.trim());
		    
			/*int i;
			for (i=0;i<TransDimsInput.length;i++)
			{
				if (totrans.equals(doTrans(TransDimsInput[i][1])))  /////反向翻译成gb中文
				{
					sret=TransDimsInput[i][1];
					break;
				}
			}*/
		}
		catch(Exception e)
		{
			
		}
		
		if ((sret==null)||(sret.length()==0))
		{
			return totrans;
		}
		else
			return sret;
	};	
	

}
