package cn.kuwo.kwmusiccar;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Process;

public class NoSDCardActivity
  extends Activity
{
  private BroadcastReceiver receiver = new BroadcastReceiver()
  {
    public void onReceive(Context paramAnonymousContext, Intent paramAnonymousIntent)
    {
      if ("android.intent.action.MEDIA_MOUNTED".equals(paramAnonymousIntent.getAction())) {
        NoSDCardActivity.this.startActivity(new Intent(NoSDCardActivity.this, MainActivity.class));
      }
    }
  };
  
  protected void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(2130903154);
  }
  
  protected void onPause()
  {
    super.onPause();
    unregisterReceiver(this.receiver);
    finish();
    Process.killProcess(Process.myPid());
  }
  
  protected void onResume()
  {
    super.onResume();
    IntentFilter localIntentFilter = new IntentFilter("android.intent.action.MEDIA_MOUNTED");
    localIntentFilter.addAction("android.intent.action.MEDIA_MOUNTED");
    localIntentFilter.addDataScheme("file");
    registerReceiver(this.receiver, localIntentFilter);
  }
}
