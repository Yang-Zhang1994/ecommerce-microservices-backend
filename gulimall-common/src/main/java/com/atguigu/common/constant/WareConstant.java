package com.atguigu.common.constant;

public class WareConstant {
    public enum PurchaseStatusEnum {
        CREATED(0, "Created"),
        ASSIGNED(1, "Assigned"),
        RECEIVED(2, "Received"),
        FINISHED(3, "Finished"),
        HASERROR(4, "Has Error");

        private final int code;
        private final String msg;

        PurchaseStatusEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }


    public enum PurchaseDetailStatusEnum {
        CREATED(0, "Created"),
        ASSIGNED(1, "Assigned"),
        PURCHASING(2, "Purchasing"),
        FINISHED(3, "Finished"),
        HASERROR(4, "Has Error");

        private final int code;
        private final String msg;

        PurchaseDetailStatusEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }
}
