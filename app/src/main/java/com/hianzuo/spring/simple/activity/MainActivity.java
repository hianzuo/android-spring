package com.hianzuo.spring.simple.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.hianzuo.spring.annotation.Resource;
import com.hianzuo.spring.core.InstanceFactory;
import com.hianzuo.spring.simple.BeanTest;
import com.hianzuo.spring.simple.PrintService;
import com.hianzuo.spring.simple.R;
import com.hianzuo.spring.simple.TestService;

import java.util.HashMap;
import java.util.Map;

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

    public void onClickMeBtnClick(View view) {
        HashMap<String, String> map = new HashMap<>();
        map.put("username", "demo");
        map.put("password", "123");
        testRequest("/api/login", map);
    }

    private void testRequest(String uri, Map<String, String> queryMap) {
        InstanceFactory.newHandler(uri)
                .setParameterMap(queryMap)
                .execute();
    }
}
