package cn.kuwo.kwmusiccar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import cn.kuwo.base.log.LogMgr;

public class MediaButtonReceiver
  extends BroadcastReceiver
{
  public static final String EXTRA = "EXTRA";
  public static final String Hardware_MediaButton = "Hardware_MediaButton";
  public static final String KUWO_CARMUSIC_MEDIABUTTON_ACTION = "cn.kuwo.kwmusicauto.action.MEDIA_BUTTON";
  public static final String MEDIA_CIRCLE = "MEDIA_CIRCLE";
  public static final String MEDIA_MUTE = "MEDIA_MUTE";
  public static final String MEDIA_NEXT = "MEDIA_NEXT";
  public static final String MEDIA_ONE = "MEDIA_ONE";
  public static final String MEDIA_ORDER = "MEDIA_ORDER";
  public static final String MEDIA_PAUSE = "MEDIA_PAUSE";
  public static final String MEDIA_PLAY = "MEDIA_PLAY";
  public static final String MEDIA_PLAY_PAUSE = "MEDIA_PLAY_PAUSE";
  public static final String MEDIA_PRE = "MEDIA_PRE";
  public static final String MEDIA_RANDOM = "MEDIA_RANDOM";
  public static final String MEDIA_SINGLE = "MEDIA_SINGLE";
  
  public void onReceive(Context paramContext, Intent paramIntent)
  {
    if ("android.intent.action.MEDIA_BUTTON".equals(paramIntent.getAction()))
    {
      paramIntent = (KeyEvent)paramIntent.getParcelableExtra("android.intent.extra.KEY_EVENT");
      if ((paramIntent != null) && (paramIntent.getAction() != 1)) {}
    }
    else
    {
      return;
    }
    Intent localIntent = new Intent("cn.kuwo.kwmusicauto.action.MEDIA_BUTTON");
    int i = paramIntent.getKeyCode();
    LogMgr.i("MediaButtonReceiver", "Received key code = " + i);
    switch (i)
    {
    default: 
      paramIntent = null;
    }
    for (;;)
    {
      LogMgr.i("MediaButtonReceiver", paramIntent);
      localIntent.putExtra("EXTRA", paramIntent);
      localIntent.putExtra("Hardware_MediaButton", true);
      paramContext.sendBroadcast(localIntent);
      return;
      paramIntent = "MEDIA_NEXT";
      continue;
      paramIntent = "MEDIA_PLAY_PAUSE";
      continue;
      paramIntent = "MEDIA_PRE";
      continue;
      paramIntent = "MEDIA_PLAY";
      continue;
      paramIntent = "MEDIA_PAUSE";
      continue;
      paramIntent = "MEDIA_MUTE";
    }
  }
}
