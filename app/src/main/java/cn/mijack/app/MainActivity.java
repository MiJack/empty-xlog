package cn.mijack.app;

import android.app.Activity;
import android.os.Bundle;

import com.mijack.XlogStater;
import com.mijack.XlogUtils;

import java.lang.reflect.Method;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        XlogStater.StateMethodType stateType = XlogStater.getStateType("void cn.mijack.app.MainActivity.onCreate(android.os.Bundle)", this, new Object[]{savedInstanceState});
        System.out.println(stateType);
    }
}
