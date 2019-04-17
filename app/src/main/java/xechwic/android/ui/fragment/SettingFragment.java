package xechwic.android.ui.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import ch.ielse.view.SwitchView;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalDialog;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import ydx.securephone.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import xechwic.android.FriendControl;
import xechwic.android.FriendManGroup;
import xechwic.android.FriendNodeInfo;
import xechwic.android.XWCodeTrans;
import xechwic.android.XWDataCenter;
import xechwic.android.XWServices;
import xechwic.android.act.MainApplication;
import xechwic.android.act.ServerConfig;
import xechwic.android.base.BaseLazyFragment;
import xechwic.android.bus.BusProvider;
import xechwic.android.bus.event.FragmentRereshEvent;
import xechwic.android.bus.event.LogoutXWIMEvent;
import xechwic.android.ui.PersonalUI;
import xechwic.android.ui.SelectFriendUI;
import xechwic.android.util.JRSConstants;
import xechwic.android.util.PrefsUtils;
import xechwic.android.util.WebUtil;
import xechwic.android.view.PushSlideSwitchView;
import xechwic.android.view.ToastUtil;

import static ydx.securephone.R.id.layout02;
import static ydx.securephone.R.id.layout04;
import static ydx.securephone.R.id.layout10;
import static ydx.securephone.R.id.ll_read_del;

/**
 * 设置界面
 */
public class SettingFragment extends BaseLazyFragment implements View.OnClickListener {

    @BindView(R.id.layout_personal)
    LinearLayout ll_personal;
    @BindView(R.id.layout01)
    LinearLayout ll_01;
    @BindView(layout02)
    LinearLayout ll_02;

    @BindView(layout04)
    LinearLayout ll_04;
    @BindView(R.id.layout05)
    LinearLayout ll_05;

    @BindView(layout10)
    LinearLayout ll_10;
    @BindView(R.id.btrelogin)
    Button bt_relogin;
    @BindView(R.id.bt01)
    Button bt_close;
    @BindView(ll_read_del)
    LinearLayout ll_read_delete;
    @BindView(R.id.read_switch)
    ch.ielse.view.SwitchView readSwitch;


    @Override
    protected void onCreateViewLazy(Bundle savedInstanceState) {
        super.onCreateViewLazy(savedInstanceState);
        setContentView(R.layout.fragment_setting);
        ButterKnife.bind(this, getContentView());
        initView();

    }

    private void initView() {
        initReadSwitch();
        setListener();
    }
    public void initReadSwitch(){
        if (XWDataCenter.getReadSwitch()) {
            readSwitch.setOpened(true);
        } else {
            readSwitch.setOpened(false);
        }
    }

    public void setReadSwitch() {
        if (XWDataCenter.getReadSwitch()) {
            readSwitch.toggleSwitch(true);
        } else {
            readSwitch.toggleSwitch(false);
        }
    }

    public void setUpSwitchs(){
        /////阅后即焚
        setReadSwitch();


    }

