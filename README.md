#AndroidBaseLibrary-x

------

 it will be quickly and easy ,if develop an android application base on this library 
 
 android快速开发基类库，集成了很多项目中通用的东西(#ImageLoader# #NetWork# #ListView# #Cache#)，免去重复造轮子的麻烦，直接下载来了即可使用.

**#ImageLoader** **#NetWork** **#ListView** **#Cache**

### NetWork

``` java
//Http网络库,封装常用功能
XHttp.load(String Url,CallBack<? extend Bean> callback);
XHttp.load(String Url,LoadingView<? extend Bean> LoadingView);

public interface LoadingView<T> extend CallBack{
	onStart();
	onProgress(int progress);
	onSuccess(T result);
	onErr(int err,String errMsg);
}
```

*  JSON反序列化 
*  异步多线程池
*  重试,错误处理策略
*  with LoadingView
*  上传/下载
*  缓存策略
*  多数据源配置(configHost)

###ImageLoader

``` java
//图片加载库
XImageLoader.load(ImageView view,String URL);
```

* 加载完成动画
* 设置加载前默认图片,加载出错误图片
* 2级缓存(Disk and Memory)
* 图片大小适配

### ListView

```java
String jsonData = "[{"name":"tom","img":"/img/tomavator.jpg"}]"
int[] ids= {R.id.textview,R.id.imageview };
String[] keys = {"Name","img"};
JsonAdapter jsonAdapter = new JsonAdapter( jsonData,R.layout.item,ids,keys);
jsonAdapter.configHost("WWW.android.com");
XListView.setAdapter(jsonAdapter);
```

* 下拉刷新,到底自动加载更多
*  分页append
*  JsonAdapter,UrlAdapter

```java
        listView = (ListView) view.findViewById(R.id.listView);
        XHttp.get("http://shuiguorili.com:8080/leavemessage", new AjaxCallBack() {
            @Override
            public void onSuccess(String t) {
                super.onSuccess(t);
                listView.setAdapter(new JsonArrayAdapter(view.getContext(),t,R.layout.item_listview_t_i, new String[]{"context"}, new int[] {R.id.tv_name}));
            }
        });
```

###BottomBarNavigation

* 底部导航栏

```java
BottomBarItem[] mFragments = new BottomBarItem[5];
        
        mFragments[0] = new BottomBarItem(BaseFragment.creatBaseFramgent(GoodsListragment.class),R.drawable.home_normal,R.drawable.home_press) ;
        mFragments[1] = new BottomBarItem( BaseFragment.creatBaseFramgent(GoodTypeFragment.class),R.drawable.shopping_normal,R.drawable.shopping_press) ;
        mFragments[2] = new BottomBarItem(BaseFragment.creatBaseFramgent(LeaveMessageListragment.class),R.drawable.smlt_normal,R.drawable.smlt_press) ;
        mFragments[3] = new BottomBarItem(BaseFragment.creatBaseFramgent(GoodsListragment.class),R.drawable.shoppingcart_normal,R.drawable.shoppingcart_press) ;
        mFragments[4] = new BottomBarItem(BaseFragment.creatBaseFramgent(GoodsListragment.class),R.drawable.user_normal,R.drawable.user_press) ;

        BottomBar.switchFragment(this,mFragments,0);

        BottomBar.initBottomBar(this,R.id.container_fragment,R.id.bottomBar,mFragments,new OnSelectListenerImp() {
            @Override
            public void onSelect(List<View> items, BottomBarItem[] mFragments,
                                 int select) {
                super.onSelect(items, mFragments, select);
                BottomBar.switchFragment(BottomNavigationActivity.this,mFragments,select);
                curFragmentTag = mFragments[select].getClass().getSimpleName();
            }
        });
```
