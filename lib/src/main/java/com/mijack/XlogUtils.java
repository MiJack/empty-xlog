package com.mijack;

import android.content.ComponentName;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.Process;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import static com.mijack.XlogBuilder.KEY_TO_VALUE;
import static com.mijack.XlogBuilder.KEY_TO_VALUE2;

/**
 * @author Mr.Yuan
 * @since 2017/4/1
 */
public class XlogUtils {

    private static String processName;
    private static final int USER_METHOD_STACK_INDEX = 4;
    private static final int SYSTEM_METHOD_STACK_INDEX = 6;
    public static final char LINE_SPLIT_CHAR = '\t';

    public static final char PARAMS_SPLIT_CHAR = '\t';


    public static String paramsToString(Object... objects) {
        StringBuilder sb = new StringBuilder();
        sb.append("\"params\":[");
        for (int i = 0; objects != null && i < objects.length; i++) {
            if (i != 0) {
                sb.append(",");
            }
            sb.append(object2String(objects[i]))
                    .append(PARAMS_SPLIT_CHAR);
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 将object转化成json
     *
     * @param o
     * @return
     */
    public static String object2String(Object o) {
        if (o == null) {
            return "null";
        }
        // 判断是否为基本类型
        String basicType = getBasicType(o);
        if (basicType != null) {
            return String.format("{\"type\":\"%s\",\"data\":\"%s\"}", basicType, o.toString());
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(String.format(KEY_TO_VALUE, "class", o.getClass().getName()))
                .append(",").append(String.format(KEY_TO_VALUE, "hashcode", System.identityHashCode(o)));
        if (o instanceof Member) {
            Member member = (Member) o;
            sb.append(",").append(String.format(KEY_TO_VALUE, "method", method2String(member)));
        } else if (o instanceof String) {
            sb.append(",").append(String.format(KEY_TO_VALUE, "string", o.toString()));
        } else if (o instanceof Throwable) {
            Throwable throwable = (Throwable) o;
            sb.append(",").append(String.format(KEY_TO_VALUE, "message", throwable.getMessage()));
        } else if (o instanceof ComponentName) {
            ComponentName componentName = (ComponentName) o;
            sb.append(",").append(String.format(KEY_TO_VALUE, "packageName", componentName.getPackageName()))
                    .append(",").append(String.format(KEY_TO_VALUE, "shortName", componentName.getShortClassName()));
        } else if (o instanceof IBinder) {
            IBinder iBinder = (IBinder) o;
            String descriptor;
            try {
                descriptor = iBinder.getInterfaceDescriptor();
            } catch (RemoteException e) {
                e.printStackTrace();
                descriptor = "null";
            }
            sb.append(",").append(String.format(KEY_TO_VALUE, "descriptor", descriptor));
        } else if (o instanceof Message) {
            Message message = (Message) o;
            sb.append(",").append(String.format(KEY_TO_VALUE, "what", message.what));
            sb.append(",").append(String.format(KEY_TO_VALUE, "arg1", message.arg1));
            sb.append(",").append(String.format(KEY_TO_VALUE, "arg2", message.arg2));
            sb.append(",").append(String.format(KEY_TO_VALUE2, "target", object2String(message.getTarget())));
            sb.append(",").append(String.format(KEY_TO_VALUE2, "obj", object2String(message.obj)));
        } else if (o instanceof View) {
            View view = (View) o;
            sb.append(",").append(String.format(KEY_TO_VALUE2, "left", String.valueOf(view.getLeft())));
            sb.append(",").append(String.format(KEY_TO_VALUE2, "right", String.valueOf(view.getRight())));
            sb.append(",").append(String.format(KEY_TO_VALUE2, "top", String.valueOf(view.getTop())));
            sb.append(",").append(String.format(KEY_TO_VALUE2, "bottom", String.valueOf(view.getBottom())));
            if (o instanceof TextView) {
                TextView tv = (TextView) view;
                CharSequence text = tv.getText();
                if (text != null) {
                    text = text.toString().replace("\"", "\\\"");
                    sb.append(",").append(String.format(KEY_TO_VALUE, "text", text));
                }
                CharSequence hint = tv.getHint();
                if (!TextUtils.isEmpty(hint)) {
                    hint = hint.toString().replace("\"", "\\\"");
                    sb.append(",").append(String.format(KEY_TO_VALUE, "hint", hint));
                }
            }
        }
        sb.append("}");
        return sb.toString();
    }

    public static String getCurrentThreadInfo() {
        Thread currentThread = Thread.currentThread();
        return currentThread.getName();
    }

    public static int getCurrentThreadId() {
        Thread currentThread = Thread.currentThread();
        return System.identityHashCode(currentThread);
    }

    public static String getProcessName() {
        if (!TextUtils.isEmpty(processName)) {
            return processName;
        }
        processId = Process.myPid();
        BufferedReader cmdlineReader = null;
        try {
            cmdlineReader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(
                            "/proc/" + processId + "/cmdline"),
                    "iso-8859-1"));
            int c;
            StringBuilder sb = new StringBuilder();
            while ((c = cmdlineReader.read()) > 0) {
                sb.append((char) c);
            }
            processName = sb.toString();
            return processName;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (cmdlineReader != null) {
                try {
                    cmdlineReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static final String[] BASIC_TYPES = new String[]{
            "java.lang.Boolean", "java.lang.Byte", "java.lang.Character",
            "java.lang.Double", "java.lang.Float", "java.lang.Integer",
            "java.lang.Long", "java.lang.Short", "java.lang.Void",
            "boolean", "byte", "char", "double", "float", "int", "long", "short"};

    private static String getBasicType(Object o) {
        String name = o.getClass().getName();
        for (String type : BASIC_TYPES) {
            if (type.equals(name)) {
                return type;
            }
        }
        return null;
    }

    public static int processId;

    public static int getProcessId() {
        if (TextUtils.isEmpty(processName)) {
            getProcessName();
        }
        return processId;
    }

    public static void setProcessName(String pName) {
        processName = pName;
        processId = Process.myPid();
    }

    public static String method2String(Member member) {
        try {
            if (member instanceof Method) {
                Method method = (Method) member;
                StringBuilder sb = new StringBuilder();
                sb.append(getTypeName(method.getReturnType())).append(' ');
                sb.append(getTypeName(method.getDeclaringClass())).append('.');
                sb.append(method.getName()).append('(');
                Class<?>[] params = method.getParameterTypes();
                for (int j = 0; j < params.length; j++) {
                    sb.append(getTypeName(params[j]));
                    if (j < (params.length - 1))
                        sb.append(',');
                }
                sb.append(')');
                return sb.toString();
            } else if (member instanceof Constructor) {
                Constructor constructor = (Constructor) member;
                StringBuffer sb = new StringBuffer();
                sb.append(getTypeName(constructor.getDeclaringClass()));
                sb.append("<init>");
                sb.append("(");
                Class<?>[] params = constructor.getParameterTypes();
                for (int j = 0; j < params.length; j++) {
                    sb.append(getTypeName(params[j]));
                    if (j < (params.length - 1))
                        sb.append(",");
                }
                sb.append(")");
                return sb.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "<" + e + ">";
        }
        return null;
    }

    private static String getTypeName(Class<?> type) {
        if (type.isArray()) {
            try {
                Class<?> cl = type;
                int dimensions = 0;
                while (cl.isArray()) {
                    dimensions++;
                    cl = cl.getComponentType();
                }
                StringBuffer sb = new StringBuffer();
                sb.append(cl.getName());
                for (int i = 0; i < dimensions; i++) {
                    sb.append("[]");
                }
                return sb.toString();
            } catch (Throwable e) { /*FALLTHRU*/ }
        }
        return type.getName();
    }

    public static long currentTime() {
        return System.nanoTime();
    }

    public static void appendInvokeLine(int hookId, StringBuilder sb) {
        // user method -> log facade -> log builder -> xlog utils
        Exception exception = new Exception();
        StackTraceElement[] stackTrace = exception.getStackTrace();
        int lineNo = -1;
        String className = "null";
        String methodName = "null";
        int targetIndex = hookId < 0 ? USER_METHOD_STACK_INDEX : SYSTEM_METHOD_STACK_INDEX;
        if (stackTrace != null && stackTrace.length > targetIndex) {
            StackTraceElement stackTraceElement = stackTrace[targetIndex];
            lineNo = stackTraceElement.getLineNumber();
            className = stackTraceElement.getClassName();
            methodName = stackTraceElement.getMethodName();
        }
        sb.append(",").append(String.format(KEY_TO_VALUE, "callerLineNo", lineNo));
        sb.append(",").append(String.format(KEY_TO_VALUE, "callerClassName", className));
        sb.append(",").append(String.format(KEY_TO_VALUE, "callerMethodName", methodName));

    }

    public static Method methodSignToMethod(String methodSign) {
        //void com.mijack.XlogBuilder.logMethodExitInfo(int,com.mijack.XlogBuilder$MethodType,java.lang.String,java.lang.Object,com.mijack.XlogBuilder$MethodExecuteResultType,java.lang.Object,java.lang.Throwable)
        String substring = methodSign.substring(methodSign.indexOf(" "));
        String pre = substring.substring(0, substring.indexOf("("));
        String className = pre.substring(0, pre.lastIndexOf("."));
        String methodName = pre.substring(pre.lastIndexOf("."));
        String args = substring.substring(substring.indexOf("("));
        try {
            Class<?> instanceClazz = Class.forName(className);
            String[] split = args.split(",");
            Class<?>[] objects = new Class[]{};
            if (split.length != 0) {
                objects = new Class[split.length];
                for (int i = 0; i < split.length; i++) {
                    objects[i] = loadClass(split[i], XlogUtils.class.getClassLoader());
                }
            }
            return instanceClazz.getMethod(methodName, objects);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public static Class loadClass(String type, ClassLoader classLoader) throws ClassNotFoundException {
        int lastIndexOf = type.lastIndexOf("[");
        int dimens = lastIndexOf + 1;
        if (dimens > 0) {
            String rawType = type.substring(dimens);
            int[] ds = new int[dimens];
            return Array.newInstance(loadClass(rawType, classLoader), ds).getClass();
        }
        if (boolean.class.getName().equals(type)) {
            return boolean.class;
        } else if (byte.class.getName().equals(type)) {
            return byte.class;
        } else if (char.class.getName().equals(type)) {
            return char.class;
        } else if (double.class.getName().equals(type)) {
            return double.class;
        } else if (float.class.getName().equals(type)) {
            return float.class;
        } else if (int.class.getName().equals(type)) {
            return int.class;
        } else if (long.class.getName().equals(type)) {
            return long.class;
        } else if (short.class.getName().equals(type)) {
            return short.class;
        } else {
            return classLoader.loadClass(type);
        }
    }

}
