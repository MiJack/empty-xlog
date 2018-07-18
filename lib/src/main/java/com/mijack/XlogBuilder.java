package com.mijack;

/**
 * @author Mi&Jack
 */
public class XlogBuilder {

    public enum MethodType {
        USER_STATIC_METHOD(1), USER_NOT_STATIC_METHOD(2),
        SYSTEM_STATIC_METHOD(3), SYSTEM_NOT_STATIC_METHOD(4);

        public static final String INSTANCE_METHOD_TYPE = "instance_method_type";
        public static final String STATIC_METHOD_TYPE = "static_method_type";
        private int id;

        MethodType(int id) {
            this.id = id;
        }

        public boolean hasInstance() {
            return id % 2 == 0;
        }

        public String typename() {
            return id % 2 == 0 ? INSTANCE_METHOD_TYPE : STATIC_METHOD_TYPE;
        }
    }

    public enum MethodExecuteResultType {
        NO_THING, HAS_RESULT, HAS_THROWABLE
    }

    public static void logMethodEnterInfo(int hookId, MethodType methodType, String methodSign, Object instance, Object... args) {
        // hook by xposed
    }

    public static void logMethodExitInfo(int hookId, MethodType methodType, String methodSign,
                                         Object instance, MethodExecuteResultType resultType, Object result, Throwable throwable) {
        // hook by xposed
    }


}
