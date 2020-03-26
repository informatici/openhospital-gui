package org.isf.gui;

import com.jgoodies.binding.PresentationModel;

public abstract class BaseComponent<PM extends PresentationModel> {
    PM model;

    /**
     * Build the panel by initialising the components, building the model, binding the
     * model, and initialising the event handling.
     *
     * @see #initComponents()
     * @see #buildModel()
     * @see #bind()
     * @see #initEventHandling()
     */
    public final void build() {
        try {
            this.model = buildModel();
            initComponents();
            bind();
            initEventHandling();
            initGUIState();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to construct " + getClass().getName(), e);
        }
    }

    /**
     * Build the model for the panel.
     */
    protected PM buildModel()
            throws Exception {
        return null;
    }

    public PM presentationModel() {
        return model;
    }

    /**
     * Initialise any GUI state.
     */
    protected void initGUIState() throws Exception {
    }

    /**
     * Initialise the components - this will be implemented by JFormDesigner generated code.
     * @throws Exception dude
     */
    protected void initComponents()
            throws Exception {
    }

    /**
     * Bind the components to the model.
     * @throws Exception dude
     */
    protected void bind() throws Exception {
    }

    /**
     * Register event handlers.
     * @throws Exception dude
     */
    protected void initEventHandling() throws Exception {
    }

}
