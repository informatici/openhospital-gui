/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2021 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.isf.telemetry;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.event.EventListenerList;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.gui.MainMenu;
import org.isf.telemetry.envdatacollector.AbstractDataCollector;
import org.isf.telemetry.manager.TelemetryManager;
import org.isf.telemetry.model.Telemetry;
import org.isf.telemetry.util.TelemetryUtils;
import org.isf.utils.ExceptionUtils;
import org.isf.utils.exception.OHException;
import org.isf.utils.layout.SpringUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

public class TelemetryGUI extends JDialog {

	private static final long serialVersionUID = 891561833857381224L;

	private static final Logger LOGGER = LoggerFactory.getLogger(Telemetry.class);
	private MainMenu parent;
	private EventListenerList telemetryListeners = new EventListenerList();
	private Telemetry telemetry;

	public TelemetryGUI(MainMenu parent) {
		super(parent, MessageBundle.getMessage("angal.login.title"), true);
		this.parent = parent;
		this.addTelemetryListener(parent);
		ApplicationContext springSexyContext = new ClassPathXmlApplicationContext("applicationContext.xml");
		TelemetryManager telemetryManager = springSexyContext.getBean(TelemetryManager.class);
		TelemetryUtils telemetryUtils = springSexyContext.getBean(TelemetryUtils.class);

		Telemetry telemetry = telemetryManager.retrieveSettings();
		Map<String, Boolean> settings = telemetry != null && telemetry.getConsentMap() != null ? telemetry.getConsentMap() : new HashMap<>();
		List<CheckBoxWrapper> checkboxes = buildPermissionCheckboxes(springSexyContext, settings);
		JButton confirmButton = buildConfirmButton(checkboxes, telemetryManager, telemetryUtils);
		JButton cancelButton = buildCancelButton(telemetryManager);

		add(this.panel(checkboxes, confirmButton, cancelButton));
		pack();

		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screensize = kit.getScreenSize();

		Dimension mySize = getSize();

		setLocationRelativeTo(null);

		setResizable(false);
		setVisible(true);
	}

	public interface TelemetryListener extends EventListener {

		void telemetryInserted(AWTEvent e);
	}

	public void addTelemetryListener(TelemetryListener listener) {
		telemetryListeners.add(TelemetryListener.class, listener);
	}

	private JButton buildConfirmButton(List<CheckBoxWrapper> checkboxes, TelemetryManager telemetryManager, TelemetryUtils telemetryUtils) {
		JButton confirmButton = new JButton("Confirm");
		confirmButton.addActionListener(buildConfirmationActionListener(checkboxes, telemetryManager, telemetryUtils));
		return confirmButton;
	}

	private JButton buildCancelButton(TelemetryManager telemetryManager) {
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(buildCancelButtonActionListener(telemetryManager));
		return cancelButton;
	}

	/**
	 * We could load checkboxes information from somewhere (properties file or from other strange places)
	 * 
	 * @return
	 */
	private List<CheckBoxWrapper> buildPermissionCheckboxes(ApplicationContext springSexyContext, Map<String, Boolean> consentMap) {

		Map<String, AbstractDataCollector> checkboxContractMap = springSexyContext.getBeansOfType(AbstractDataCollector.class);
		List<AbstractDataCollector> checkboxContractList = new ArrayList<>(checkboxContractMap.values());
		Collections.sort(checkboxContractList, AnnotationAwareOrderComparator.INSTANCE);

		List<CheckBoxWrapper> result = new ArrayList<>();

		int[] i = { 0 };
		checkboxContractList.forEach(springCheckboxConfigurationBean -> {
			JCheckBox chb = new JCheckBox(springCheckboxConfigurationBean.getDescription(), springCheckboxConfigurationBean.isSelected(consentMap));
			CheckBoxWrapper wrapper = new CheckBoxWrapper();
			wrapper.setCheckbox(chb);
			wrapper.setId(springCheckboxConfigurationBean.getId());
			wrapper.setOrder(Integer.valueOf(i[0]++));
			result.add(wrapper);
		});

		return result;
	}

	private void fireTelemetryInserted(Telemetry telemetry) {
		AWTEvent event = new AWTEvent(telemetry, AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = telemetryListeners.getListeners(TelemetryListener.class);
		for (EventListener listener : listeners) {
			((TelemetryListener) listener).telemetryInserted(event);
		}
	}

	/**
	 * Action for confirmation button: inserts for the first time in the telemetry table or updates the existing row
	 * 
	 * @param permissions
	 * @param telemetryManager
	 * @return
	 */
	private ActionListener buildConfirmationActionListener(List<CheckBoxWrapper> checkboxes, TelemetryManager telemetryManager, TelemetryUtils telemetryUtils) {
		return new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Map<String, Boolean> consentMap = buildConsentData(checkboxes);
				if (this.isReallyEnabled(consentMap)) {
					telemetry = telemetryManager.enable(consentMap);
					fireTelemetryInserted(telemetry);
					try {
						telemetryUtils.sendTelemetryData(consentMap);
					} catch (RuntimeException | OHException f) {
						LOGGER.error("Something strange happened: " + f.getMessage());
						LOGGER.error(ExceptionUtils.retrieveExceptionStacktrace(f));
					}
				} else {
					telemetryManager.disable(new HashMap<>());
				}
				removeTelemetryListener(parent);
				dispose();
			}

			private void removeTelemetryListener(TelemetryListener listener) {
				telemetryListeners.remove(TelemetryListener.class, listener);
			}

			private boolean isReallyEnabled(Map<String, Boolean> cd) {
				if (cd == null || cd.isEmpty()) {
					return false;
				}
				return cd.keySet().stream().anyMatch(key -> Boolean.TRUE.equals(cd.get(key)));
			}

			private Map<String, Boolean> buildConsentData(List<CheckBoxWrapper> permissions) {

				Map<String, Boolean> result = new HashMap<>();
				permissions.forEach(per -> {
					result.put(per.getId(), Boolean.valueOf(per.getCheckbox().isSelected()));
				});

				return result;
			}

		};
	}

	private ActionListener buildCancelButtonActionListener(TelemetryManager telemetryManager) {
		return new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		};

	}

	public JPanel panel(List<CheckBoxWrapper> checkboxes, JButton confirmButton, JButton cancelButton) {

		JPanel panel = new JPanel(new SpringLayout());

		panel.add(new JLabel(MessageBundle.getMessage("angal.common.userid.label")));
		checkboxes.forEach(chb -> {
			panel.add(chb.getCheckbox());
		});
		SpringUtilities.makeCompactGrid(panel, checkboxes.size(), 1, 5, 5, 5, 5);

		JPanel buttons = new JPanel();
		buttons.setLayout(new FlowLayout());
		buttons.add(confirmButton);
		buttons.add(cancelButton);

		setLayout(new BorderLayout(10, 10));
		add(panel, BorderLayout.NORTH);
		add(buttons, BorderLayout.SOUTH);
		return panel;
	}

}