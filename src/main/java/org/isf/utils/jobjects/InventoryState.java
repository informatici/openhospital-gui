package org.isf.utils.jobjects;

public class InventoryState {
    public InventoryState() {
    }

    public enum State {
        PROGRESS("1", "angal.inventory.state.inprogress"),
        CANCELED("2", "angal.inventory.state.canceled"),
        VALIDATE("3", "angal.inventory.state.validate");

        String code;
        String label;

        private State(String code, String label) {
            this.code = code;
            this.label = label;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }
}
