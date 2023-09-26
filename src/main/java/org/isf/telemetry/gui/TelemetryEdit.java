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
package org.isf.telemetry.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.event.EventListenerList;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.gui.MainMenu;
import org.isf.menu.manager.Context;
import org.isf.telemetry.daemon.TelemetryDaemon;
import org.isf.telemetry.envdatacollector.AbstractDataCollector;
import org.isf.telemetry.envdatacollector.constants.CollectorsConstants;
import org.isf.telemetry.manager.TelemetryManager;
import org.isf.telemetry.model.Telemetry;
import org.isf.telemetry.util.TelemetryUtils;
import org.isf.utils.ExceptionUtils;
import org.isf.utils.exception.OHException;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.layout.SpringUtilities;
import org.isf.utils.time.TimeTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

public class TelemetryEdit extends ModalJFrame {

	private static final long serialVersionUID = 891561833857381224L;

	private static final Logger LOGGER = LoggerFactory.getLogger(TelemetryEdit.class);

	private static final String KEY_TELEMETRY_TITLE = "angal.telemetry.title";
	private static final String KEY_TELEMETRY_ABOUT = "angal.telemetry.about";
	private static final String KEY_TELEMETRY_INFO = "angal.telemetry.info";
	private static final String KEY_TELEMETRY_BODY = "angal.telemetry.body";
	private static final String KEY_TELEMETRY_BUTTON_LABEL_CONFIRM = "angal.telemetry.button.label.confirm";
	private static final String KEY_TELEMETRY_BUTTON_LABEL_ASK_LATER = "angal.telemetry.button.label.ask.later";
	private static final String KEY_TELEMETRY_BUTTON_LABEL_ASK_NEVER = "angal.telemetry.button.label.ask.never";
	private static final String KEY_TELEMETRY_CONFIRMATION_DIALOG_MESSAGE = "angal.telemetry.confirmation.dialog.message";

	private EventListenerList telemetryListeners = new EventListenerList();
	private JPanel panel;
	private TelemetryManager telemetryManager = Context.getApplicationContext().getBean(TelemetryManager.class);
	private TelemetryUtils telemetryUtils = Context.getApplicationContext().getBean(TelemetryUtils.class);

	public TelemetryEdit() {
		super();
		init();
	}

	public TelemetryEdit(MainMenu parent) {
		super();
		init();
		super.showAsModal(parent);
	}

