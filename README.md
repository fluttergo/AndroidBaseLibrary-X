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
String jsonData = {"name":"tom","img":"/img/tomavator.jpg"} 
int[] ids= {R.id.textview,R.id.imageview };
String[] keys = {"Name","img"};
JsonAdapter jsonAdapter = new JsonAdapter( jsonData,R.layout.item,ids,keys);
jsonAdapter.configHost("WWW.android.com");
XListView.setAdapter(jsonAdapter);
```

* 下拉刷新,到底自动加载更多
*  分页append
*  JsonAdapter,UrlAdapter
