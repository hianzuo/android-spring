# Introduction

Android-spring is a android library project support IOC , DI , AOP and HTTP/Handler , it use annotation to config 。 It contains a simple project. 


### Add the dependency

```gradle
 dependencies {
   compile 'com.hianzuo.android:LibSpring:1.0.3'
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

### Service Annotation Support
```java
@Service
public class TestServiceImpl implements TestService {
    @Resource
    private PrintService printService;

    @Resource(beanName = "testBean")
    private BeanTest testBean;

    @Resource(beanName = "methodIsBeanName")
    private BeanTest testBean1;

    @Override
    public void handle() {
        printService.print();
        System.out.println("AAA BeanTest :" + testBean.getText());
        System.out.println("AAA BeanTest1 :" + testBean1.getText());
        System.out.println("AAA TestService.handle.");
    }

    @Override
    public void execute() {
        System.out.println("AAA TestService.execute.");
    }
}
```
### Cache Support
```java
@Component
public class DemoProviderImpl extends AbstractCacheAble<Integer, Demo> {
    @Override
    protected Integer getKey(Demo demo) {
        return demo.getId();
    }

    @Override
    protected List<Demo> loadData() {
        //load Demo data from remote server or database 
        ArrayList<Demo> list = new ArrayList<>();
        list.add(new Demo(1, "aaa"));
        list.add(new Demo(2, "bbb"));
        return list;
    }
}
```
### Http Handler Support
```java
@Handler("/api/login")
public class HttpLoginHandler extends BaseHandler {

    @Override
    protected Object getMethodParamObjectByType(Class<?> type) {
        if(type == LoginData.class){
            String username = (String) getMethodParamObject("username");
            String password = (String) getMethodParamObject("password");
            return new LoginData(username,password);
        }
        return super.getMethodParamObjectByType(type);
    }

    @Override
    protected Object getMethodParamObject(String value) {
        // get value from request.
        // demo request.getParameter(value);
        return null;
    }

    @Resource
    private LoginService loginService;

    //you can use @MethodParam Annotation to get parameter
    /*@HandleMethod
    public void handle(@MethodParam("username") String username, @MethodParam("password") String password) {
        loginService.login(username, password);
    }*/

    //you can get DataModel in Method Param , register in (Object getMethodParamObjectByType(Class<?> type))
    @HandleMethod
    public void handle(LoginData data) {
        loginService.login(data.getUsername(), data.getPassword());
    }
}
```
### Repository Annotation Support

@Repository like @Service Annotation for the Component.


