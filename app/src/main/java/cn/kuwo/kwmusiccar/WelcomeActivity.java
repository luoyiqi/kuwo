package cn.kuwo.kwmusiccar;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import cn.kuwo.base.config.ConfMgr;
import cn.kuwo.base.db.DataBaseManager;
import cn.kuwo.base.log.LogMgr;
import cn.kuwo.base.util.DeviceUtils;
import cn.kuwo.base.util.GraphicsUtils;
import cn.kuwo.base.util.KwExceptionHandler;
import cn.kuwo.base.util.KwThreadPool;
import cn.kuwo.base.util.KwThreadPool.JobType;
import cn.kuwo.base.util.ToastUtil;
import cn.kuwo.core.messagemgr.MessageID;
import cn.kuwo.core.messagemgr.MessageManager;
import cn.kuwo.core.messagemgr.MessageManager.Caller;
import cn.kuwo.core.messagemgr.MessageManager.Runner;
import cn.kuwo.core.observers.IAppObserver;
import cn.kuwo.mod.welcome.WelcomeMgrImpl;
import cn.kuwo.service.MainService;

public class WelcomeActivity
  extends Activity
  implements IAppObserver
{
  public static final int WELCOME_TIME = 1500;
  public static volatile boolean initFinished = false;
  Bitmap mBitmap = null;
  int mInSampleSize = 1;
  ImageView mLogo = null;
  ImageView mWelcomeImage = null;
  private MessageManager.Runner runner = null;
  private MediaPlayer startRingPlayer;
  
  private boolean hasSDCard()
  {
    return "mounted".equals(Environment.getExternalStorageState());
  }
  
  @TargetApi(19)
  private void hideNavigationBar()
  {
    getWindow().getDecorView().setSystemUiVisibility(5382);
  }
  
  private void init()
  {
    DeviceUtils.init(this);
    if (DeviceUtils.LOWER_DEVICE) {
      this.mInSampleSize = 2;
    }
    playStartRing();
    loadWelcomePic();
    App.fetchAppUid();
    KwExceptionHandler.initAndSendLogs();
    KwThreadPool.runThread(KwThreadPool.JobType.IMMEDIATELY, new Runnable()
    {
      public void run()
      {
        DataBaseManager.init(App.getInstance().getApplicationContext());
        MessageManager.getInstance().asyncRun(new MessageManager.Runner()
        {
          public void call()
          {
            App.initModMgr(true);
            MainService.connect();
          }
        });
      }
    });
  }
  
  private void loadWelcomePic()
  {
    if (ConfMgr.getBoolValue("", "start_pic", true))
    {
      WelcomeMgrImpl localWelcomeMgrImpl = new WelcomeMgrImpl();
      localWelcomeMgrImpl.init();
      this.mBitmap = localWelcomeMgrImpl.getTodayPic();
      if (this.mBitmap != null)
      {
        this.mWelcomeImage.setImageBitmap(this.mBitmap);
        return;
      }
      try
      {
        this.mBitmap = GraphicsUtils.getBitmap(this, 2130837947, this.mInSampleSize);
        this.mWelcomeImage.setImageBitmap(this.mBitmap);
        cn.kuwo.mod.welcome.WelComeConstants.isDefaultPic = true;
        return;
      }
      catch (OutOfMemoryError localOutOfMemoryError1)
      {
        return;
      }
    }
    try
    {
      this.mBitmap = GraphicsUtils.getBitmap(this, 2130837947, this.mInSampleSize);
      this.mWelcomeImage.setImageBitmap(this.mBitmap);
      cn.kuwo.mod.welcome.WelComeConstants.isDefaultPic = true;
      return;
    }
    catch (OutOfMemoryError localOutOfMemoryError2) {}
  }
  
  private boolean needShowGuide()
  {
    return (DeviceUtils.FIRST_INSTALL) || (DeviceUtils.COVER_INSTALL);
  }
  
  private void playStartRing()
  {
    if (!ConfMgr.getBoolValue("", "need_play_start_sound", false)) {
      return;
    }
    KwThreadPool.runThread(KwThreadPool.JobType.NORMAL, new Runnable()
    {
      public void run()
      {
        AudioManager localAudioManager = (AudioManager)WelcomeActivity.this.getSystemService("audio");
        if (localAudioManager == null) {}
        do
        {
          do
          {
            return;
          } while ((localAudioManager.getRingerMode() == 0) || (localAudioManager.getRingerMode() == 1));
          WelcomeActivity.access$202(WelcomeActivity.this, MediaPlayer.create(WelcomeActivity.this.getApplicationContext(), 2131034112));
        } while (WelcomeActivity.this.startRingPlayer == null);
        WelcomeActivity.this.startRingPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
          public void onCompletion(MediaPlayer paramAnonymous2MediaPlayer)
          {
            if (paramAnonymous2MediaPlayer != null)
            {
              paramAnonymous2MediaPlayer.setOnCompletionListener(null);
              paramAnonymous2MediaPlayer.stop();
              paramAnonymous2MediaPlayer.release();
            }
            WelcomeActivity.access$202(WelcomeActivity.this, null);
          }
        });
        WelcomeActivity.this.startRingPlayer.start();
      }
    });
  }
  
  private void startActivity(Class<?> paramClass)
  {
    paramClass = new Intent(this, paramClass);
    paramClass.setData(getIntent().getData());
    paramClass.setAction(getIntent().getAction());
    Bundle localBundle = getIntent().getExtras();
    if (localBundle != null) {
      paramClass.putExtras(localBundle);
    }
    startActivity(paramClass);
    finish();
  }
  
  public void IAppObserver_InitFinished()
  {
    initFinished = true;
  }
  
  public void IAppObserver_NetworkStateChanged(boolean paramBoolean1, boolean paramBoolean2) {}
  
  public void IAppObserver_OnBackground() {}
  
  public void IAppObserver_OnForground() {}
  
  public void IAppObserver_OnLowMemory() {}
  
  public void IAppObserver_OnNowplayingShow(boolean paramBoolean) {}
  
  public void IAppObserver_OnUpdateDatabase()
  {
    ToastUtil.show("??????????????????...");
  }
  
  public void IAppObserver_PrepareExitApp() {}
  
  public void IAppObserver_SDCardStateChanged(boolean paramBoolean) {}
  
  public void IAppObserver_WelcomePageDisappear()
  {
    LogMgr.i("WelcomeActivity", "IAppObserver_WelcomePageDisappear");
  }
  
  protected void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    if (Build.VERSION.SDK_INT >= 11) {
      hideNavigationBar();
    }
    paramBundle = getIntent().getAction();
    if ((!TextUtils.isEmpty(paramBundle)) && ((paramBundle.equals("cn.kuwo.kwmusicauto.action.STARTAPP")) || (paramBundle.equals("cn.kuwo.kwmusicauto.action.PLAY_MUSIC")) || (paramBundle.equals("cn.kuwo.kwmusicauto.action.SEARCH_MUSIC"))))
    {
      paramBundle = getIntent().getExtras();
      if ((paramBundle != null) && (!App.hasRightKey(paramBundle.getString("kuwo_key"))))
      {
        finish();
        return;
      }
    }
    if (initFinished)
    {
      startActivity(MainActivity.class);
      return;
    }
    if (!hasSDCard())
    {
      startActivity(new Intent(this, NoSDCardActivity.class));
      finish();
      return;
    }
    setContentView(2130903172);
    this.mWelcomeImage = ((ImageView)findViewById(2131427986));
    this.mLogo = ((ImageView)findViewById(2131427987));
    MessageManager.getInstance().attachMessage(MessageID.OBSERVER_APP, this);
    init();
    this.runner = new MessageManager.Runner()
    {
      public void call()
      {
        if (!WelcomeActivity.initFinished)
        {
          MessageManager.getInstance().asyncRun(300, WelcomeActivity.this.runner);
          LogMgr.i("serviceinit", "??????????????????");
          return;
        }
        MessageManager.getInstance().detachMessage(MessageID.OBSERVER_APP, WelcomeActivity.this);
        WelcomeActivity.this.startActivity(MainActivity.class);
        WelcomeActivity.this.overridePendingTransition(2130968583, 2130968584);
        MessageManager.getInstance().asyncNotify(MessageID.OBSERVER_APP, new MessageManager.Caller()
        {
          public void call()
          {
            ((IAppObserver)this.ob).IAppObserver_WelcomePageDisappear();
          }
        });
      }
    };
    MessageManager.getInstance().asyncRun(1500, this.runner);
  }
  
  protected void onDestroy()
  {
    if (this.mWelcomeImage != null) {
      this.mWelcomeImage.setImageBitmap(null);
    }
    if ((this.mBitmap != null) && (!this.mBitmap.isRecycled()))
    {
      this.mBitmap.recycle();
      this.mBitmap = null;
    }
    super.onDestroy();
  }
  
  public boolean onKeyDown(int paramInt, KeyEvent paramKeyEvent)
  {
    if (paramInt == 4) {
      return false;
    }
    return super.onKeyDown(paramInt, paramKeyEvent);
  }
}
