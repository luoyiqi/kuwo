package cn.kuwo.kwmusiccar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import cn.kuwo.base.cache.CacheMgr;
import cn.kuwo.base.uilib.CirclePageIndicator;
import cn.kuwo.base.util.DeviceUtils;
import cn.kuwo.base.util.GraphicsUtils;
import cn.kuwo.core.messagemgr.MessageManager;
import cn.kuwo.core.messagemgr.MessageManager.Runner;
import java.util.ArrayList;
import java.util.List;

public class GuideActivity
  extends Activity
{
  private int[] ids = { 2130837700, 2130837701, 2130837702 };
  private CirclePageIndicator mIndicator;
  private List<Bitmap> mListBitmaps = null;
  private List<View> mListView = null;
  private ViewPager viewPager;
  
  private void startMainActivity()
  {
    Intent localIntent = new Intent(this, MainActivity.class);
    localIntent.setData(getIntent().getData());
    localIntent.setAction(getIntent().getAction());
    Bundle localBundle = getIntent().getExtras();
    if (localBundle != null) {
      localIntent.putExtras(localBundle);
    }
    startActivity(localIntent);
    finish();
  }
  
  public void onBackPressed() {}
  
  protected void onCreate(Bundle paramBundle)
  {
    int j = 0;
    super.onCreate(paramBundle);
    setContentView(2130903041);
    this.viewPager = ((ViewPager)findViewById(2131427342));
    this.mIndicator = ((CirclePageIndicator)findViewById(2131427343));
    this.mIndicator.setCount(this.ids.length);
    this.mListView = new ArrayList(this.ids.length);
    this.mListBitmaps = new ArrayList(this.ids.length);
    if (DeviceUtils.LOWER_DEVICE) {}
    for (int i = 2;; i = 1)
    {
      paramBundle = this.ids;
      int m = paramBundle.length;
      int k = 0;
      for (;;)
      {
        int n;
        ImageView localImageView;
        if (j < m)
        {
          n = paramBundle[j];
          localImageView = new ImageView(this);
          localImageView.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        }
        try
        {
          Bitmap localBitmap = GraphicsUtils.getBitmap(this, n, i);
          this.mListBitmaps.add(localBitmap);
          localImageView.setImageBitmap((Bitmap)this.mListBitmaps.get(k));
          k += 1;
          localImageView.setScaleType(ImageView.ScaleType.FIT_XY);
          this.mListView.add(localImageView);
          j += 1;
          continue;
          MessageManager.getInstance().asyncRun(new MessageManager.Runner()
          {
            public void call()
            {
              CacheMgr.getInstance().cleanAll("WEBACTION");
            }
          });
          this.viewPager.setAdapter(new MyPagerAdapter(this.mListView));
          this.viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
          {
            private boolean flag;
            
            public void onPageScrollStateChanged(int paramAnonymousInt)
            {
              switch (paramAnonymousInt)
              {
              default: 
                return;
              case 1: 
                this.flag = false;
                return;
              case 2: 
                this.flag = true;
                return;
              }
              if ((GuideActivity.this.viewPager.getCurrentItem() == GuideActivity.this.viewPager.getAdapter().getCount() - 1) && (!this.flag)) {
                GuideActivity.this.startMainActivity();
              }
              this.flag = true;
            }
            
            public void onPageScrolled(int paramAnonymousInt1, float paramAnonymousFloat, int paramAnonymousInt2)
            {
              GuideActivity.this.mIndicator.onPageScrolled(paramAnonymousInt1, paramAnonymousFloat, paramAnonymousInt2);
            }
            
            public void onPageSelected(int paramAnonymousInt)
            {
              if (paramAnonymousInt < GuideActivity.this.ids.length) {
                GuideActivity.this.mIndicator.onPageSelected(paramAnonymousInt);
              }
            }
          });
          return;
        }
        catch (OutOfMemoryError localOutOfMemoryError)
        {
          for (;;) {}
        }
      }
    }
  }
  
  protected void onDestroy()
  {
    int j = 0;
    int i;
    if (this.mListView != null)
    {
      i = 0;
      while (i < this.mListView.size())
      {
        ((ImageView)this.mListView.get(i)).setImageBitmap(null);
        i += 1;
      }
      this.mListView.clear();
      this.mListView = null;
    }
    if (this.mListBitmaps != null)
    {
      i = j;
      while (i < this.mListBitmaps.size())
      {
        Bitmap localBitmap = (Bitmap)this.mListBitmaps.get(i);
        if ((localBitmap != null) && (!localBitmap.isRecycled())) {
          localBitmap.recycle();
        }
        i += 1;
      }
      this.mListBitmaps.clear();
      this.mListBitmaps = null;
    }
    super.onDestroy();
  }
  
  private static class MyPagerAdapter
    extends PagerAdapter
  {
    public List<View> mListViews;
    
    public MyPagerAdapter(List<View> paramList)
    {
      this.mListViews = paramList;
    }
    
    public void destroyItem(View paramView, int paramInt, Object paramObject)
    {
      ((ViewPager)paramView).removeView((View)this.mListViews.get(paramInt));
    }
    
    public void finishUpdate(View paramView) {}
    
    public int getCount()
    {
      return this.mListViews.size();
    }
    
    public Object instantiateItem(View paramView, int paramInt)
    {
      ((ViewPager)paramView).addView((View)this.mListViews.get(paramInt), 0);
      return this.mListViews.get(paramInt);
    }
    
    public boolean isViewFromObject(View paramView, Object paramObject)
    {
      return paramView == paramObject;
    }
    
    public void restoreState(Parcelable paramParcelable, ClassLoader paramClassLoader) {}
    
    public Parcelable saveState()
    {
      return null;
    }
    
    public void startUpdate(View paramView) {}
  }
}