    /**
     * 初始化设置界面
     */
    private void setListener() {
        //ll_read_delete.setOnClickListener(this);
        ll_personal.setOnClickListener(this);
        ll_01.setOnClickListener(this);//状态修改
        ll_02.setOnClickListener(this);//密码修改
        ll_04.setOnClickListener(this);//分组管理
        ll_05.setOnClickListener(this);//我的好友
        ll_10.setOnClickListener(this);//关于
        bt_close.setOnClickListener(this);
        bt_relogin.setOnClickListener(this);
        readSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isOpened = readSwitch.isOpened();
                showReadSwitchDialog();
            }
        });
    }

    @Override
    protected void refresh() {

    }

    String optionSelect = null;
    private void modifyStatus(){
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        final LinearLayout buildLayout = new LinearLayout(mActivity);
        final Spinner spin = new Spinner(mActivity);
        final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(mActivity.getResources().getString(
                R.string.alert_alert_status));
        final EditText edit = new EditText(mActivity);
        edit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});// 限输入10个字符
        ArrayList<String> list = new ArrayList<String>();
        list.add(mActivity.getResources().getString(R.string.status_online)
        );
        list.add(mActivity.getResources().getString(R.string.status_busy));
        list.add(mActivity.getResources().getString(R.string.status_become)
        );
        list.add(mActivity.getResources().getString(R.string.status_leave)
        );
        // list.add("接听电话");
        list.add(mActivity.getResources().getString(R.string.alert_others));
        list.add(mActivity.getResources().getString(
                R.string.status_display_outline));

        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> adpView, View view,
                                       int id, long position) {
                String selected = adpView.getItemAtPosition(id).toString();
                if (selected.equals(getResources().getString(
                        R.string.alert_others))) {

                    spin.setVisibility(View.GONE);
                    edit.setVisibility(View.VISIBLE);

                } else {
                    edit.setText("");
                    optionSelect = selected;
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        ArrayAdapter<String> status_adapter = new ArrayAdapter<>(
                mActivity, android.R.layout.simple_spinner_item, list);
        status_adapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(status_adapter);
        spin.setLayoutParams(layoutParams);
        edit.setLayoutParams(layoutParams);
        buildLayout.setLayoutParams(layoutParams);

        buildLayout.addView(spin);
        buildLayout.addView(edit);
        edit.setVisibility(View.GONE);
        builder.setView(buildLayout);
        edit.setMaxLines(1);
        builder.setPositiveButton(
                getResources().getString(R.string.alert_confirm),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        String addStr = edit.getText().toString();
                        FriendNodeInfo fni = XWDataCenter.xwDC.getFNInfoFromID(XWDataCenter.xwDC.cid);

                        if (fni == null) {
                            dialog.dismiss();
                            return;
                        }

                        //////////optionSelect=xechwic.android.XWCodeTrans.doTransInput(optionSelect);
                        optionSelect = optionSelect.trim();

                        fni.setOnline_status(optionSelect);

                        XWDataCenter.xwDC.sLoginStatus = optionSelect;


                        ////////////////////////////////////
                        {
                            SharedPreferences settings = MainApplication.getInstance().getSharedPreferences(XWDataCenter.PackageName, 0);
                            SharedPreferences.Editor editor = settings.edit();
                            /////////boolean isFirstRun=false;

                            /////if (!settings.getBoolean("FIRST_RUN", false))
                            {//只有第一次安装才创建icon
                                editor.putString("LOGIN_STATUS", optionSelect);
                                ////////isFirstRun=true;
                            }
                            editor.commit();
                        }

                        Log.v("XIM", "SelectFriendUI status optionSelect:" + optionSelect + " " + xechwic.android.XWCodeTrans.doTransInput(optionSelect));

                        // ///////////////2012-03-16,告知底层状态
                        try {
                            XWDataCenter.xwDC.SetLoginStatus((xechwic.android.XWCodeTrans.doTransInput(optionSelect) + "\0")
                                    .getBytes("GBK"));
                        } catch (Exception e) {
                        }

                        try {
                            if ((addStr == null) || (addStr.equals(""))) {
                                XWDataCenter.xwDC.updateFNInfo(XWDataCenter.xwDC.cid, (fni
                                        .getSignName() + "\0")
                                        .getBytes("GBK"), (xechwic.android.XWCodeTrans.doTransInput(fni
                                        .getOnline_status()) + "\0")
                                        .getBytes("GBK"), "\0"
                                        .getBytes("GBK"), "2"
                                        .getBytes("GBK"));
                            } else {
                                XWDataCenter.xwDC.updateFNInfo(XWDataCenter.xwDC.cid, (fni
                                                .getSignName() + "\0")
                                                .getBytes("GBK"),
                                        (edit.getText().toString() + "\0")
                                                .getBytes("GBK"), "\0"
                                                .getBytes("GBK"), "2"
                                                .getBytes("GBK"));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        dialog.dismiss();
                    }
                });
        builder.setNeutralButton(
                getResources().getString(R.string.alert_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {

                        dialog.dismiss();
                    }
                });
        try {
            builder.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void modifyPwdNik(){
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        final LinearLayout buildLayout = new LinearLayout(mActivity);
        final Spinner spin = new Spinner(mActivity);
        final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(mActivity.getResources().getString(
                R.string.menu_update_name_passwd));
        FriendNodeInfo fni = XWDataCenter.xwDC.getFNInfoFromID(XWDataCenter.xwDC.cid);
        if (fni == null)
            return ;
        final TextView signNameText = new TextView(mActivity);
        final TextView passText = new TextView(mActivity);
        final TextView passAgainText = new TextView(mActivity);
        signNameText.setText(mActivity.getResources().getString(
                R.string.alert_new_signName));
        passText.setText(mActivity.getResources().getString(
                R.string.alert_new_passwd));
        passAgainText.setText(mActivity.getResources().getString(
                R.string.alert_new_passwd_again));
        final EditText signNameEdit = new EditText(mActivity);
        final EditText passEdit = new EditText(mActivity);
        final EditText passAgainEdit = new EditText(mActivity);
        signNameEdit.setText(fni.getSignName());
        signNameEdit.setLayoutParams(layoutParams);
        passEdit.setLayoutParams(layoutParams);
        passAgainEdit.setLayoutParams(layoutParams);
        passEdit.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passAgainEdit.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passEdit.setMaxLines(1);
        passAgainEdit.setMaxLines(1);
        signNameEdit.setMaxLines(1);
        ViewGroup.LayoutParams tmpLP = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        signNameText.setLayoutParams(tmpLP);
        passText.setLayoutParams(tmpLP);
        passAgainText.setLayoutParams(tmpLP);
        LinearLayout signNameLayout = new LinearLayout(mActivity);
        signNameLayout.setOrientation(LinearLayout.HORIZONTAL);
        signNameLayout.setLayoutParams(layoutParams);
        LinearLayout passLayout = new LinearLayout(mActivity);
        passLayout.setOrientation(LinearLayout.HORIZONTAL);
        passLayout.setLayoutParams(layoutParams);
        LinearLayout passAgainLayout = new LinearLayout(mActivity);
        passAgainLayout.setOrientation(LinearLayout.HORIZONTAL);
        passAgainLayout.setLayoutParams(layoutParams);
        signNameLayout.addView(signNameText);
        signNameLayout.addView(signNameEdit);
        passLayout.addView(passText);
        passLayout.addView(passEdit);
        passAgainLayout.addView(passAgainText);
        passAgainLayout.addView(passAgainEdit);
        buildLayout.setOrientation(LinearLayout.VERTICAL);

        buildLayout.addView(signNameLayout);
        buildLayout.addView(passLayout);
        buildLayout.addView(passAgainLayout);
        builder.setView(buildLayout);
        builder.setPositiveButton(mActivity.getResources()
                        .getString(R.string.alert_confirm),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {

                        if ((passEdit.getText() != null)
                                && (passAgainEdit.getText() != null)
                                ) {
                            byte nums[] = new byte[33];
                            byte pass[] = new byte[33];

                            int status = XWDataCenter.xwDC.getLoginUser_new(nums, pass);

                            String sUserPass = new String(pass).trim();
                            try {
                                sUserPass = new String(com.example.mcryptolmsdimpl_demo.MainActivity.decrypt_userpassword(XWDataCenter.xwDC.loginName, sUserPass.getBytes("iso-8859-1")), "iso-8859-1");
                            }
                            catch(Exception ex)
                            {

                            }

                            if (passEdit.getText().length() == 0) {
                                passEdit.setText(sUserPass);
                            }
                            if (passAgainEdit.getText().length() == 0) {
                                passAgainEdit.setText(sUserPass);
                            }

                            // Log.e("tag",
                            // passEdit.getText()+" "+passAgainEdit.getText());
                            if (passEdit
                                    .getText()
                                    .toString()
                                    .equals(passAgainEdit.getText()
                                            .toString())) {
                                try {
                                    XWDataCenter.xwDC.changPasswdSignName(XWDataCenter.xwDC.cid,
                                            (XWDataCenter.xwDC.loginName + "\0")
                                                    .getBytes("GBK"),
                                            (signNameEdit.getText() + "\0")
                                                    .getBytes("GBK"),
                                            (passEdit.getText() + "\0")
                                                    .getBytes("GBK"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(mActivity, mActivity.getResources().getString(R.string.alert_password_again),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                        dialog.dismiss();
                    }
                });
        builder.setNeutralButton(mActivity.getResources()
                        .getString(R.string.alert_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {

                        dialog.dismiss();
                    }
                });
        try {
            builder.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() != -1) {
            int tag = v.getId();
            switch (tag) {
                case R.id.layout_personal://个人信息
                    Intent intentP = new Intent();
                    intentP.setClass(getApplicationContext(), PersonalUI.class);
                    startActivity(intentP);
                    break;
                case R.id.layout01:// 状态修改
                    modifyStatus();
                    break;
                case R.id.layout02:// 修改签名、密码
                    modifyPwdNik();
                    break;
                case R.id.layout04: // 分组管理
                    Intent nextPage = new Intent();
                    nextPage.setClass(mActivity, FriendManGroup.class);
                    mActivity.startActivity(nextPage);
                    break;
                case R.id.layout05: // 好友管理
                    startActivity(new Intent(mActivity, SelectFriendUI.class));
                    break;
                case R.id.bt01: // 关闭应用
                    BusProvider.getInstance().post(new LogoutXWIMEvent(1));
                    break;
                case R.id.layout10: /////About menu
                {
                    String url=ServerConfig.XIM_SERVER_HOST;
                    WebUtil.openBrowser(mActivity,url);
                }
                break;
                case R.id.btrelogin:
                {
//                    destroyAccount();
                    BusProvider.getInstance().post(new LogoutXWIMEvent(2));
                }
                break;
                case R.id.ll_read_del:
                {
                   showReadSwitchDialog();
                }
                break;

            }

        }
    }

    private void showScreenSwitchDialog(){
        String title="";
        String content="";
        boolean isread=PrefsUtils.getInstance().get(JRSConstants.KEY_SCREEN_SWITCH,false);
        if(isread){
            title= XWCodeTrans.doTrans("关闭防止休眠");
            content=XWCodeTrans.doTrans("可能会引起通讯中断");
        }else{
            title=XWCodeTrans.doTrans("开启防止休眠");
            content=XWCodeTrans.doTrans("可以保证通讯连接，但会消耗更多电量");
        }
        final NormalDialog normalDialog=new NormalDialog(mActivity);
        normalDialog.setTitle(title);
        normalDialog.content(content);
        normalDialog.btnNum(2).btnText(getResources().getString(R.string.alert_cancel),
                getResources().getString(R.string.alert_confirm));
        normalDialog.setOnBtnClickL(new OnBtnClickL() {
            @Override
            public void onBtnClick() {
                normalDialog.dismiss();
            }
        }, new OnBtnClickL() {
            @Override
            public void onBtnClick() {
                normalDialog.dismiss();
                boolean isCheck=PrefsUtils.getInstance().get(JRSConstants.KEY_SCREEN_SWITCH,false);
                PrefsUtils.getInstance().put(JRSConstants.KEY_SCREEN_SWITCH,!isCheck);
                setUpSwitchs();
            }
        });
        normalDialog.show();
    }

    private void showReadSwitchDialog(){
        String title="";
        String content="";
        boolean isread=XWDataCenter.getReadSwitch();
        if(isread){
            title= XWCodeTrans.doTrans("关闭阅后即焚");
            content=XWCodeTrans.doTrans("信息阅读后不会被销毁");
        }else{
            title=XWCodeTrans.doTrans("开启阅后即焚");
            content=XWCodeTrans.doTrans("信息阅读后会被销毁,包含以前阅读过的信息都会被销毁");
        }
        final NormalDialog normalDialog=new NormalDialog(mActivity);
        normalDialog.setTitle(title);
        normalDialog.content(content);
        normalDialog.btnNum(2).btnText(getResources().getString(R.string.alert_cancel),
                getResources().getString(R.string.alert_confirm));
        normalDialog.setOnBtnClickL(new OnBtnClickL() {
            @Override
            public void onBtnClick() {
                normalDialog.dismiss();
                setReadSwitch();
            }
        }, new OnBtnClickL() {
            @Override
            public void onBtnClick() {
                normalDialog.dismiss();
                toggleReadDel();
            }
        });
        normalDialog.show();
    }


    private void toggleReadDel(){
        XWDataCenter.setReadSwitch(!XWDataCenter.getReadSwitch());
        setReadSwitch();
        if(XWDataCenter.getReadSwitch()){
            ToastUtil.getInstance(mActivity).show(XWCodeTrans.doTrans("启动阅后即焚"));
            new AsyncTask<String,Integer,String>(){
                @Override
                protected void onPreExecute() {
                    ((FriendControl)mActivity).showPlg("");
                }

                @Override
                protected void onPostExecute(String s) {
                    ((FriendControl)mActivity).disPlg();
                    //启动阅后即焚轮询任务
                    Intent intentService=new Intent(mActivity,XWServices.class);
                    intentService.setAction(JRSConstants.CMD_ACTION_SNAPCHAT);
                    mActivity.startService(intentService);
                }

                @Override
                protected String doInBackground(String... strings) {
                    long lst=System.currentTimeMillis();
                    try{
                        XWDataCenter.clearALLReadFile();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute("");

        }else{

            ToastUtil.getInstance(mActivity).show(XWCodeTrans.doTrans("取消阅后即焚"));
            //关闭阅后即焚轮询任务
            Intent intentService=new Intent(mActivity,XWServices.class);
            intentService.setAction(JRSConstants.CMD_ACTION_STOPSNAPCHAT);
            mActivity.startService(intentService);
        }
    }

    @Override
    protected void onPauseLazy() {
        super.onPauseLazy();
    }

    @Override
    public void onDestroyViewLazy() {
        super.onDestroyViewLazy();
    }

    @Subscribe
    public void onFragmentRereshEvent(FragmentRereshEvent event){
        if(event!=null&&event.type==FriendControl.SYS_SETTING){
            postRefresh();
        }

    }
}
