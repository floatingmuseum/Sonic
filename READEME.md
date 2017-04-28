### Sonic
Sonic is a android download library.

### Features

1. MultiThreading.
2. MultiTask.
3. Breakpoint.
4. Auto retry.

### How to use

#### Step1
Add permission to your AndroidManifest.xml.
Request permission at runtime if your android version higher than or equal 6.0.
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```
#### Step2
Init sonic at your application class.
```java
Sonic.getInstance.init(getApplicationContext()).
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
      .setDirPath(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath())//Default is sdcard/Download
      .init(getApplicationContext());
```
#### Step3
Start use.
```java
Sonic sonic = Sonic.getInstance().registerDownloadListener(new DownloadListener(){
                    @Override
                    public void onStart(TaskInfo taskInfo) {
                        
                    }

                    @Override
                    public void onWaiting(TaskInfo taskInfo) {

                    }

                    @Override
                    public void onPause(TaskInfo taskInfo) {

                    }

                    @Override
                    public void onProgress(TaskInfo taskInfo) {

                    }

                    @Override
                    public void onFinish(TaskInfo taskInfo) {

                    }

                    @Override
                    public void onError(TaskInfo taskInfo, DownloadException downloadException) {

                    }
		});
                
//start download
sonic.addTask(url);

//stop download
sonic.stopTask(tag);

//stop all task
sonic.stopAllTask();

//cancel task
sonic.cancelTask(tag);
```
### Other

for more detail,you can see sample project.
Some memory leak found by using leakcanary.i'm working on it,if you have any solution.please tell me by issue or send email to me.
### To do

1.Custom http headers.
2.Custom http params.
3.Check if server support multithreadings and breakpoint.
4.Improve DownloadException by http response code.

### About me

Email:[floatingmuseumyan@gmail.com](floatingmuseumyan@gmail.com)