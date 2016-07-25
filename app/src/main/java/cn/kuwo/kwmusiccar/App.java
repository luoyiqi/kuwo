package cn.kuwo.kwmusiccar;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.text.TextUtils;
import cn.kuwo.base.bean.UserInfo;
import cn.kuwo.base.config.ConfMgr;
import cn.kuwo.base.db.DataBaseManager;
import cn.kuwo.base.http.HttpSession;
import cn.kuwo.base.imageloader.ImageManager;
import cn.kuwo.base.log.LogMgr;
import cn.kuwo.base.util.DirUtils;
import cn.kuwo.base.util.KwDate;
import cn.kuwo.base.util.KwExceptionHandler;
import cn.kuwo.base.util.KwFileUtils;
import cn.kuwo.base.util.KwThreadPool;
import cn.kuwo.base.util.KwThreadPool.JobType;
import cn.kuwo.base.util.NetworkStateUtil;
import cn.kuwo.base.util.SDCardUtils;
import cn.kuwo.base.util.SettingsUtils;
import cn.kuwo.base.util.SysUtils;
import cn.kuwo.base.util.ToastUtil;
import cn.kuwo.base.util.UidFetcher;
import cn.kuwo.core.messagemgr.MessageID;
import cn.kuwo.core.messagemgr.MessageManager;
import cn.kuwo.core.messagemgr.MessageManager.Caller;
import cn.kuwo.core.messagemgr.MessageManager.Runner;
import cn.kuwo.core.modulemgr.ModMgr;
import cn.kuwo.core.observers.IAppObserver;
import cn.kuwo.mod.list.RecentPlayListMgr;
import cn.kuwo.mod.userinfo.IUserInfoMgr;
import cn.kuwo.service.DownloadProxy;
import cn.kuwo.service.MainService;
import cn.kuwo.service.PlayProxy;
import cn.kuwo.service.kwplayer.codec.DecoderManager;
import com.umeng.analytics.MobclickAgent;
import java.util.Iterator;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;

