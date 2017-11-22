# Introduction

Android-spring is a android library project support IOC , DI , AOP and HTTP/Handler , it use annotation to config 。 It contains a simple project. 


### Add the dependency

```gradle
 dependencies {
   compile 'com.hianzuo.android:LibSpring:1.0.0'
 }
```

### Init spring from Application

```java
public class SimpleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //if in dev mode ,please line blow code
        SpringInitializer.devMode();
        //spring init， you can add more package to scan spring component.
        SpringInitializer.init(this,
                "com.hianzuo.spring.simple.test.",
                "other package to scan spring component");
    }
}
```
### DI Support in Activity
```java
public class MainActivity extends AppCompatActivity {

    @Resource
    private TestService testService;

    @Resource
    private PrintService printService;

    @Resource(beanName = "testBean")
    private BeanTest testBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        testService.handle();
        setContentView(R.layout.activity_main);
        TextView tv = findViewById(R.id.tv);
        tv.setText(printService.print() + "\n\n" + testBean.getText());
    }
}
```
### Configuration Bean
```java
@Component
@Configuration
public class TestConfiguration {

    @Bean("testBean")
    public BeanTest bean1() {
        return new BeanTest("bean name in annotation");
    }

    @Bean
    public BeanTest methodIsBeanName() {
        return new BeanTest("method is bean name");
    }
}
```

### AOP 
```java
@Aspect
public class TestServiceAspect {

    @Pointcut("^.*?handle\\(\\).*+$")
    public void handle() {
        System.out.println("AAA TestServiceAspect handle");
    }

    @Before("handle")
    public void before(JointPoint point) {
        System.out.println("AAA TestServiceAspect before");
    }

    @Around(value = "handle")
    public Object around(JointPoint point) {
        System.out.println("AAA TestServiceAspect around start");
        Object result = point.invokeResult();
        System.out.println("AAA TestServiceAspect around end");
        return result;
    }

    @After(value = "handle")
    public void after(JointPoint point) {
        System.out.println("AAA TestServiceAspect after");
    }
```


