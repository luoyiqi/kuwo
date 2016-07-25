package cn.kuwo.kwmusiccar;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.text.TextUtils;
import cn.kuwo.base.bean.ListType;
import cn.kuwo.base.bean.Music;
import cn.kuwo.base.bean.MusicList;
import cn.kuwo.base.config.ConfMgr;
import cn.kuwo.base.log.LogMgr;
import cn.kuwo.base.util.JumperUtils;
import cn.kuwo.base.util.NetworkStateUtil;
import cn.kuwo.base.util.ToastUtil;
import cn.kuwo.core.messagemgr.MessageID;
import cn.kuwo.core.messagemgr.MessageManager;
import cn.kuwo.core.messagemgr.MessageManager.Runner;
import cn.kuwo.core.modulemgr.ModMgr;
import cn.kuwo.core.observers.ILocalMgrObserver;
import cn.kuwo.mod.list.IListMgr;
import cn.kuwo.mod.localmgr.ILocalMgr;
import cn.kuwo.mod.playcontrol.IPlayControl;
import cn.kuwo.mod.radio.IRadioMgr;
import cn.kuwo.service.PlayProxy.Status;
import cn.kuwo.ui.fragment.dialog.BaseProgressDialog;
import cn.kuwo.ui.fragment.dialog.DialogFragmentUtils;
import cn.kuwo.ui.topbar.TopBarController;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class KwCarPlay
{
  public static final String AUTO_PLAY = "auto_play";
  private static final String CLOSE_DESKLYRIC = "cn.kuwo.kwmusicauto.action.CLOSE_DESKLYRIC";
  private static final String ENDTYPE = "ENDTYPE";
  private static final String EXIT_KWMUSICAPP = "cn.kuwo.kwmusicauto.action.EXITAPP";
  private static final String HAS_MV = "hasMv";
  private static final String MUSIC = "music";
  private static final String MUSIC_ALBUM = "album";
  private static final String MUSIC_ARTISTID = "artistid";
  private static final String MUSIC_NAME = "name";
  private static final String MUSIC_RID = "rid";
  private static final String MUSIC_SINGER = "singer";
  private static final String MUSIC_SOURCE = "source";
  private static final String MV_QUALITY = "mvQuality";
  private static final String OPEN_DESKLYRIC = "cn.kuwo.kwmusicauto.action.OPEN_DESKLYRIC";
  private static final String PLAYERSTATUS = "PLAYERSTATUS";
  private static final String PLAYER_STATUS = "cn.kuwo.kwmusicauto.action.PLAYER_STATUS";
  private static final String PLAYMUSIC_ALBUM = "play_music_album";
  private static final String PLAYMUSIC_ARTIST = "play_music_artist";
  private static final String PLAYMUSIC_NAME = "play_music_name";
  private static final String PLAY_END = "cn.kuwo.kwmusicauto.action.PLAY_END";
  public static final String PLAY_MUSIC = "cn.kuwo.kwmusicauto.action.PLAY_MUSIC";
  public static final String SEARCH_MUSIC = "cn.kuwo.kwmusicauto.action.SEARCH_MUSIC";
  public static final String START_KWMUSICAPP = "cn.kuwo.kwmusicauto.action.STARTAPP";
  private static final String TAG = "KwCarPlay";
  private static ILocalMgrObserver localMgrObserver = new ILocalMgrObserver()
  {
    public void ILocalMgrObserver_OnFinished(int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3, Collection<Music> paramAnonymousCollection)
    {
      if (KwCarPlay.progressDialog != null)
      {
        KwCarPlay.progressDialog.dismiss();
        KwCarPlay.access$202(null);
        paramAnonymousCollection = ModMgr.getLocalMgr().getAllMusics();
        if ((paramAnonymousCollection != null) && (paramAnonymousCollection.size() > 0) && (paramAnonymousCollection.get(0) != null)) {
          ModMgr.getLocalMgr().playMusic(paramAnonymousCollection, 0);
        }
      }
      else
      {
        return;
      }
      ToastUtil.show("????????????????");
    }
    
    public void ILocalMgrObserver_OnListChanged(Collection<Music> paramAnonymousCollection) {}
    
    public void ILocalMgrObserver_OnProgress(int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3, String paramAnonymousString) {}
  };
  private static final CarPlayBroadcastReceiver mCarPlayBroadcastReceiver = new CarPlayBroadcastReceiver(null);
  private static final KwMediaReceiver mKwMediaReceiver = new KwMediaReceiver(null);
  private static BaseProgressDialog progressDialog = null;
  
  private static boolean findArtistMusic(String paramString1, String paramString2)
  {
    if (TextUtils.isEmpty(paramString1)) {}
    for (;;)
    {
      return false;
      ModMgr.getLocalMgr().getArtistView();
      MusicList localMusicList = ModMgr.getLocalMgr().getArtistPlaylist(paramString1);
      if ((localMusicList != null) && (localMusicList.size() > 0))
      {
        if (TextUtils.isEmpty(paramString2))
        {
          if ((NetworkStateUtil.isAvaliable()) && (localMusicList.size() < 20))
          {
            searchOnline(paramString1);
            return true;
          }
          paramString2 = ModMgr.getPlayControl().getNowPlayingList();
          if ((ModMgr.getPlayControl().getNowPlayingMusic() != null) && (paramString2 != null) && (paramString1.equals(paramString2.getShowName())) && (paramString2.getName().equals("local.artist")) && (ModMgr.getPlayControl().continuePlay())) {
            return true;
          }
          MainActivity.getInstance().getTopBarController().setSelectedTab(TopBarController.Tab_Lyric);
          ModMgr.getLocalMgr().playMusic(localMusicList, 0);
          return true;
        }
        int i = 0;
        while (i < localMusicList.size())
        {
          if (localMusicList.get(i).name.toLowerCase().contains(paramString2.toLowerCase()))
          {
            MainActivity.getInstance().getTopBarController().setSelectedTab(TopBarController.Tab_Lyric);
            ModMgr.getLocalMgr().playMusic(localMusicList, i);
            return true;
          }
          i += 1;
        }
      }
    }
  }
  
  private static boolean findMusic(String paramString1, String paramString2, String paramString3)
  {
    boolean bool2 = false;
    MusicList localMusicList = ModMgr.getListMgr().getList(paramString3);
    boolean bool1 = bool2;
    int i;
    if (localMusicList != null)
    {
      bool1 = bool2;
      if (localMusicList.size() != 0) {
        i = 0;
      }
    }
    for (;;)
    {
      bool1 = bool2;
      Object localObject;
      if (i < localMusicList.size())
      {
        localObject = localMusicList.get(i);
        if ((!TextUtils.isEmpty(paramString1)) && (!TextUtils.isEmpty(paramString2)))
        {
          if ((((Music)localObject).name == null) || (!((Music)localObject).name.toLowerCase().contains(paramString1.toLowerCase())) || (((Music)localObject).artist == null) || (!((Music)localObject).artist.toLowerCase().contains(paramString2.toLowerCase()))) {
            break label320;
          }
          MainActivity.getInstance().getTopBarController().setSelectedTab(TopBarController.Tab_Lyric);
          localObject = ModMgr.getListMgr().getList(paramString3);
          if (localObject == null) {
            break label320;
          }
          ModMgr.getPlayControl().play((MusicList)localObject, i);
          bool1 = true;
        }
      }
      else
      {
        return bool1;
      }
      if ((((Music)localObject).name != null) && (!TextUtils.isEmpty(paramString1)) && (((Music)localObject).name.toLowerCase().contains(paramString1.toLowerCase())))
      {
        MainActivity.getInstance().getTopBarController().setSelectedTab(TopBarController.Tab_Lyric);
        localObject = ModMgr.getListMgr().getList(paramString3);
        if (localObject != null)
        {
          ModMgr.getPlayControl().play((MusicList)localObject, i);
          return true;
        }
      }
      else if ((((Music)localObject).artist != null) && (!TextUtils.isEmpty(paramString2)) && (((Music)localObject).artist.toLowerCase().contains(paramString2.toLowerCase())))
      {
        MainActivity.getInstance().getTopBarController().setSelectedTab(TopBarController.Tab_Lyric);
        paramString1 = ModMgr.getListMgr().getList(paramString3);
        if (paramString1 != null)
        {
          ModMgr.getPlayControl().play(paramString1, i);
          return true;
        }
        return true;
      }
      label320:
      i += 1;
    }
  }
  
  public static boolean handleCarPlay(Intent paramIntent)
  {
    if (paramIntent == null) {}
    String str1;
    label81:
    do
    {
      do
      {
        do
        {
          return false;
          str1 = paramIntent.getAction();
        } while (TextUtils.isEmpty(str1));
        localObject = paramIntent.getExtras();
      } while ((localObject == null) || (!App.hasRightKey(paramIntent.getStringExtra("kuwo_key"))));
      if ("cn.kuwo.kwmusicauto.action.STARTAPP".equals(str1))
      {
        if (paramIntent.getBooleanExtra("auto_play", false))
        {
          if (ModMgr.getPlayControl().getNowPlayingMusic() == null) {
            break label81;
          }
          ModMgr.getPlayControl().continuePlay();
        }
        for (;;)
        {
          return true;
          paramIntent = ModMgr.getLocalMgr().getAllMusics();
          if ((paramIntent != null) && (paramIntent.size() > 0) && (paramIntent.get(0) != null))
          {
            ModMgr.getLocalMgr().playMusic(paramIntent, 0);
          }
          else if (NetworkStateUtil.isAvaliable())
          {
            ToastUtil.show("??????????????????????????");
            ModMgr.getRadioMgr().playRadio(60924, "????????", "");
          }
          else
          {
            progressDialog = DialogFragmentUtils.showProgressDialog(MainActivity.getInstance(), "??????????...");
            MessageManager.getInstance().asyncRun(300, new MessageManager.Runner()
            {
              public void call()
              {
                ModMgr.getLocalMgr().autoScan();
              }
            });
          }
        }
      }
      if ("cn.kuwo.kwmusicauto.action.SEARCH_MUSIC".equals(str1))
      {
        paramIntent = ((Bundle)localObject).getString("name");
        str1 = ((Bundle)localObject).getString("singer");
        String str2 = ((Bundle)localObject).getString("album");
        localObject = ((Bundle)localObject).getString("source");
        if (!TextUtils.isEmpty((CharSequence)localObject)) {
          MainActivity.getInstance().processUri(Uri.parse((String)localObject));
        }
        for (;;)
        {
          return true;
          searchMusic(paramIntent, str1, str2);
        }
      }
    } while (!"cn.kuwo.kwmusicauto.action.PLAY_MUSIC".equals(str1));
    paramIntent = new Music();
    paramIntent.rid = ((Bundle)localObject).getLong("rid");
    paramIntent.name = ((Bundle)localObject).getString("name");
    paramIntent.artist = ((Bundle)localObject).getString("singer");
    paramIntent.album = ((Bundle)localObject).getString("album");
    paramIntent.artistId = ((Bundle)localObject).getLong("artistid");
    paramIntent.mvQuality = ((Bundle)localObject).getString("mvQuality");
    paramIntent.hasMv = ((Bundle)localObject).getBoolean("hasMv");
    Object localObject = new ArrayList();
    ((List)localObject).add(paramIntent);
    int i = ModMgr.getListMgr().insertMusic("????????", (List)localObject);
    if (i >= 0)
    {
      paramIntent = ModMgr.getListMgr().getList("????????");
      ModMgr.getPlayControl().play(paramIntent, i);
    }
    for (;;)
    {
      return true;
      ToastUtil.show("????????????????????");
    }
  }
  
  public static void init(Context paramContext)
  {
    if (paramContext == null)
    {
      LogMgr.e("KwCarPlay", "unexpected null context in init");
      return;
    }
    registerMediaButtonEventReveiver(paramContext);
    registerCarPlayReceiver(paramContext);
    MessageManager.getInstance().attachMessage(MessageID.OBSERVER_LOCAL, localMgrObserver);
  }
  
  private static void registerCarPlayReceiver(Context paramContext)
  {
    IntentFilter localIntentFilter = new IntentFilter();
    localIntentFilter.addAction("cn.kuwo.kwmusicauto.action.EXITAPP");
    localIntentFilter.addAction("cn.kuwo.kwmusicauto.action.CLOSE_DESKLYRIC");
    localIntentFilter.addAction("cn.kuwo.kwmusicauto.action.OPEN_DESKLYRIC");
    paramContext.registerReceiver(mCarPlayBroadcastReceiver, localIntentFilter);
  }
  
  private static void registerKuwoMediaReceiver(Context paramContext)
  {
    IntentFilter localIntentFilter = new IntentFilter();
    localIntentFilter.addAction("cn.kuwo.kwmusicauto.action.MEDIA_BUTTON");
    paramContext.registerReceiver(mKwMediaReceiver, localIntentFilter);
  }
  
  private static void registerMediaButtonEventReveiver(Context paramContext)
  {
    ((AudioManager)paramContext.getSystemService("audio")).registerMediaButtonEventReceiver(new ComponentName(paramContext.getPackageName(), MediaButtonReceiver.class.getName()));
    registerKuwoMediaReceiver(paramContext);
  }
  
  public static void release(Context paramContext)
  {
    if (paramContext == null)
    {
      LogMgr.e("KwCarPlay", "unexpected null context in release");
      return;
    }
    unRegisterMediaButtonEventReceiver(paramContext);
    unRegisterCarPlayReceiver(paramContext);
    MessageManager.getInstance().detachMessage(MessageID.OBSERVER_LOCAL, localMgrObserver);
  }
  
  private static void searchMusic(String paramString1, final String paramString2, final String paramString3)
  {
    MessageManager.getInstance().asyncRun(new MessageManager.Runner()
    {
      public void call()
      {
        Object localObject2 = "";
        if (!TextUtils.isEmpty(this.val$songName)) {
          localObject2 = "" + this.val$songName + " ";
        }
        Object localObject1 = localObject2;
        if (!TextUtils.isEmpty(paramString2)) {
          localObject1 = (String)localObject2 + paramString2 + " ";
        }
        localObject2 = localObject1;
        if (!TextUtils.isEmpty(paramString3)) {
          localObject2 = (String)localObject1 + paramString3;
        }
        LogMgr.i("????", (String)localObject2);
        if (TextUtils.isEmpty((CharSequence)localObject2))
        {
          localObject1 = ModMgr.getListMgr().getList(ListType.LIST_LOCAL_ALL.getTypeName());
          if ((localObject1 != null) && (((MusicList)localObject1).size() > 0))
          {
            int i = new Random().nextInt(((MusicList)localObject1).size());
            ModMgr.getPlayControl().play((MusicList)localObject1, i);
          }
        }
        do
        {
          return;
          KwCarPlay.access$202(DialogFragmentUtils.showProgressDialog(MainActivity.getInstance(), "??????????..."));
          MessageManager.getInstance().asyncRun(300, new MessageManager.Runner()
          {
            public void call()
            {
              ModMgr.getLocalMgr().autoScan();
            }
          });
          return;
          if ((!TextUtils.isEmpty(paramString3)) && (NetworkStateUtil.isAvaliable()))
          {
            KwCarPlay.searchOnline((String)localObject2);
            return;
          }
        } while (((!TextUtils.isEmpty(paramString2)) && (KwCarPlay.findArtistMusic(paramString2, this.val$songName))) || (KwCarPlay.findMusic(this.val$songName, paramString2, ListType.LIST_LOCAL_ALL.getTypeName())) || (KwCarPlay.findMusic(this.val$songName, paramString2, ListType.LIST_DEFAULT.getTypeName())));
        if (!NetworkStateUtil.isAvaliable())
        {
          localObject1 = ModMgr.getListMgr().getList(ListType.LIST_LOCAL_ALL.getTypeName());
          if ((localObject1 != null) && (((MusicList)localObject1).size() > 0))
          {
            ToastUtil.show("??????????????????????????!");
            ModMgr.getPlayControl().play((MusicList)localObject1, 0);
            return;
          }
          ToastUtil.show("??????????????????????????????");
          return;
        }
        KwCarPlay.searchOnline((String)localObject2);
      }
    });
  }
  
  private static void searchOnline(String paramString)
  {
    JumperUtils.JumpToSearch(paramString, true);
  }
  
  public static void sendMediaUpdateBroadcast(Context paramContext, String paramString)
  {
    if (Build.VERSION.SDK_INT >= 19)
    {
      MediaScannerConnection.scanFile(paramContext, new String[] { paramString }, null, null);
      return;
    }
    paramContext.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.parse("file://" + paramString)));
  }
  
  public static void sendPlayEndBroadcast(Context paramContext, int paramInt)
  {
    Intent localIntent = new Intent("cn.kuwo.kwmusicauto.action.PLAY_END");
    localIntent.putExtra("ENDTYPE", paramInt);
    paramContext.sendBroadcast(localIntent);
  }
  
  public static void sendPlayerStatusBroadcast(Context paramContext, int paramInt, Music paramMusic)
  {
    Intent localIntent = new Intent("cn.kuwo.kwmusicauto.action.PLAYER_STATUS");
    localIntent.putExtra("PLAYERSTATUS", paramInt);
    if (paramMusic != null)
    {
      localIntent.putExtra("play_music_name", paramMusic.name);
      localIntent.putExtra("play_music_artist", paramMusic.artist);
      localIntent.putExtra("play_music_album", paramMusic.album);
    }
    paramContext.sendBroadcast(localIntent);
  }
  
  private static void unKuwoMediaReceiver(Context paramContext)
  {
    paramContext.unregisterReceiver(mKwMediaReceiver);
  }
  
  private static void unRegisterCarPlayReceiver(Context paramContext)
  {
    paramContext.unregisterReceiver(mCarPlayBroadcastReceiver);
  }
  
  private static void unRegisterMediaButtonEventReceiver(Context paramContext)
  {
    unKuwoMediaReceiver(paramContext);
    ((AudioManager)paramContext.getSystemService("audio")).unregisterMediaButtonEventReceiver(new ComponentName(paramContext.getPackageName(), MediaButtonReceiver.class.getName()));
  }
  
  private static class CarPlayBroadcastReceiver
    extends BroadcastReceiver
  {
    public void onReceive(Context paramContext, Intent paramIntent)
    {
      paramContext = paramIntent.getAction();
      if (TextUtils.isEmpty(paramContext)) {}
      do
      {
        return;
        paramIntent = paramIntent.getStringExtra("kuwo_key");
        if ((paramContext.equals("cn.kuwo.kwmusicauto.action.EXITAPP")) && (App.hasRightKey(paramIntent)))
        {
          App.getInstance().exitApp();
          return;
        }
        if ((paramContext.equals("cn.kuwo.kwmusicauto.action.OPEN_DESKLYRIC")) && (App.hasRightKey(paramIntent)))
        {
          ConfMgr.setBoolValue("", "desk_lrc_enable", true, true);
          return;
        }
      } while ((!paramContext.equals("cn.kuwo.kwmusicauto.action.CLOSE_DESKLYRIC")) || (!App.hasRightKey(paramIntent)));
      ConfMgr.setBoolValue("", "desk_lrc_enable", false, true);
    }
  }
  
  private static class KwMediaReceiver
    extends BroadcastReceiver
  {
    public void onReceive(Context paramContext, Intent paramIntent)
    {
      boolean bool = true;
      paramContext = paramIntent.getAction();
      if ((paramContext == null) || (!paramContext.equals("cn.kuwo.kwmusicauto.action.MEDIA_BUTTON")) || ((!paramIntent.getBooleanExtra("Hardware_MediaButton", false)) && (!App.hasRightKey(paramIntent.getStringExtra("kuwo_key"))))) {}
      do
      {
        do
        {
          return;
          paramIntent = paramIntent.getStringExtra("EXTRA");
          paramContext = ModMgr.getPlayControl();
        } while (paramContext == null);
        if ("MEDIA_CIRCLE".equals(paramIntent))
        {
          paramContext.setPlayMode(3);
          return;
        }
        if ("MEDIA_ONE".equals(paramIntent))
        {
          paramContext.setPlayMode(1);
          return;
        }
        if ("MEDIA_SINGLE".equals(paramIntent))
        {
          paramContext.setPlayMode(0);
          return;
        }
        if ("MEDIA_ORDER".equals(paramIntent))
        {
          paramContext.setPlayMode(2);
          return;
        }
        if ("MEDIA_RANDOM".equals(paramIntent))
        {
          paramContext.setPlayMode(4);
          return;
        }
        if ("MEDIA_PRE".equals(paramIntent))
        {
          paramContext.playPre();
          return;
        }
        if ("MEDIA_NEXT".equals(paramIntent))
        {
          paramContext.playNext();
          return;
        }
        if ("MEDIA_PLAY".equals(paramIntent))
        {
          paramContext.continuePlay();
          return;
        }
        if ("MEDIA_PAUSE".equals(paramIntent))
        {
          paramContext.pause();
          return;
        }
        if ("MEDIA_MUTE".equals(paramIntent))
        {
          if (!paramContext.isMute()) {}
          for (;;)
          {
            paramContext.setMute(bool);
            return;
            bool = false;
          }
        }
      } while (!"MEDIA_PLAY_PAUSE".equals(paramIntent));
      paramIntent = paramContext.getStatus();
      if ((PlayProxy.Status.PAUSE == paramIntent) || (PlayProxy.Status.STOP == paramIntent))
      {
        paramContext.continuePlay();
        return;
      }
      paramContext.pause();
    }
  }
}