	private void init() {
		Telemetry telemetry = telemetryManager.retrieveSettings();
		Map<String, Boolean> settings = telemetry != null && telemetry.getConsentMap() != null
						? telemetry.getConsentMap()
						: new HashMap<>();
		List<CheckBoxWrapper> checkboxes = buildPermissionCheckboxes(Context.getApplicationContext(), settings);
		JButton confirmButton = buildConfirmButton(checkboxes);
		JButton askMeLaterButton = buildAskMeLaterButton();
		JButton neverAskButton = buildNeverAskButton();
		this.panel = this.makePanel(checkboxes, confirmButton, askMeLaterButton, neverAskButton);
		add(this.panel);
		pack();

		setTitle(MessageBundle.getMessage(KEY_TELEMETRY_TITLE));
		setResizable(false);
		setSize(new Dimension(700, 400));
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public interface TelemetryListener extends EventListener {

		void telemetryInserted(AWTEvent e);
	}

	public void addTelemetryListener(TelemetryListener listener) {
		telemetryListeners.add(TelemetryListener.class, listener);
	}

	private JButton buildConfirmButton(List<CheckBoxWrapper> checkboxes) {
		JButton confirmButton = new JButton(MessageBundle.getMessage(KEY_TELEMETRY_BUTTON_LABEL_CONFIRM));
		confirmButton.addActionListener(buildConfirmationActionListener(checkboxes, telemetryManager, telemetryUtils));
		return confirmButton;
	}

	private JButton buildAskMeLaterButton() {
		JButton cancelButton = new JButton(MessageBundle.getMessage(KEY_TELEMETRY_BUTTON_LABEL_ASK_LATER));
		cancelButton.addActionListener(buildAskMeLaterButtonActionListener(telemetryManager));
		return cancelButton;
	}

	private JButton buildNeverAskButton() {
		JButton cancelButton = new JButton(MessageBundle.getMessage(KEY_TELEMETRY_BUTTON_LABEL_ASK_NEVER));
		cancelButton.addActionListener(buildNeverAskButtonActionListener(telemetryManager));
		return cancelButton;
	}

	/**
	 * We could load checkboxes information from somewhere (properties file or from
	 * other strange places)
	 * 
	 * @return
	 */
	private List<CheckBoxWrapper> buildPermissionCheckboxes(ApplicationContext applicationContext,
					Map<String, Boolean> consentMap) {

		Map<String, AbstractDataCollector> checkboxContractMap = applicationContext.getBeansOfType(AbstractDataCollector.class);
		List<AbstractDataCollector> checkboxContractList = new ArrayList<>(checkboxContractMap.values());
		Collections.sort(checkboxContractList, AnnotationAwareOrderComparator.INSTANCE);

		List<CheckBoxWrapper> result = new ArrayList<>();

		int[] i = { 0 };
		checkboxContractList.forEach(springCheckboxConfigurationBean -> {
			JCheckBox chb = new JCheckBox(springCheckboxConfigurationBean.getDescription(),
							springCheckboxConfigurationBean.isSelected(consentMap));
			CheckBoxWrapper wrapper = new CheckBoxWrapper();
			wrapper.setCheckbox(chb);
			wrapper.setId(springCheckboxConfigurationBean.getId());
			wrapper.setOrder(Integer.valueOf(i[0]++));
			if (springCheckboxConfigurationBean.getId().equals("TEL_ID")) {
				// Compulsory Collector
				wrapper.getCheckbox().setSelected(true);
				wrapper.getCheckbox().setEnabled(false);
			}
			result.add(wrapper);
		});

		return result;
	}

	/**
	 * Action for confirmation button: inserts for the first time in the telemetry
	 * table or updates the existing row
	 * 
	 * @param permissions
	 * @param telemetryManager
	 * @return
	 */
	private ActionListener buildConfirmationActionListener(List<CheckBoxWrapper> checkboxes,
					TelemetryManager telemetryManager, TelemetryUtils telemetryUtils) {
		JPanel panel = this.panel;
		return new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Map<String, Boolean> consentMap = buildConsentData(checkboxes);
				if (this.isReallyEnabled(consentMap)) {
					try {
						Telemetry telemetry = telemetryManager.enable(consentMap);
						Map<String, Map<String, String>> dataToSend = telemetryUtils.retrieveDataToSend(consentMap);
						dataToSend = prepareDataToSend(telemetry, dataToSend);

						if (isShowDialog(dataToSend) == JOptionPane.OK_OPTION) {
							LOGGER.debug("Trying to send data...");
							telemetryManager.save(telemetry);
							telemetryUtils.sendTelemetryData(dataToSend, GeneralData.DEBUG);
							TelemetryDaemon.getTelemetryDaemon().reloadSettings();
						} else {
							LOGGER.debug("User canceled action.");
							return;
						}
					} catch (RuntimeException | OHException f) {
						LOGGER.error("Something strange happened: " + f.getMessage());
						LOGGER.error(ExceptionUtils.retrieveExceptionStacktrace(f));
					}
				} else {
					Telemetry telemetry = telemetryManager.disable(new HashMap<>());
					telemetryManager.save(telemetry);
				}
				dispose();
			}

			private Map<String, Map<String, String>> prepareDataToSend(Telemetry telemetry, Map<String, Map<String, String>> dataToSend) {
				Map<String, String> tempTelemetryData = dataToSend.get("TEL_ID");
				telemetry.setSentTimestamp(LocalDateTime.now());
				if (null == telemetry.getSentTimestamp()) {
					telemetry.setSentTimestamp(LocalDateTime.now());
				}
				tempTelemetryData.put(CollectorsConstants.TEL_SENT_DATE, TimeTools.formatDateTimeReport(telemetry.getSentTimestamp()));
				if (null != telemetry.getOptinDate()) {
					tempTelemetryData.put(CollectorsConstants.TEL_OPTIN_DATE, TimeTools.formatDateTimeReport(telemetry.getOptinDate()));
				} else {
					tempTelemetryData.remove(CollectorsConstants.TEL_OPTIN_DATE);
				}
				if (null != telemetry.getOptoutDate()) {
					tempTelemetryData.put(CollectorsConstants.TEL_OPTOUT_DATE, TimeTools.formatDateTimeReport(telemetry.getOptoutDate()));
				} else {
					tempTelemetryData.remove(CollectorsConstants.TEL_OPTOUT_DATE);
				}
				dataToSend.put("TEL_ID", tempTelemetryData);
				return dataToSend;
			}

			private int isShowDialog(Map<String, Map<String, String>> dataToSend) {
				List<List<String>> lists = new ArrayList<>();
				dataToSend.keySet().forEach(function -> {
					Map<String, String> data = dataToSend.get(function);
					data.keySet().forEach(dataKey -> {
						List<String> keyValue = new ArrayList<>();
						keyValue.add(function + " | " + dataKey);
						keyValue.add(data.get(dataKey));
						lists.add(keyValue);
					});
				});

				String[][] rows = convertToArray(lists);
				Object[] cols = { "Key", "Value" };
				JTable table = new JTable(rows, cols);
				table.setSize(new Dimension(450, 200));
				String message = MessageBundle.getMessage(KEY_TELEMETRY_CONFIRMATION_DIALOG_MESSAGE);

				Label lb = new Label(message);
				JScrollPane scrollableTable = new JScrollPane(table);

				JPanel buttonPane = new JPanel();
				buttonPane.add(lb);
				buttonPane.add(scrollableTable);

				String title = MessageBundle.getMessage(KEY_TELEMETRY_TITLE);

				int result = JOptionPane.showConfirmDialog(panel, buttonPane, title, JOptionPane.YES_NO_OPTION,
								JOptionPane.OK_CANCEL_OPTION);
				LOGGER.debug("Dialog result: {}", result);
				return result;
			}

			private String[][] convertToArray(List<List<String>> lists) {
				String[][] rows = new String[lists.size()][];
				String[] blankArray = new String[0];
				for (int i = 0; i < lists.size(); i++) {
					rows[i] = lists.get(i).toArray(blankArray);
				}
				return rows;
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

	private ActionListener buildAskMeLaterButtonActionListener(TelemetryManager telemetryManager) {
		return new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		};

	}

	private ActionListener buildNeverAskButtonActionListener(TelemetryManager telemetryManager) {
		return new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Telemetry telemetry = telemetryManager.disable(new HashMap<>());
				telemetryManager.save(telemetry);

				// send opt-out info before stopping
				Map<String, Boolean> consentMap = new HashMap<String, Boolean>();
				consentMap.put("TEL_ID", true);
				try {
					LOGGER.info("Trying to send a last opt-out message...");
					Map<String, Map<String, String>> dataToSend = telemetryUtils.retrieveDataToSend(consentMap);
					telemetryUtils.sendTelemetryData(dataToSend, GeneralData.DEBUG);
				} catch (RuntimeException | OHException sendException) {
					LOGGER.error("Something strange happened: " + sendException.getMessage());
					LOGGER.error(ExceptionUtils.retrieveExceptionStacktrace(sendException));
				}

				TelemetryDaemon.getTelemetryDaemon().reloadSettings();
				TelemetryDaemon.getTelemetryDaemon().stop();
				dispose();
			}
		};

	}

	public JPanel makePanel(List<CheckBoxWrapper> checkboxes, JButton confirmButton, JButton askMeLaterButton,
					JButton neverAskButton) {

		JPanel panel = new JPanel(new SpringLayout());

		panel.add(makeTextArea(KEY_TELEMETRY_ABOUT));
		panel.add(makeTextArea(KEY_TELEMETRY_INFO));
		panel.add(makeTextArea(KEY_TELEMETRY_BODY));

		checkboxes.forEach(chb -> {
			panel.add(chb.getCheckbox());
		});

		SpringUtilities.makeCompactGrid(panel, checkboxes.size() + 3, 1, 5, 5, 5, 5);

		JPanel buttons = new JPanel();
		buttons.setLayout(new FlowLayout());
		buttons.add(confirmButton);
		buttons.add(askMeLaterButton);
		buttons.add(neverAskButton);

		setLayout(new BorderLayout(10, 10));
		add(panel, BorderLayout.NORTH);
		add(buttons, BorderLayout.SOUTH);
		return panel;
	}

	private JTextArea makeTextArea(String keyCode) {
		JTextArea textArea = new JTextArea(MessageBundle.getMessage(keyCode));
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setOpaque(false);
		textArea.setEditable(false);
		return textArea;
	}

}