public class App
  extends Application
{
  private static String APP_CHANNEL = "";
  public static boolean IS_DEBUG = false;
  public static boolean IS_FORGROUND = false;
  private static boolean IS_NEEDCHECK = false;
  public static final String KUWO_KEY = "kuwo_key";
  public static long START_TIME;
  private static App _instance;
  private static boolean bcopyrightopen = true;
  public static boolean forceForground;
  private static volatile boolean isExiting;
  private static Handler mainThreadHandler = new Handler();
  private static long mainThreadID = Thread.currentThread().getId();
  
  static
  {
    _instance = null;
    START_TIME = 0L;
    IS_NEEDCHECK = true;
  }
  
  private void ReadConf()
  {
    Object localObject = SettingsUtils.getStringSettings("last_check_vip");
    if ((localObject != null) && (!((String)localObject).isEmpty()))
    {
      localObject = new KwDate((String)localObject);
      ((KwDate)localObject).increase(86400, 1);
      if (((KwDate)localObject).after(new KwDate())) {
        return;
      }
    }
    KwThreadPool.runThread(KwThreadPool.JobType.NORMAL, new MessageManager.Runner()
    {
      public void call()
      {
        final String str = null;
        int i = 0;
        while ((str == null) && (i < 3))
        {
          str = HttpSession.getString("http://updatepage.kuwo.cn/pagesig/arpad/config.htm");
          i += 1;
        }
        if ((str != null) && (str.startsWith("copyright=")))
        {
          i = str.indexOf('=') + 1;
          str = str.substring(i, i + 1);
          LogMgr.i("ReadConf", str);
          MessageManager.getInstance().syncRun(new MessageManager.Runner()
          {
            public void call()
            {
              if (str.equals("1"))
              {
                SettingsUtils.setBooleanSettings("copyright", true);
                App.access$102(true);
              }
              for (;;)
              {
                SettingsUtils.setStringSettings("last_check_vip", new KwDate().toDateTimeString());
                return;
                App.access$102(true);
                SettingsUtils.setBooleanSettings("copyright", App.bcopyrightopen);
              }
            }
          });
        }
      }
    });
  }
  
  public static void fetchAppUid()
  {
    String str = ConfMgr.getStringValue("appconfig", "appuid", "");
    LogMgr.d("UidFetcher", "appUid = " + str);
    UidFetcher.fetchUid(str);
  }
  
  public static String getAppUid()
  {
    String str = ConfMgr.getStringValue("appconfig", "appuid", "");
    if ((TextUtils.isEmpty(str)) || (str.equals("0"))) {
      fetchAppUid();
    }
    return str;
  }
  
  public static App getInstance()
  {
    return _instance;
  }
  
  public static Handler getMainThreadHandler()
  {
    return mainThreadHandler;
  }
  
  public static long getMainThreadID()
  {
    return mainThreadID;
  }
  
  public static boolean hasRightKey(String paramString)
  {
    if (!IS_NEEDCHECK) {}
    do
    {
      return true;
      if (TextUtils.isEmpty(paramString))
      {
        ToastUtil.show("????????????????????");
        return false;
      }
    } while ((TextUtils.isEmpty(APP_CHANNEL)) || (APP_CHANNEL.equals(paramString)));
    ToastUtil.show("??????????????????");
    return false;
  }
  
  public static void initModMgr(boolean paramBoolean)
  {
    DecoderManager.getDecoder("aac");
    RecentPlayListMgr.getInstance();
    ModMgr.getRadioMgr();
    ModMgr.getUserInfoMgr();
    ModMgr.getListMgr();
    ModMgr.getSearchMgr();
    ModMgr.getPlayControl();
    ModMgr.getLyricsMgr();
    ModMgr.getLocalMgr();
  }
  
  private static void initVipKey()
  {
    boolean bool = ConfMgr.getBoolValue("vip", "vip_on", false);
    if (bool != ConfMgr.getBoolValue("", "local_vip_on", false))
    {
      if (!bool) {
        break label96;
      }
      ConfMgr.setBoolValue("", "download_when_play_setting_enable", false, false);
    }
    for (;;)
    {
      ConfMgr.setBoolValue("", "local_vip_on", bool, false);
      if (KwFileUtils.isExist(DirUtils.getDirectory(0) + "kuwo.vip"))
      {
        ConfMgr.setBoolValue("", "local_vip_on", true, false);
        ConfMgr.setBoolValue("vip", "vip_on", true, false);
      }
      return;
      label96:
      ConfMgr.setBoolValue("", "download_when_play_setting_enable", true, false);
    }
  }
  
  private static void innerUpdateForgroundState()
  {
    boolean bool1 = true;
    boolean bool2 = false;
    Object localObject1 = getInstance().getApplicationContext();
    Object localObject2 = (ActivityManager)((Context)localObject1).getSystemService("activity");
    localObject1 = ((Context)localObject1).getPackageName();
    localObject2 = ((ActivityManager)localObject2).getRunningAppProcesses();
    if (localObject2 == null) {
      return;
    }
    label196:
    label197:
    for (;;)
    {
      localObject2 = ((List)localObject2).iterator();
      if (((Iterator)localObject2).hasNext())
      {
        ActivityManager.RunningAppProcessInfo localRunningAppProcessInfo = (ActivityManager.RunningAppProcessInfo)((Iterator)localObject2).next();
        if (!localRunningAppProcessInfo.processName.equals(localObject1)) {
          break;
        }
        if (forceForground)
        {
          if (!KwExceptionHandler.lockScreenVisible) {
            break label196;
          }
          bool1 = bool2;
        }
        for (;;)
        {
          if (IS_FORGROUND == bool1) {
            break label197;
          }
          IS_FORGROUND = bool1;
          LogMgr.i("??????", "IS_FORGROUND??" + IS_FORGROUND + "   isForground:" + bool1);
          if (bool1)
          {
            MessageManager.getInstance().asyncNotify(MessageID.OBSERVER_APP, new MessageManager.Caller()
            {
              public void call()
              {
                ((IAppObserver)this.ob).IAppObserver_OnForground();
              }
            });
            return;
            if (localRunningAppProcessInfo.importance == 100) {}
            for (bool1 = true;; bool1 = false) {
              break;
            }
          }
          MessageManager.getInstance().asyncNotify(MessageID.OBSERVER_APP, new MessageManager.Caller()
          {
            public void call()
            {
              ((IAppObserver)this.ob).IAppObserver_OnBackground();
            }
          });
          return;
        }
      }
    }
  }
  
  public static boolean isExiting()
  {
    return isExiting;
  }
  
  public static boolean isOpenCopyRight()
  {
    return bcopyrightopen;
  }
  
  private void saveAppStatusWhenExit()
  {
    int i = ModMgr.getUserInfoMgr().getLoginStatus();
    if (TextUtils.isEmpty(ModMgr.getUserInfoMgr().getLoginType()))
    {
      str = "";
      if (i != UserInfo.LOGIN_STATUS_NOT_LOGIN) {
        break label70;
      }
      ConfMgr.setBoolValue("", "login_auto_login", false, false);
      ConfMgr.setStringValue("", "login_type", "kong", false);
    }
    label70:
    do
    {
      return;
      str = ModMgr.getUserInfoMgr().getLoginType();
      break;
      ConfMgr.setStringValue("", "login_type", str, false);
    } while ((!str.equals(UserInfo.LOGIN_QQ)) && (!str.equals(UserInfo.LOGIN_SINA)));
    if (TextUtils.isEmpty(ModMgr.getUserInfoMgr().getUserInfo().getAccessToken())) {}
    for (String str = "";; str = ModMgr.getUserInfoMgr().getUserInfo().getAccessToken())
    {
      ConfMgr.setStringValue("", "login_access_token", str, false);
      return;
    }
  }
  
  public static void setForceForground(Activity paramActivity, boolean paramBoolean)
  {
    forceForground = paramBoolean;
    innerUpdateForgroundState();
  }
  
  public static void updateForgroundState()
  {
    MessageManager.getInstance().asyncRun(new MessageManager.Runner()
    {
      public void call() {}
    });
  }
  
  public void exitApp()
  {
    saveAppStatusWhenExit();
    NetworkStateUtil.release();
    MessageManager.getInstance().syncNotify(MessageID.OBSERVER_APP, new MessageManager.Caller()
    {
      public void call()
      {
        try
        {
          ((IAppObserver)this.ob).IAppObserver_PrepareExitApp();
          return;
        }
        catch (Throwable localThrowable) {}
      }
    });
    if (MainService.getPlayProxy() != null) {
      MainService.getPlayProxy().stop();
    }
    MessageManager.getInstance().asyncRun(new MessageManager.Runner()
    {
      public void call()
      {
        App.access$002(true);
        MainService.disconnect();
        MessageManager.getInstance().asyncRun(new MessageManager.Runner()
        {
          public void call()
          {
            MessageManager.getInstance().silence();
            try
            {
              ModMgr.releaseAll();
              if (MainService.getDownloadProxy() != null) {
                MainService.getDownloadProxy().prepareExit();
              }
              MainService.release();
              DataBaseManager.getInstance().closeDb();
              ImageManager.getInstance().recycleCache();
              MobclickAgent.onKillProcess(App.this);
              Process.killProcess(Process.myPid());
              System.exit(0);
              return;
            }
            catch (Throwable localThrowable) {}
          }
        });
      }
    });
  }
  
  public void init()
  {
    for (;;)
    {
      try
      {
        ApplicationInfo localApplicationInfo = getPackageManager().getApplicationInfo(getPackageName(), 128);
        if ((localApplicationInfo.flags & 0x2) == 0) {
          continue;
        }
        bool = true;
        IS_DEBUG = bool;
        LogMgr.setDebug(IS_DEBUG);
        Object localObject = localApplicationInfo.metaData.get("src");
        if (localObject != null) {
          APP_CHANNEL = localObject.toString();
        }
        IS_NEEDCHECK = localApplicationInfo.metaData.getBoolean("check_key");
      }
      catch (PackageManager.NameNotFoundException localNameNotFoundException)
      {
        boolean bool;
        continue;
      }
      LogMgr.i("check_key", IS_NEEDCHECK + "");
      LogMgr.i("APP_CHANNEL", APP_CHANNEL);
      LogMgr.i("appinit", "????????????");
      IS_FORGROUND = true;
      HttpsURLConnection.setFollowRedirects(true);
      NetworkStateUtil.init();
      SDCardUtils.init();
      if (!SettingsUtils.getBooleanSettings("shortcut", false)) {
        SysUtils.createShortcut(this);
      }
      SettingsUtils.setBooleanSettings("shortcut", true);
      bcopyrightopen = SettingsUtils.getBooleanSettings("copyright", true);
      ReadConf();
      return;
      bool = false;
    }
  }
  
  public void onCreate()
  {
    super.onCreate();
    START_TIME = System.currentTimeMillis();
    _instance = this;
    Thread.setDefaultUncaughtExceptionHandler(new KwExceptionHandler());
    try
    {
      getExternalCacheDir();
      init();
      return;
    }
    catch (Exception localException)
    {
      for (;;) {}
    }
  }
  
  public void onLowMemory()
  {
    KwExceptionHandler.lowMemory = true;
    MessageManager.getInstance().syncNotify(MessageID.OBSERVER_APP, new MessageManager.Caller()
    {
      public void call()
      {
        ((IAppObserver)this.ob).IAppObserver_OnLowMemory();
      }
    });
    super.onLowMemory();
    System.gc();
  }
}
