package com.example.ljd.retrofit.pojo;

/**
 * Created by ljd on 4/2/16.
 */
public class Example {


    /**
     * type : object
     * properties : {"foo":{"type":"string"},"bar":{"type":"integer"},"baz":{"type":"boolean"}}
     */

    private String type;
    /**
     * foo : {"type":"string"}
     * bar : {"type":"integer"}
     * baz : {"type":"boolean"}
     */

    private PropertiesBean properties;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public PropertiesBean getProperties() {
        return properties;
    }

    public void setProperties(PropertiesBean properties) {
        this.properties = properties;
    }

    public static class PropertiesBean {
        /**
         * type : string
         */

        private FooBean foo;
        /**
         * type : integer
         */

        private BarBean bar;
        /**
         * type : boolean
         */

        private BazBean baz;

        public FooBean getFoo() {
            return foo;
        }

        public void setFoo(FooBean foo) {
            this.foo = foo;
        }

        public BarBean getBar() {
            return bar;
        }

        public void setBar(BarBean bar) {
            this.bar = bar;
        }

        public BazBean getBaz() {
            return baz;
        }

        public void setBaz(BazBean baz) {
            this.baz = baz;
        }

        public static class FooBean {
            private String type;

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }
        }

        public static class BarBean {
            private String type;

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }
        }

        public static class BazBean {
            private String type;

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }
        }
    }
}
