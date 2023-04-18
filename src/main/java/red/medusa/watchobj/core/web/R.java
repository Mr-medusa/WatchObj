package red.medusa.watchobj.core.web;

public  class R {
        private boolean success;
        private EventType type;
        private Object data;
        private Object hint;
        private String tip;

        public R(boolean success, String tip, Object data) {
            this.success = success;
            this.tip = tip;
            this.data = data;
        }

        public R(boolean success, String tip, EventType type, Object data) {
            this.success = success;
            this.tip = tip;
            this.data = data;
            this.type = type;
        }

        public R(EventType type, Object data, Object hint, String tip) {
            this.type = type;
            this.data = data;
            this.hint = hint;
            this.tip = tip;
            this.success = true;
        }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }

        public Object getHint() {
            return hint;
        }

        public void setHint(Object hint) {
            this.hint = hint;
        }

        public String getTip() {
            return tip;
        }

        public void setTip(String tip) {
            this.tip = tip;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }
    }