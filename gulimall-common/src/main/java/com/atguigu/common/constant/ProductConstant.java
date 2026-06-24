package com.atguigu.common.constant;

public class ProductConstant {
    public enum AttrEnum {
        ATTR_TYPE_BASE(1, "Base Attribute"),
        ATTR_TYPE_SALE(0, "Sale Attribute");

        private final int code;
        private final String msg;

        AttrEnum(int code, String msg) {
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

    /** SPU status */
    public enum StatusEnum {
        NEW_SPU(0, "New"),
        SPU_UP(1, "Product on shelf"),
        SPU_DOWN(2, "Product off shelf");

        private final int code;
        private final String msg;

        StatusEnum(int code, String msg) {
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
