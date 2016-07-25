package cn.kuwo.kwmusiccar;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.widget.ImageView;
import cn.kuwo.base.bean.Music;
import cn.kuwo.base.bean.MusicList;
import cn.kuwo.base.config.ConfMgr;
import cn.kuwo.base.db.DataBaseManager;
import cn.kuwo.base.log.LogDef.LogType;
import cn.kuwo.base.log.LogMgr;
import cn.kuwo.base.log.ServiceLevelLogger;
import cn.kuwo.base.util.DeviceUtils;
import cn.kuwo.base.util.DirUtils;
import cn.kuwo.base.util.JumperUtils;
import cn.kuwo.base.util.KitkatPathUtils;
import cn.kuwo.base.util.KwFileUtils;
import cn.kuwo.base.util.NetworkStateUtil;
import cn.kuwo.base.util.ToastUtil;
import cn.kuwo.base.util.Umeng;
import cn.kuwo.core.messagemgr.MessageID;
import cn.kuwo.core.messagemgr.MessageManager;
import cn.kuwo.core.messagemgr.MessageManager.Caller;
import cn.kuwo.core.messagemgr.MessageManager.Runner;
import cn.kuwo.core.modulemgr.ModMgr;
import cn.kuwo.core.observers.IAppObserver;
import cn.kuwo.core.observers.IConfigMgrObserver;
import cn.kuwo.core.observers.ILyricsObserver;
import cn.kuwo.core.observers.IMainLayoutObserver;
import cn.kuwo.core.observers.ISkinManagerObserver;
import cn.kuwo.core.observers.IUserPicMgrObserver;
import cn.kuwo.mod.localmgr.ILocalMgr;
import cn.kuwo.mod.lyric.ILyricsMgr;
import cn.kuwo.mod.playcontrol.HeadsetControlReceiver;
import cn.kuwo.mod.playcontrol.IPlayControl;
import cn.kuwo.mod.push.IPushMgr;
import cn.kuwo.mod.push.PushHandler;
import cn.kuwo.service.MainService;
import cn.kuwo.service.PlayProxy.Status;
import cn.kuwo.ui.fragment.FragmentCtroller;
import cn.kuwo.ui.fragment.MVFragment;
import cn.kuwo.ui.fragment.dialog.BaseDialogFragment;
import cn.kuwo.ui.fragment.dialog.BaseDialogFragment.OnClickListener;
import cn.kuwo.ui.fragment.dialog.DialogFragmentUtils;
import cn.kuwo.ui.mine.MineUserInfo;
import cn.kuwo.ui.nowplaying.NowplayingController;
import cn.kuwo.ui.skin.CropImageActivity;
import cn.kuwo.ui.topbar.TopBarController;
import cn.kuwo.ui.userinfo.SsoFactory;
import com.umeng.analytics.MobclickAgent;
import com.weibo.sdk.android.sso.SsoHandler;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.Stack;

