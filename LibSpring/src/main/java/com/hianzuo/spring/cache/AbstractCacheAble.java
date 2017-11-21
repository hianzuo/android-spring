package com.hianzuo.spring.cache;

import android.os.Handler;

import com.hianzuo.spring.internal.ThreadFactoryUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author Ryan
 * @date 2017/11/14.
 */

public abstract class AbstractCacheAble<Key, Item> implements Comparator<Item> {
    private HashMap<Key, Item> cacheMap;
    private List<Item> items;
    private final Object mLock = new Object();

    /**
     * 如果首次加载，将会返回null，然后会在线程中取执行获取数据并缓存
     *
     * @return 数据
     */
    public List<Item> getAll() {
        if (null != items) {
            return items;
        }
        synchronized (mLock) {
            if (null != items) {
                return items;
            }
            items = listItemFromCache();
        }
        return items;
    }

    private List<Item> listItemFromCache() {
        HashMap<Key, Item> cacheMap = getCacheMap();
        if (null == cacheMap) {
            return null;
        }
        List<Item> values = new ArrayList<>(cacheMap.values());
        Collections.sort(values, this);
        return values;
    }

    public void loadCache() {
        getCacheMap();
    }

    public void clearCache() {
        if (null != cacheMap) {
            cacheMap.clear();
        }
        cacheMap = null;
        if (null != items) {
            items.clear();
        }
        items = null;
    }

    private ExecutorService mExecutor;
    private static Handler uiHandler;

    private HashMap<Key, Item> getCacheMap() {
        if (null != cacheMap) {
            return cacheMap;
        }
        if (null == uiHandler) {
            uiHandler = new Handler();
        }
        synchronized (mLock) {
            if (null != mExecutor) {
                return null;
            }
            mExecutor = ThreadFactoryUtil.createSingle(AbstractCacheAble.class);
            mExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    loadDataPrivate();
                }
            });
            mExecutor.shutdown();
            return null;
        }
    }

    private void loadDataPrivate() {
        try {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    onLoadDataStart();
                }
            });
            loadDataInternal();
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    onLoadDataSuccess();
                }
            });
        } catch (final Exception ex) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    onLoadDataFailure(ex);
                }
            });
        } finally {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    onLoadDataEnd();
                }
            });
        }
    }

    @Override
    public int compare(Item o1, Item o2) {
        return 0;
    }

    protected void onLoadDataStart() {

    }

    protected void onLoadDataSuccess() {

    }

    protected void onLoadDataFailure(Exception ex) {
        ex.printStackTrace();
    }


    private void onLoadDataEnd() {
        try {
            mExecutor.shutdown();
        } catch (Exception ignored) {
        }
        mExecutor = null;
    }

    private void loadDataInternal() {
        List<Item> list = loadData();
        cacheMap = new HashMap<>(16);
        if (null != list) {
            for (Item item : list) {
                cacheMap.put(getKey(item), item);
            }
        }
    }

    /**
     * 获取缓存Key
     *
     * @param item Item
     * @return 缓存Key
     */
    protected abstract Key getKey(Item item);

    /**
     * 加载需要缓存的数据，可能从远端获取
     *
     * @return 数据
     */
    protected abstract List<Item> loadData();
}
