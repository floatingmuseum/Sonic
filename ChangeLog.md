#### Version 1.0.9
1. Upgrade database.

#### Version 1.0.8
1. Separated pauseAllForceTask() and pauseAllNormalTask() from pauseAllTask().

#### Version 1.0.7
1. Fixed several bugs.

#### Version 1.0.6
1. Fixed wrong state callback when some download task has finished.

#### Version 1.0.5
1. Fixed ConcurrentModificationException between DownloadTask and DownloadThread.
2. Improve download info response speed.

#### Version 1.0.4
1. Remove task from task queue when exception is MalformedURLException.
2. Replace DownloadListener to LocalBroadcastReceiver.

#### Version 1.0.3
1. Make TaskInfo implements Parcelable

#### Version 1.0.2
1. Fixed FileNotFoundException occurred,but doesn't call onError.
2. Fixed when MalformedURLException and FileNotFoundException occurred,sonic stuck on the way.
3. Support download non-support multi-threads task.

#### Version 1.0.1
1. Not save task state when state is start.
2. Hiding library log as default setting.you can open it when init Sonic in Application.
3. Fixed setDirPath in Application not work.

#### Version 1.0.0
1. Multiple-threading.
2. Breakpoint resume.
3. Custom download config.
4. Download listener.
5. Force download when download queue is full.