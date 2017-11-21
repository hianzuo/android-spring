package com.hianzuo.spring.simple.test.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.hianzuo.spring.R;
import com.hianzuo.spring.annotation.Resource;
import com.hianzuo.spring.simple.test.BeanTest;
import com.hianzuo.spring.simple.test.PrintService;
import com.hianzuo.spring.simple.test.TestService;

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
