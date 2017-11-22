# Introduction

Android-spring is a android library project support IOC , DI , AOP and HTTP/Handler , it use annotation to config 。 It contains a simple project. 


* Step 1. Add the dependency

```gradle
dependencies {
	compile 'com.hianzuo.spring:spring:1.0'
}
```

## Implement

```XML
<nl.dionsegijn.steppertouch.StepperTouch
        android:id="@+id/stepperTouch"
        android:layout_width="100dp"
        android:layout_height="40dp" />
```

Quick example written in Kotlin:

```Kotlin
val stepperTouch = findViewById(R.id.stepperTouch) as StepperTouch
stepperTouch.stepper.setMin(0)
stepperTouch.stepper.setMax(3)
stepperTouch.stepper.addStepCallback(object : OnStepCallback {
	override fun onStep(value: Int, positive: Boolean) {
    	Toast.makeText(applicationContext, value.toString(), Toast.LENGTH_SHORT).show()
	}
})
```

You are able to further customize or set initial values with styled attributes: 

Add res-auto to your xml layout if you haven't yet
 
```XML
xmlns:app="http://schemas.android.com/apk/res-auto"
``` 

After that the following attributes will become available:

```XML
app:stepperBackgroundColor=""
app:stepperButtonColor=""
app:stepperActionsColor=""
app:stepperActionsDisabledColor=""
app:stepperTextColor=""
app:stepperTextSize=""
```
