# RfidUtils

## 引入

### Gradle:

1. 在Project的 **build.gradle** 里面添加远程仓库

```gradle
allprojects {
    repositories {
        //...
        maven { url 'https://jitpack.io' }
    }
}
```

2. 在Module的 **build.gradle** 里面添加引入依赖项

```gradle
implementation('com.github.coolxiao:RfidUtils:1.2.4') {
        exclude group: "com.android.support"
    }

```

## 示例

代码示例 （二维码/条形码）

```Java
    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rfid.setOnClickListener{
        RFIDSwitcher.instance.open(this,object:TagListener{
        override fun onSuccess(rfid:String?,info:String?){
        Toast.makeText(this@MainActivity,rfid,Toast.LENGTH_SHORT).show()
        RFIDSwitcher.instance.close()
        }
        override fun onLogReceive(log:String?,action:String?){
        }
        })
        }
        }

        override fun onDestroy(){
        super.onDestroy()
        RFIDSwitcher.instance.close()
        }
```