public class MainActivity
  extends FragmentActivity
  implements IMainLayoutObserver, IAppObserver, ISkinManagerObserver
{
  private static String TAG = "MainActivity";
  public static final int VOICE_SEARCH = 8298;
  private static MainActivity _INSTANCE = null;
  private IConfigMgrObserver configMgrObserver = new IConfigMgrObserver()
  {
    public void IConfigMgrObserver_ItemChanged(final String paramAnonymousString1, String paramAnonymousString2)
    {
      if ("key_pref_background".equals(paramAnonymousString2))
      {
        paramAnonymousString1 = null;
        boolean bool1 = ConfMgr.getBoolValue("", "key_pref_background", false);
        boolean bool2 = ConfMgr.getBoolValue("", "backgroundChange", true);
        if (bool1)
        {
          if ((MainActivity.this.topBarController != null) && (MainActivity.this.topBarController.getCurrentTab() == TopBarController.Tab_Lyric) && (bool2)) {
            ModMgr.getLyricsMgr().startBackgroundLoop();
          }
          if (ModMgr.getPlayControl().getNowPlayingMusic() != null) {
            paramAnonymousString1 = ModMgr.getLyricsMgr().getBackgroundPic();
          }
          MessageManager.getInstance().syncNotify(MessageID.OBSERVER_LYRICS, new MessageManager.Caller()
          {
            public void call()
            {
              ((ILyricsObserver)this.ob).ILyricObserver_BackgroundPic_Changed(paramAnonymousString1, false, false);
            }
          });
        }
      }
      do
      {
        do
        {
          return;
          ModMgr.getLyricsMgr().stopBackgroundLoop();
          break;
        } while (!"backgroundChange".equals(paramAnonymousString2));
        if (!ConfMgr.getBoolValue("", "backgroundChange", true)) {
          break label178;
        }
      } while ((MainActivity.this.topBarController == null) || (MainActivity.this.topBarController.getCurrentTab() != TopBarController.Tab_Lyric));
      ModMgr.getLyricsMgr().startBackgroundLoop();
      return;
      label178:
      ModMgr.getLyricsMgr().stopBackgroundLoop();
    }
    
    public void IConfigMgrObserver_RealTimeSave() {}
    
    public void IConfigMgrObserver_UpdateFinish(boolean paramAnonymousBoolean) {}
  };
  IntentFilter filter = null;
  HeadsetControlReceiver headsetPlugReceiver = new HeadsetControlReceiver();
  private FragmentCtroller mFragmentCtroller = null;
  private Intent mIntent = null;
  private ImageView mSkinView = null;
  private NowplayingController nowplayingController = null;
  private TopBarController topBarController = null;
  private PowerManager.WakeLock wakeLock = null;
  
  private void createController()
  {
    if (this.mFragmentCtroller == null) {
      this.mFragmentCtroller = new FragmentCtroller();
    }
    if (this.topBarController == null) {
      this.topBarController = new TopBarController(this);
    }
    if (this.nowplayingController == null) {
      this.nowplayingController = new NowplayingController(this);
    }
  }
  
  public static MainActivity getInstance()
  {
    return _INSTANCE;
  }
  
  private void init()
  {
    MessageManager.getInstance().attachMessage(MessageID.OBSERVER_APP, this);
    MessageManager.getInstance().attachMessage(MessageID.OBSERVER_MAINLAYOUT, this);
    MessageManager.getInstance().attachMessage(MessageID.OBSERVER_SKINMANAGER, this);
    MessageManager.getInstance().attachMessage(MessageID.OBSERVER_CONF, this.configMgrObserver);
    setVolumeControlStream(3);
    initController();
    MineUserInfo.InitAutoLogin();
    ModMgr.getPushMgr();
    ModMgr.getPushMgr().startPushService(this);
  }
  
  private void initController()
  {
    createController();
    MessageManager.getInstance().asyncRun(3000, new MessageManager.Runner()
    {
      public void call()
      {
        if (!NetworkStateUtil.isAvaliable()) {
          ToastUtil.show("??????????????????????????????~");
        }
      }
    });
  }
  
  private boolean isV5System()
  {
    Object localObject = new Properties();
    try
    {
      ((Properties)localObject).load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
      localObject = ((Properties)localObject).getProperty("ro.miui.ui.version.name");
      if ((localObject != null) && (((String)localObject).contains("V"))) {
        localObject = ((String)localObject).replace("V", "");
      }
    }
    catch (Exception localException)
    {
      for (;;)
      {
        try
        {
          int i = Integer.parseInt((String)localObject);
          if (i <= 4) {
            break;
          }
          return true;
        }
        catch (NumberFormatException localNumberFormatException) {}
        localException = localException;
        localException.printStackTrace();
      }
    }
    return false;
  }
  
  private void processIntent(Intent paramIntent)
  {
    if (paramIntent == null) {
      return;
    }
    if (processWidgetAction(paramIntent))
    {
      this.mIntent = null;
      return;
    }
    if (processPushCommand(paramIntent))
    {
      this.mIntent = null;
      return;
    }
    if (KwCarPlay.handleCarPlay(paramIntent))
    {
      this.mIntent = null;
      return;
    }
    processUri(paramIntent.getData());
    this.mIntent = null;
  }
  
  private boolean processPushCommand(Intent paramIntent)
  {
    LogMgr.d("push", "processPushCommand");
    if (paramIntent == null) {}
    boolean bool;
    do
    {
      return false;
      bool = paramIntent.getBooleanExtra("cn.kuwo.kwmusiccar.PushHandler", false);
      LogMgr.d("push", "pushKey: " + bool);
    } while (!bool);
    long l = paramIntent.getLongExtra("PushHandler.pushid", 0L);
    int i = paramIntent.getIntExtra("PushHandler.type", 0);
    String str1 = paramIntent.getStringExtra("PushHandler.title");
    String str2 = paramIntent.getStringExtra("PushHandler.content");
    paramIntent.putExtra("cn.kuwo.kwmusiccar.PushHandler", false);
    LogMgr.d("push", "ys:|pushID=" + l + " pushType=" + i + " pushTitle=" + str1 + " pushContent=" + str2);
    PushHandler.sendPushLog("click", 0, l, null);
    switch (i)
    {
    }
    for (;;)
    {
      Umeng.onPushNotificationClickEVENT(this);
      LogMgr.i("MainActivity", "PushCommand??" + l);
      return true;
      if ((!URLUtil.isHttpUrl(str2)) && (!URLUtil.isHttpsUrl(str2)))
      {
        LogMgr.d("push", "pushContent:" + str2);
        return false;
      }
      JumperUtils.JumpToWebView(str2);
      continue;
      if ((!URLUtil.isHttpUrl(str2)) && (!URLUtil.isHttpsUrl(str2)))
      {
        LogMgr.d("push", "pushContent:" + str2);
        return false;
      }
      JumperUtils.JumpToDefaultWeb(str2);
      continue;
      JumperUtils.JumpToQukuSongList(str2, str1, null);
      continue;
      JumperUtils.JumpToQukuAlbum(str2, str1, null);
    }
  }
  
  private boolean processWidgetAction(Intent paramIntent)
  {
    int i = 0;
    paramIntent = paramIntent.getStringExtra("widget_action");
    if (paramIntent == null) {
      return false;
    }
    IPlayControl localIPlayControl = ModMgr.getPlayControl();
    int j;
    if (paramIntent.equals("cn.kuwo.playercar.CHANGE_MODE"))
    {
      j = ModMgr.getPlayControl().getPlayMode() + 1;
      if (j < 5) {
        break label219;
      }
    }
    for (;;)
    {
      localIPlayControl.setPlayMode(i);
      return true;
      if ((!paramIntent.equals("cn.kuwo.playercar.TOGGLE_PAUSE")) && (!paramIntent.equals("cn.kuwo.playercar.NEXT")) && (!paramIntent.equals("cn.kuwo.playercar.PREV"))) {
        break;
      }
      if (localIPlayControl.getNowPlayingList() == null)
      {
        if ((ModMgr.getLocalMgr().getAllMusics() != null) && (ModMgr.getLocalMgr().getAllMusics().size() > 0)) {
          localIPlayControl.play(ModMgr.getLocalMgr().getAllMusics(), 0);
        }
        return true;
      }
      if (paramIntent.equals("cn.kuwo.playercar.TOGGLE_PAUSE")) {
        if (localIPlayControl.getStatus() == PlayProxy.Status.PLAYING) {
          localIPlayControl.pause();
        }
      }
      for (;;)
      {
        return true;
        localIPlayControl.continuePlay();
        continue;
        if (paramIntent.equals("cn.kuwo.playercar.NEXT")) {
          localIPlayControl.playNext();
        } else if (paramIntent.equals("cn.kuwo.playercar.PREV")) {
          localIPlayControl.playPre();
        }
      }
      label219:
      i = j;
    }
  }
  
  private void registerHeadsetPlugReceiver(Context paramContext)
  {
    if (this.filter == null)
    {
      this.filter = new IntentFilter();
      this.filter.addAction("android.intent.action.HEADSET_PLUG");
      this.filter.addAction("android.intent.action.MEDIA_BUTTON");
      this.filter.setPriority(Integer.MAX_VALUE);
    }
    try
    {
      paramContext.registerReceiver(this.headsetPlugReceiver, this.filter);
      return;
    }
    catch (Throwable paramContext) {}
  }
  
  private void release()
  {
    MessageManager.getInstance().detachMessage(MessageID.OBSERVER_APP, this);
    MessageManager.getInstance().detachMessage(MessageID.OBSERVER_MAINLAYOUT, this);
    MessageManager.getInstance().detachMessage(MessageID.OBSERVER_SKINMANAGER, this);
    MessageManager.getInstance().detachMessage(MessageID.OBSERVER_CONF, this.configMgrObserver);
    if (this.topBarController != null)
    {
      this.topBarController.release();
      this.topBarController = null;
    }
    if (this.nowplayingController != null)
    {
      this.nowplayingController.release();
      this.nowplayingController = null;
    }
  }
  
  private void resumeSaveInstanceState()
  {
    DeviceUtils.init(this);
    App.fetchAppUid();
    DataBaseManager.init(this);
    App.initModMgr(false);
    MainService.connect();
  }
  
  private void setScreenAlwaysON(boolean paramBoolean)
  {
    if (paramBoolean) {
      if (this.wakeLock == null)
      {
        this.wakeLock = ((PowerManager)getSystemService("power")).newWakeLock(536870922, getClass().getName());
        this.wakeLock.acquire();
        LogMgr.d(TAG, "????????????????????");
      }
    }
    while ((this.wakeLock == null) || (!this.wakeLock.isHeld())) {
      return;
    }
    try
    {
      this.wakeLock.release();
      this.wakeLock = null;
      LogMgr.d(TAG, "????????????");
      return;
    }
    catch (Exception localException)
    {
      for (;;)
      {
        localException.printStackTrace();
      }
    }
  }
  
  private void unregisterHeadsetPlugReceiver(Context paramContext)
  {
    if (this.filter != null) {
      paramContext.unregisterReceiver(this.headsetPlugReceiver);
    }
  }
  
  public void IAppObserver_InitFinished()
  {
    WelcomeActivity.initFinished = true;
  }
  
  public void IAppObserver_NetworkStateChanged(boolean paramBoolean1, boolean paramBoolean2)
  {
    if (!paramBoolean1) {
      ToastUtil.show("????????????????????????????????????");
    }
  }
  
  public void IAppObserver_OnBackground() {}
  
  public void IAppObserver_OnForground() {}
  
  public void IAppObserver_OnLowMemory() {}
  
  public void IAppObserver_OnNowplayingShow(boolean paramBoolean) {}
  
  public void IAppObserver_OnUpdateDatabase() {}
  
  public void IAppObserver_PrepareExitApp()
  {
    KwFileUtils.deleteFile(DirUtils.getDirectory(14));
    finish();
  }
  
  public void IAppObserver_SDCardStateChanged(boolean paramBoolean)
  {
    if (paramBoolean)
    {
      ToastUtil.show("????????????????...");
      MessageManager.getInstance().asyncRun(300, new MessageManager.Runner()
      {
        public void call()
        {
          ModMgr.getLocalMgr().autoScan();
        }
      });
    }
  }
  
  public void IAppObserver_WelcomePageDisappear() {}
  
  public void IMainLayouOb_NoEnoughSDSpace()
  {
    ToastUtil.show("????????????????????????????????");
  }
  
  public void IMainLayoutOb_ChangeFloatState(boolean paramBoolean) {}
  
  public void IMainLayoutOb_HasNoCopyright()
  {
    ToastUtil.show("????????????????????????????????????????????????????????????");
  }
  
  public void IMainLayoutOb_HeaderPicClick() {}
  
  public void IMainLayoutOb_ListSelect(String paramString) {}
  
  public void IMainLayoutOb_PopNowplayFloatView(Boolean paramBoolean) {}
  
  public void IMainLayoutOb_TabClick(int paramInt1, int paramInt2)
  {
    if (paramInt1 != paramInt2)
    {
      if (paramInt1 == TopBarController.Tab_Lyric)
      {
        setScreenAlwaysON(true);
        ModMgr.getLyricsMgr().startBackgroundLoop();
      }
    }
    else {
      return;
    }
    setScreenAlwaysON(false);
    ModMgr.getLyricsMgr().stopBackgroundLoop();
  }
  
  public void ISkinManagerOb_AddSkin() {}
  
  public void ISkinManagerOb_ChangeSkin(int paramInt) {}
  
  public void ISkinManagerOb_DeleteSkin() {}
  
  public void cropHeadImage(Uri paramUri, int paramInt1, int paramInt2, int paramInt3)
  {
    Intent localIntent = new Intent(this, CropImageActivity.class);
    localIntent.setDataAndType(paramUri, "image/*");
    localIntent.putExtra("outputX", paramInt1);
    localIntent.putExtra("outputY", paramInt2);
    localIntent.putExtra("rotateEnable", true);
    localIntent.putExtra("return-data", true);
    startActivityForResult(localIntent, paramInt3);
  }
  
  public FragmentCtroller getFragmentCtroller()
  {
    return this.mFragmentCtroller;
  }
  
  public TopBarController getTopBarController()
  {
    return this.topBarController;
  }
  
  protected void onActivityResult(int paramInt1, int paramInt2, Intent paramIntent)
  {
    final Object localObject2 = null;
    Object localObject1 = null;
    super.onActivityResult(paramInt1, paramInt2, paramIntent);
    App.setForceForground(this, false);
    if (paramInt1 == 8298) {
      if ((paramInt2 == 10001) && (paramIntent != null))
      {
        paramIntent = paramIntent.getStringExtra("firstSrResult");
        if (TextUtils.isEmpty(paramIntent)) {
          break label60;
        }
        this.topBarController.search(paramIntent);
      }
    }
    label60:
    do
    {
      do
      {
        return;
        ToastUtil.show("????????");
        return;
        if (paramInt1 != 32973) {
          break;
        }
      } while (SsoFactory.getSsoInstance() == null);
      SsoFactory.getSsoInstance().authorizeCallBack(paramInt1, paramInt2, paramIntent);
      return;
    } while (((paramInt1 >= 5656) && (paramInt1 <= 6656)) || (paramInt2 != -1));
    switch (paramInt1)
    {
    default: 
      return;
    case 19: 
      if (paramIntent != null)
      {
        localObject2 = KitkatPathUtils.getPath(this, paramIntent.getData());
        paramIntent = (Intent)localObject1;
        if (!TextUtils.isEmpty((CharSequence)localObject2)) {
          LogMgr.d("ajh.test", "file: " + (String)localObject2);
        }
      }
      for (paramIntent = Uri.fromFile(new File((String)localObject2));; paramIntent = Uri.fromFile(new File(DirUtils.getDirectory(14), paramIntent)))
      {
        localObject1 = new DisplayMetrics();
        getInstance().getWindowManager().getDefaultDisplay().getMetrics((DisplayMetrics)localObject1);
        if (paramIntent != null) {
          break;
        }
        ToastUtil.show("??????????????????????????????????");
        return;
        paramIntent = ConfMgr.getStringValue("", "pic_temp_mine_menu", "temp");
      }
      cropHeadImage(paramIntent, 300, 300, 20);
      return;
    }
    Uri localUri = paramIntent.getData();
    localObject1 = localObject2;
    if (localUri != null) {
      localObject1 = BitmapFactory.decodeFile(localUri.getPath());
    }
    localObject2 = localObject1;
    if (localObject1 == null)
    {
      paramIntent = paramIntent.getExtras();
      localObject2 = localObject1;
      if (paramIntent != null)
      {
        localObject2 = (Bitmap)paramIntent.get("data");
        paramIntent = new ByteArrayOutputStream();
        ((Bitmap)localObject2).compress(Bitmap.CompressFormat.JPEG, 100, paramIntent);
      }
    }
    MessageManager.getInstance().syncNotify(MessageID.OBSERVER_USERPIC, new MessageManager.Caller()
    {
      public void call()
      {
        ((IUserPicMgrObserver)this.ob).IUserPicMgrObserver_Changed(true, localObject2);
      }
    });
  }
  
  public void onBack()
  {
    this.topBarController.clearFocus();
    Object localObject;
    if (this.topBarController.getCurrentTab() == TopBarController.Tab_MyMusic) {
      localObject = this.mFragmentCtroller.getMineTagStack();
    }
    for (;;)
    {
      if ((localObject != null) && (((Stack)localObject).size() > 1))
      {
        String str = (String)((Stack)localObject).pop();
        localObject = (String)((Stack)localObject).peek();
        this.mFragmentCtroller.removeFragment(getSupportFragmentManager(), str);
        LogMgr.i("LocalBaseFragment", "removeFragment:1111");
        this.mFragmentCtroller.showFragment(getSupportFragmentManager(), (String)localObject);
        LogMgr.i("LocalBaseFragment", "removeFragment:2222");
        return;
        if (this.topBarController.getCurrentTab() == TopBarController.Tab_MusicLib) {
          localObject = this.mFragmentCtroller.getQukuTagStack();
        }
      }
      else
      {
        moveTaskToBack(true);
        return;
      }
      localObject = null;
    }
  }
  
  @TargetApi(19)
  protected void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    resumeSaveInstanceState();
    setContentView(2130903043);
    KwCarPlay.init(this);
    this.mSkinView = ((ImageView)findViewById(2131427350));
    _INSTANCE = this;
    init();
    this.mIntent = getIntent();
    if ((DeviceUtils.FIRST_INSTALL) && (!ConfMgr.getBoolValue("", "desk_lrc_v5_tip", false)) && (isV5System())) {
      DialogFragmentUtils.showV5TipDialog(true);
    }
    MessageManager.getInstance().asyncRun(300, new MessageManager.Runner()
    {
      public void call()
      {
        ModMgr.getLocalMgr().autoScan();
      }
    });
  }
  
  protected void onDestroy()
  {
    release();
    KwCarPlay.release(this);
    unregisterHeadsetPlugReceiver(this);
    registerHeadsetPlugReceiver(App.getInstance());
    super.onDestroy();
  }
  
  public boolean onKeyDown(int paramInt, KeyEvent paramKeyEvent)
  {
    Fragment localFragment = this.mFragmentCtroller.getFragmentByTag(getSupportFragmentManager(), "mv_fragment");
    if ((localFragment != null) && ((localFragment instanceof MVFragment))) {
      return ((MVFragment)localFragment).onKeyDown(paramInt, paramKeyEvent);
    }
    if (paramInt == 4)
    {
      onBack();
      return true;
    }
    return super.onKeyDown(paramInt, paramKeyEvent);
  }
  
  protected void onNewIntent(Intent paramIntent)
  {
    super.onNewIntent(paramIntent);
    this.mIntent = paramIntent;
  }
  
  protected void onPause()
  {
    super.onPause();
    MobclickAgent.onPause(this);
  }
  
  protected void onResume()
  {
    super.onResume();
    MobclickAgent.onResume(this);
    registerHeadsetPlugReceiver(this);
    if (ConfMgr.getBoolValue("", "manual_earphone_wire_control", true)) {
      HeadsetControlReceiver.enable(this);
    }
    if ((this.topBarController != null) && (this.topBarController.getCurrentTab() == TopBarController.Tab_Lyric))
    {
      setScreenAlwaysON(true);
      ModMgr.getLyricsMgr().startBackgroundLoop();
    }
    App.updateForgroundState();
    if (this.nowplayingController != null) {
      this.nowplayingController.onResume();
    }
    processIntent(this.mIntent);
    if (!DeviceUtils.START_LOG_SENDED)
    {
      DeviceUtils.START_LOG_SENDED = true;
      long l = System.currentTimeMillis() - App.START_TIME;
      LogMgr.i("appstart", "start??" + App.START_TIME + "time:" + l);
      String str = "STARTTM:" + l;
      ServiceLevelLogger.sendLog(LogDef.LogType.AppStart.name(), str, 0);
    }
  }
  
  protected void onSaveInstanceState(Bundle paramBundle) {}
  
  protected void onStop()
  {
    setScreenAlwaysON(false);
    super.onStop();
    App.updateForgroundState();
    if (this.nowplayingController != null)
    {
      this.nowplayingController.onPause();
      ModMgr.getLyricsMgr().stopBackgroundLoop();
    }
  }
  
  public void processUri(Uri paramUri)
  {
    Object localObject1 = null;
    if (paramUri == null) {
      return;
    }
    Object localObject2 = paramUri.getScheme();
    if ("file".equals(localObject2)) {
      paramUri = paramUri.getPath();
    }
    for (;;)
    {
      LogMgr.d(TAG, "path: " + paramUri);
      if (TextUtils.isEmpty(paramUri)) {
        break;
      }
      localObject1 = paramUri;
      if (paramUri.startsWith("/sdcard"))
      {
        localObject2 = Environment.getExternalStorageDirectory().getPath();
        localObject1 = paramUri;
        if (!((String)localObject2).equalsIgnoreCase("/sdcard")) {
          localObject1 = paramUri.replace("/sdcard", (CharSequence)localObject2);
        }
      }
      if (this.mFragmentCtroller != null) {
        this.mFragmentCtroller.removeFragment(getSupportFragmentManager(), "mv_fragment");
      }
      paramUri = Uri.parse((String)localObject1);
      ModMgr.getLocalMgr().addAndPlayUri(paramUri);
      if (this.topBarController == null) {
        break;
      }
      LogMgr.i("UI", "processData:");
      this.topBarController.setSelectedTab(TopBarController.Tab_Lyric);
      return;
      if ("content".equals(localObject2))
      {
        localObject2 = getContentResolver().query(paramUri, new String[] { "_data" }, null, null, null);
        if (localObject2 == null) {
          break;
        }
        if ((((Cursor)localObject2).getCount() <= 0) && (Build.VERSION.SDK_INT < 14))
        {
          ((Cursor)localObject2).close();
          return;
        }
        if (((Cursor)localObject2).moveToNext()) {
          localObject1 = ((Cursor)localObject2).getString(((Cursor)localObject2).getColumnIndex("_data"));
        }
        paramUri = (Uri)localObject1;
        if (Build.VERSION.SDK_INT >= 14) {
          continue;
        }
        ((Cursor)localObject2).close();
        paramUri = (Uri)localObject1;
        continue;
      }
      paramUri = paramUri.getPath();
    }
  }
  
  public void saveSingerSkin(Bitmap paramBitmap, Music paramMusic) {}
  
  public void showQuitDialog()
  {
    DialogFragmentUtils.showTipDialog(this, "????????", "??????????????", "????", "????", new BaseDialogFragment.OnClickListener()
    {
      public void onClick(BaseDialogFragment paramAnonymousBaseDialogFragment, int paramAnonymousInt)
      {
        if (paramAnonymousInt == -1) {
          App.getInstance().exitApp();
        }
      }
    }, null);
  }
}
