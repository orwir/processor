# processor

[![Build Status](https://travis-ci.org/deadly-cat/processor.svg?branch=master)](https://travis-ci.org/deadly-cat/processor)
[![License](https://raw.githubusercontent.com/novoda/novoda/master/assets/btn_apache_lisence.png)](LICENSE.txt)
[![Download](https://api.bintray.com/packages/deadly-cat/maven/processor/images/download.svg) ](https://bintray.com/deadly-cat/maven/processor/_latestVersion)

Modular android framework for async task processing.

Features:
* Caching results.
* Processing multiple tasks as one(but not atomic) with joining results.
* Same interfaces for working with single process result or collection of results
* Modules for working with memory, filesystem, sqlite and basis-module for network.
* Network module provide base scope for work. What to use only your choice.


### Usage

Add url to Bintray repository in your build.gradle
```groovy
allprojects {
    repositories {
        //... other urls
        maven { url  'http://dl.bintray.com/deadly-cat/maven' }
    }
}
```

Next add dependency for your module
```groovy
dependencies {
    versionProcessorLibrary = '1.1.0' //check latest above

    //core part
    compile "ingvar.android.processor:processor:$versionProcessorLibrary"
    //other parts
    compile "ingvar.android.processor:processor-memory:$versionProcessorLibrary"
    compile "ingvar.android.processor:processor-sqlite:$versionProcessorLibrary"
    compile "ingvar.android.processor:processor-network:$versionProcessorLibrary"
    compile "ingvar.android.processor:processor-filesystem:$versionProcessorLibrary"
}
```

Then you need to extend service from [ProcessorService](https://github.com/deadly-cat/processor/blob/master/processor/src/main/java/ingvar/android/processor/service/ProcessorService.java) and add it to AndroidManifest.xml.
Module 'ultimate' already has [BaseProcessorService](https://github.com/deadly-cat/processor/blob/master/processor-ultimate/src/main/java/ingvar/android/processor/ultimate/service/BaseProcessorService.java) which provide Bitmap/Common caches to filesystem decorated by memory. Also included sources for memory and filesystem.

Next you need to instantiate [Processor](https://github.com/deadly-cat/processor/blob/master/processor/src/main/java/ingvar/android/processor/service/Processor.java) class in your activity.
```java
Processor processor = new Processor(BaseProcessorService.class);
```

And don't forget bind/unbind service in the right places.
```java
@Override
protected void onStart() {
    super.onStart();
    processor.bind(this);
}

@Override
protected void onStop() {
    super.onStop();
    processor.unbind(this);
}
```
Note: Unbinding does not stop your tasks and you need to remove observers. If you use [ContextObserver](https://github.com/deadly-cat/processor/blob/master/processor/src/main/java/ingvar/android/processor/observation/ContextObserver.java) and unbound service from same context your observers removed automatically. Otherwise you need to remove observers by yourself.
```java
processor.removeObservers(groupName);
```

For creating task you need to extend [SingleTask](https://github.com/deadly-cat/processor/blob/master/processor/src/main/java/ingvar/android/processor/task/SingleTask.java) and override method 'SingleTask#process(IObserverManager observerManager, ISource source)'.
For example lets try to create task for gaining weather. As [network source](https://github.com/deadly-cat/processor/blob/master/examples/src/main/java/ingvar/android/processor/examples/weather/network/RetrofitSource.java) used Retrofit.
```java
public class WeatherRequest extends SingleTask<WeatherKey, Weather, RetrofitSource> {
	
	public WeatherRequest(WeatherKey key) {
        super(key, Weather.class, RetrofitSource.class, TimeUnit.HOURS.toMillis(1));
    }

    @Override
    public Weather process(IObserverManager observerManager, RetrofitSource source) {
        return source.getWeatherService().receiveWeather(getTaskKey().getCity());
    }

}
```
More details about usage Retrofit for network you can find in the [weather example](https://github.com/deadly-cat/processor/tree/master/examples/src/main/java/ingvar/android/processor/examples/weather).

If you need to return collection of objects you also need to use **single object** class in the constructor.
```java
public class WeatherRequest extends SingleTask<WeatherKey, List<Weather>, RetrofitSource> {
	
	public WeatherRequest(WeatherKey key) {
        super(key, Weather.class, RetrofitSource.class, TimeUnit.HOURS.toMillis(1));
    }

    @Override
    public List<Weather> process(IObserverManager observerManager, RetrofitSource source) {
        return Arrays.asList(source.getWeatherService().receiveWeather(getTaskKey().getCity()));
    }

}
```
But you need to use [CompositeKey](https://github.com/deadly-cat/processor/blob/master/processor/src/main/java/ingvar/android/processor/persistence/CompositeKey.java) for these tasks.

For receiving results of task you need to implement [IObserver](https://github.com/deadly-cat/processor/blob/master/processor/src/main/java/ingvar/android/processor/observation/IObserver.java)(or AbstractObserver/ContextObserver).
```java
private class WeatherObserver extends ContextObserver<WeatherActivity, Weather> {

        public WeatherObserver(WeatherActivity activity) {
            super(activity);
        }

        @Override
        public void completed(Weather result) {
        	//show result.
        }
}
```
All methods in the IObserver invoked in the *MainThread*.

For executing task you can use one of the following methods:
```java
processor.execute(task, observer)
\\OR
processor.planExecute(task, observer)
```
First directly call service and return Future.
Second also call service if it bound. But if not task will be added to non-ordered queue and after service will be bind execute it.

[AggregatedTask](https://github.com/deadly-cat/processor/blob/master/processor/src/main/java/ingvar/android/processor/task/AggregatedTask.java) used for execution couple of tasks as one operation(but not atomic).

For example if you have a list of items and you need to gain price for them and total amount from external service, but service has api only for gaining price for a single item you can write something like this:
```java
AggregatedTask sumTask = new SumTask();
for(Item item : inventory) {
	sumTask.addTask(new ItemPriceTask(item));
}
processor.execute(sumTask, observer);
```
All inner tasks will be executed in different threads and return result to aggregated task. Number of parallel threads is set AggregatedTask#setThreadsCount(int threadsCount). By default the value is 15.


Module sqlite used for work library [Literepo](https://github.com/deadly-cat/literepo).
For caching results it used special [SqlKey](https://github.com/deadly-cat/processor/blob/master/processor-sqlite/src/main/java/ingvar/android/processor/sqlite/persistence/SqlKey.java) which contains information for creation query to DB.
Example of usage:
```java
public class WeatherKey extends SqlKey {

    private String city;

    public WeatherKey(String city) {
        this.city = city;
        Uri uri = new UriBuilder()
            .authority(WeatherContract.AUTHORITY)
            .table(WeatherContract.Weather.TABLE_NAME)
            .query().eq(WeatherContract.Weather.Col.NAME, city).end()
        .build();
        setUri(uri);
        setProjection(WeatherContract.Weather.PROJECTION);
    }

    public String getCity() {
        return city;
    }

    @Override
    public String toString() {
        return city.toString();
    }
}
```

Other examples of usages you can find in the [examples](https://github.com/deadly-cat/processor/tree/master/examples/src/main/java/ingvar/android/processor/examples) module and unit-tests for modules.
