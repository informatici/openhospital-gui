/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2023 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
 *
 * Open Hospital is a free and open source software for healthcare data management.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * https://www.gnu.org/licenses/gpl-3.0-standalone.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.isf.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoodies.binding.PresentationModel;

public abstract class BaseComponent<PM extends PresentationModel> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseComponent.class);

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
        } catch (Exception exception) {
            LOGGER.error(exception.getMessage(), exception);
            throw new RuntimeException("Unable to construct " + getClass().getName(), exception);
        }
    }

    /**
     * Build the model for the panel.
     */
    protected PM buildModel() throws Exception {
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
