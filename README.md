# Introduction

Android-spring is a android library project support IOC , DI , AOP and HTTP/Handler , it use annotation to config 。 It contains a simple project. 


* Step 1. Add the dependency

```gradle
 dependencies {
   compile 'com.hianzuo.android:LibSpring:1.0.0'
 }
```

* Step 2. Init spring from Application

```java
public class SimpleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //if in dev mode ,please line blow code
        SpringInitializer.devMode();
        //spring init.
        SpringInitializer.init(this,"com.hianzuo.spring.simple.test.");
    }
}
```

