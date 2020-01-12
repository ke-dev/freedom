package com.example.skypageui;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;


//循环播放
public class PictureViewPager extends RelativeLayout {

    private static final int[] TEST_IMAGE_IDS = {R.drawable.a, R.drawable.b, R.drawable.c, R.drawable.d, R.drawable.e};
    // 点点的大小
    private static final int DOT_IV_SIZE = 10;
    // 点点的间距
    private static final int DOT_IV_MARGIN = 5;
    //滑动间隔
    private static final int SCROLL_PERIOD_MILLISECONDS = 2000;

    private ViewPager viewPager = null;
    // 图片数组
    private ImageView[] posterIvs = null;
    // 装点点的容器
    private LinearLayout dotLayout = null;
    // 装点点的数组
    private ImageView[] dotIvs = null;
    //当前显示的页面位置
    private int currentPosition = 0;
    // 页面是否改变
    private boolean isChange = false;
    //手指是否已经按下
    private boolean isActionDown = false;
    // 手指按下的时间，大于1秒的时候进行切换
    private long actionUpTime = 0;

    private MyPagerAdapter adapter = null;

    private Timer timer = null;


    private final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            viewPager.setCurrentItem(msg.what, true);
        }
    };

    public PictureViewPager(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public PictureViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PictureViewPager(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View parent = inflater.inflate(R.layout.layout_viewpager, this, true);
        viewPager = (ViewPager) parent.findViewById(R.id.viewpager);
        dotLayout = (LinearLayout) parent.findViewById(R.id.dot_layout);
        adapter = new MyPagerAdapter();
        viewPager.setAdapter(adapter);
        setPosterImages(context, TEST_IMAGE_IDS);
        viewPager.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isActionDown = true;
                        break;
                    case MotionEvent.ACTION_UP:
                        isActionDown = false;
                        break;
                }
                return false;
            }
        });
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                if (posterIvs.length <= 1) {
                    return;
                }
                currentPosition = position;
                for (int i = 0, resid = 0; i < dotIvs.length; i++) {
                    if (i == (position - 1) || (position == 0 && i == (dotIvs.length - 1))) {
                        resid = R.drawable.shape_dot_focus;
                    } else {
                        resid = R.drawable.shape_dot_unfocus;
                    }
                    dotIvs[i].setBackgroundResource(resid);
                }
                if (currentPosition == 0) {
                    currentPosition = posterIvs.length - 2;
                    isChange = true;
                } else if (currentPosition == posterIvs.length - 1) {
                    currentPosition = 1;
                    isChange = true;
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (isChange && state == ViewPager.SCROLL_STATE_IDLE) {
                    viewPager.setCurrentItem(currentPosition, false);
                }
            }
        });
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                if (isActionDown || viewPager == null || posterIvs == null || posterIvs.length <= 1) {
                    return;
                }
                if (actionUpTime != 0) {
                    if (System.currentTimeMillis() - actionUpTime > SCROLL_PERIOD_MILLISECONDS) {
                        actionUpTime = 0;
                    } else {
                        return;
                    }
                }
                int count = posterIvs.length;
                int nextItem = (viewPager.getCurrentItem() + 1) % count;
                handler.sendEmptyMessage(nextItem);
            }
        }, 1000, SCROLL_PERIOD_MILLISECONDS);
    }

//设置图片
    public void setPosterImages(Context context, int[] imageIds) {
        if (imageIds == null || imageIds.length == 0) {
            return;
        }
        dotLayout.removeAllViews();
        if (imageIds.length == 1) {
            //如果只有一张图则不允许滑动
            posterIvs = new ImageView[1];
            posterIvs[0] = new ImageView(context);
            posterIvs[0].setBackgroundResource(imageIds[0]);
        } else {
            //如果有多张图,则运行循环滚动,并在图片下方显示指示点
            posterIvs = new ImageView[imageIds.length + 2];
            dotIvs = new ImageView[imageIds.length];
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(DOT_IV_SIZE, DOT_IV_SIZE);
            params.setMargins(DOT_IV_MARGIN, DOT_IV_MARGIN, DOT_IV_MARGIN, DOT_IV_MARGIN);
            for (int i = 0, resid = 0, dotIndex = 0; i < posterIvs.length; i++) {
                posterIvs[i] = new ImageView(context);
                if (i == 0) {
                    posterIvs[i].setBackgroundResource(imageIds[imageIds.length - 1]);
                } else if (i == (posterIvs.length - 1)) {
                    posterIvs[i].setBackgroundResource(imageIds[0]);
                } else {
                    dotIndex = i - 1;
                    posterIvs[i].setBackgroundResource(imageIds[dotIndex]);
                    dotIvs[dotIndex] = new ImageView(context);
                    if (dotIndex == 0) {
                        resid = R.drawable.shape_dot_focus;
                    } else {
                        resid = R.drawable.shape_dot_unfocus;
                    }
                    dotIvs[dotIndex].setBackgroundResource(resid);
                    dotLayout.addView(dotIvs[dotIndex], params);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    class MyPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            if (posterIvs == null || posterIvs.length == 0) {
                return 0;
            }
            return posterIvs.length;
        }

        @Override
        public boolean isViewFromObject(@NonNull View arg0, @NonNull Object arg1) {
            return arg0 == arg1;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            ImageView iv = posterIvs[position];
            ((ViewPager) container).addView(iv);
            return iv;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            ((ViewPager) container).removeView(posterIvs[position]);
        }
    }
}