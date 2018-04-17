### Sonic
Sonic is a android download library.

##### [Sample Apk](https://github.com/floatingmuseum/Sonic/raw/master/apk/Sonic_sample.apk)
##### [Change Log](https://github.com/floatingmuseum/Sonic/blob/master/ChangeLog.md)

### Features

1. MultiThreading.
2. MultiTask.
3. Breakpoint resume.
4. Auto retry.

### How to use

#### Step1
Add dependency to your build.gradle.
```groovy
dependencies{
	compile 'com.floatingmuseum:sonic:1.0.6'
}
```
Add permission to your AndroidManifest.xml.
Request permission at runtime if your android version higher than or equal 6.0.
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```
#### Step2
Init sonic at your application class.
```java
Sonic.getInstance.init(getApplicationContext());
```
custom config if you want.
```java
Sonic.getInstance()
      .setActiveTaskNumber(2)//Default is 3.
      .setMaxThreads(5)//Default is 3.
      .setProgressResponseInterval(300)//Default is 500.
      .setRetryTime(4)//Default is 5.
      .setReadTimeout(3000)//Default is 5000.
      .setConnectTimeout(3000)//Default is 5000.
      .setBroadcastAction("Floatingmuseum")//Default is your packagname.
      .setDirPath(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath())//Default is sdcard/Download
      .init(getApplicationContext());
```
#### Step3
Start use.
```java
//Create a BroadcastReceiver to receive download info.
BroadcastReceiver downloadReceiver = new BroadcastReceiver(){
	@Override
	public void onReceive(Context context,Intent intent){
    	TaskInfo taskInfo = intent.getParcelableExtra(Sonic.EXTRA_DOWNLOAD_TASK_INFO);
        switch(taskInfo.getState()){
        	case Sonic.STATE_NONE:
            	break;
            case Sonic.STATE_START:
            	break;
            case Sonic.STATE_START:
            	break;
            case Sonic.STATE_WAITING:
            	break;
            case Sonic.STATE_PAUSE:
            	break;
            case Sonic.STATE_DOWNLOADING:
            	DownloadException exception = (DownloadException) intent.getSerializableExtra(Sonic.EXTRA_DOWNLOAD_EXCEPTION);
            	break;
            case Sonic.STATE_ERROR:
            	break;
            case Sonic.STATE_FINISH:
            	break;
            case Sonic.STATE_CANCEL:
            	break;
        }
    }
}
//Use LocalBroadcastManager register your receiver.If you made custom BroadcastAction at step2,add action in filter.
IntentFilter filter = new IntentFilter();
filter.addAction("FloatingMuseum");
LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
                
//Start a simple download,3 ways
sonic.addTask(url);
sonic.addTask(url,tag);
sonic.addTask(url,tag,fileName);

//start a DownloadRequest,custom multiple config for a single task if you need.
DownloadRequest request = new DownloadRequest().setUrl(url)
                            .setTag("tag")
                            .setFileName("test.apk")
                            .setDirPath(Environment.getExternalStorageDirectory().getAbsolutePath())
                            .setMaxThreads(4)
                            .setRetryTime(5)
                            .setConnectTimeout(5000)
                            .setReadTimeout(5000)
                            .setProgressResponseInterval(400)
                            .setForceStart(Sonic.FORCE_START_YES);
sonic.addTask(request);

//pause download
sonic.pauseTask(tag);

//pause all task
sonic.pauseAllTask();

//cancel task,remove all infomation about task,include database and loca file.
sonic.cancelTask(tag);
```