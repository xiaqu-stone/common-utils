

```
implementation "com.sqq.xiaqu:common-utils:1.0.0"
```


**每个工具类中的具体方法，请在 AS 中通过 Structure 视图来查看，方法的具体功能都有注释来说明**

- ==ActManager==：Activity管理类，可以全局获取到当前栈顶的Activity；逻辑代码的应用仅与Application中的 ActivityLifecycleCallbacks 耦合
- ==AppExt==: 设备 or 应用级别的工具类
- ==ContextExt==：Context下的一些扩展函数，**备注**：比较难划分归属，也不必强行与 APPExt 做区分，你高兴就好
- ==ActivityExt==：Activity扩展函数的封装
- ==CompressUtils==：关于压缩&解压的工具类，目前目前仅封装了 zip 系列的解压缩
- ==CheckRootUtil==：检查设备是否Root
- ==ViewExt==：View相关的扩展函数
- ==DisplayExt==: 屏幕相关，转换界面尺寸的工具类
- ==EditTextExt==：针对 EditText 的一些扩展封装
- ==EncrptExt==：针对加解密的一些扩展封装（Base64,MD5,Hex,UrlEncode等）
- ==FileExt==：IO 操作的扩展类
- ==FormatExt==：格式化的扩展类（数值，小数，日期等）
- ==HandlerManager==：Handler相关的简单封装
- ==ImageExt==：图片相关操作的扩展类（Drawable，Bitmap，Canvas等）
- ==ImageFilePath==：根据 Uri 获取图片的路径
- ==IntentExt==：Intent相关的扩展（如：系统应用的隐式跳转等）
- ==RegexExt==：正则校验类（手机号，身份证等）
- ==SPExt/SPUtils==：SP的封装类，SPExt是为了进一步方便在Kotlin中使用而扩展出来的，SPUtis是为了方便Java中使用而保留的
- ==QUtil==：一些无法划分，并且不方便将其写成Kotlin扩展函数的工具类