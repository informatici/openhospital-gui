/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2024 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
package org.isf.telemetry.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
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

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
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
import org.isf.utils.exception.OHException;
import org.isf.utils.jobjects.MessageDialog;
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

	private static final String HTML_BODY_P_UNDERLINE_OPEN = "<html><body><p><u>";
	private static final String HTML_BODY_P_UNDERLINE_CLOSE = "</u></p></body></html>";
	private static final String KEY_TELEMETRY_TITLE = "angal.telemetry.title";
	private static final String KEY_TELEMETRY_ABOUT = "angal.telemetry.about.txt";
	private static final String KEY_TELEMETRY_INFO = "angal.telemetry.info";
	private static final String KEY_TELEMETRY_BODY = "angal.telemetry.body";
	private static final String KEY_TELEMETRY_CHECKBOX = "angal.telemetry.checkbox.label";
	private static final String KEY_TELEMETRY_BUTTON_LABEL_CONFIRM_AND_SEND = "angal.telemetry.button.label.confirmandsend";
	private static final String KEY_TELEMETRY_BUTTON_LABEL_ASK_LATER = "angal.telemetry.button.label.askmelater";
	private static final String KEY_TELEMETRY_BUTTON_LABEL_DISABLE_NEVER_ASK = "angal.telemetry.button.label.disableandneveraskagain";
	private static final String KEY_TELEMETRY_BUTTON_LABEL_DISABLE = "angal.telemetry.button.label.disable";
	private static final String KEY_TELEMETRY_BUTTON_LABEL_CLOSE = "angal.common.cancel.btn";
	private static final String KEY_TELEMETRY_CONFIRMATION_DIALOG_MESSAGE = "angal.telemetry.confirmation.dialog.message";

	private EventListenerList telemetryListeners = new EventListenerList();
	private TelemetryManager telemetryManager = Context.getApplicationContext().getBean(TelemetryManager.class);
	private TelemetryUtils telemetryUtils = Context.getApplicationContext().getBean(TelemetryUtils.class);
	private boolean firstTime = false;

	private JCheckBox agreementCheckbox;

	public TelemetryEdit() {
		super();
		init();
	}

	public TelemetryEdit(MainMenu parent, boolean askMeLater) {
		super();
		this.firstTime = askMeLater;
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
		JButton disableNeverAskButton = buildDisableNeverAskButton();
		JButton disableButton = buildDisableButton();
		JButton closeButton = buildCloseButton();
		boolean enabled = telemetry != null && telemetry.getOptoutDate() == null;
		JPanel panel = this.makePanel(checkboxes, confirmButton, askMeLaterButton, disableNeverAskButton, disableButton, closeButton,
						enabled);
		add(panel);
		pack();

		setTitle(MessageBundle.getMessage(KEY_TELEMETRY_TITLE));
		setResizable(false);
		// setSize(new Dimension(600, 400));
		setLocationRelativeTo(null);
		pack();
		setVisible(true);
	}

	public interface TelemetryListener extends EventListener {

		void telemetryInserted(AWTEvent e);
	}

	public void addTelemetryListener(TelemetryListener listener) {
		telemetryListeners.add(TelemetryListener.class, listener);
	}

	private JButton buildConfirmButton(List<CheckBoxWrapper> checkboxes) {
		JButton confirmButton = new JButton(MessageBundle.getMessage(KEY_TELEMETRY_BUTTON_LABEL_CONFIRM_AND_SEND));
		confirmButton.addActionListener(buildConfirmationActionListener(checkboxes, telemetryManager, telemetryUtils));
		return confirmButton;
	}

	private JButton buildAskMeLaterButton() {
		JButton cancelButton = new JButton(MessageBundle.getMessage(KEY_TELEMETRY_BUTTON_LABEL_ASK_LATER));
		cancelButton.addActionListener(buildAskMeLaterButtonActionListener(telemetryManager));
		return cancelButton;
	}

	private JButton buildDisableNeverAskButton() {
		JButton cancelButton = new JButton(MessageBundle.getMessage(KEY_TELEMETRY_BUTTON_LABEL_DISABLE_NEVER_ASK));
		cancelButton.addActionListener(buildDisableNeverAskButtonActionListener(telemetryManager));
		return cancelButton;
	}

	private JButton buildDisableButton() {
		JButton disableButton = new JButton(MessageBundle.getMessage(KEY_TELEMETRY_BUTTON_LABEL_DISABLE));
		disableButton.addActionListener(buildDisableNeverAskButtonActionListener(telemetryManager));
		return disableButton;
	}

	private JButton buildCloseButton() {
		JButton closeButton = new JButton(MessageBundle.getMessage(KEY_TELEMETRY_BUTTON_LABEL_CLOSE));
		closeButton.addActionListener(closeButtonActionListener(telemetryManager));
		return closeButton;
	}

	/**
	 * We could load checkboxes information from somewhere (properties file or from other strange places)
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
	 * Action for confirmation button: inserts for the first time in the telemetry table or updates the existing row
	 * 
	 * @param checkboxes
	 * @param telemetryManager
	 * @return
	 */
	private ActionListener buildConfirmationActionListener(List<CheckBoxWrapper> checkboxes,
					TelemetryManager telemetryManager, TelemetryUtils telemetryUtils) {
		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (!agreementCheckbox.isSelected()) {
					MessageDialog.info(TelemetryEdit.this,
									"angal.telemetry.fmt.confirm.pleaseselecttoproceed",
									MessageBundle.getMessage(KEY_TELEMETRY_CHECKBOX));
					return;
				}
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
						LOGGER.error("Something strange happened: {}", f.getMessage());
						LOGGER.debug(f.getMessage(), f);
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

				Label confirmMessage = new Label(MessageBundle.getMessage(KEY_TELEMETRY_CONFIRMATION_DIALOG_MESSAGE));
				JScrollPane scrollTable = new JScrollPane(table);

				JPanel buttonPane = new JPanel();
				buttonPane.add(confirmMessage);
				buttonPane.add(scrollTable);

				String title = MessageBundle.getMessage(KEY_TELEMETRY_TITLE);

				int result = JOptionPane.showConfirmDialog(TelemetryEdit.this, buttonPane, title, JOptionPane.YES_NO_OPTION,
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
		return actionEvent -> dispose();
	}

	private ActionListener closeButtonActionListener(TelemetryManager telemetryManager) {
		return actionEvent -> dispose();
	}

	private ActionListener buildDisableNeverAskButtonActionListener(TelemetryManager telemetryManager) {
		return actionEvent -> {
			Telemetry telemetry = telemetryManager.disable(new HashMap<>());
			telemetryManager.save(telemetry);

			// send opt-out info before stopping
			Map<String, Boolean> consentMap = new HashMap<>();
			consentMap.put("TEL_ID", true);
			try {
				LOGGER.info("Trying to send a last opt-out message...");
				Map<String, Map<String, String>> dataToSend = telemetryUtils.retrieveDataToSend(consentMap);
				telemetryUtils.sendTelemetryData(dataToSend, GeneralData.DEBUG);
			} catch (RuntimeException | OHException sendException) {
				LOGGER.error("Something strange happened: {}", sendException.getMessage());
				LOGGER.error(sendException.getMessage(), sendException);
			}

			TelemetryDaemon.getTelemetryDaemon().reloadSettings();
			TelemetryDaemon.getTelemetryDaemon().stop();
			dispose();
		};
	}

	public JPanel makePanel(List<CheckBoxWrapper> checkboxes, JButton confirmButton, JButton askMeLaterButton,
					JButton disableNeverAskButton, JButton disableButton, JButton closeButton, boolean enabled) {

		JPanel panel = new JPanel(new SpringLayout());

		panel.add(buildJLabelEnabled(enabled));
		panel.add(makeTextArea(KEY_TELEMETRY_ABOUT));
		panel.add(makeTextArea(KEY_TELEMETRY_INFO));
		panel.add(makeTextArea(KEY_TELEMETRY_BODY));
		checkboxes.forEach(chb -> {
			panel.add(chb.getCheckbox());
		});
		panel.add(buildCheckBoxAgreement(enabled));
		panel.add(Box.createRigidArea(new Dimension(50, 50))); // workaround to resolve tightest pack()

		SpringUtilities.makeCompactGrid(panel, panel.getComponentCount(), 1, 5, 5, 5, 5);

		JPanel buttons = new JPanel();
		buttons.add(confirmButton);
		if (firstTime) {
			buttons.add(askMeLaterButton);
			buttons.add(disableNeverAskButton);
		} else {
			if (enabled) {
				buttons.add(disableButton);
			} else {
				buttons.add(closeButton);
			}
		}

		setLayout(new BorderLayout(10, 10));
		add(panel, BorderLayout.NORTH);
		add(buttons, BorderLayout.SOUTH);
		return panel;
	}

	private JCheckBox buildCheckBoxAgreement(boolean enabled) {
		if (agreementCheckbox == null) {
			StringBuilder agreementText = new StringBuilder(HTML_BODY_P_UNDERLINE_OPEN)
							.append(MessageBundle.getMessage(KEY_TELEMETRY_CHECKBOX))
							.append(HTML_BODY_P_UNDERLINE_CLOSE);
			agreementCheckbox = new JCheckBox(agreementText.toString());
			agreementCheckbox.setSelected(enabled);
		}
		return agreementCheckbox;
	}

	private Component buildJLabelEnabled(boolean enabled) {
		JLabel jLabelEnabled = new JLabel(MessageBundle.getMessage("angal.telemetry.enabled"));
		jLabelEnabled.setIcon(new ImageIcon("rsc/icons/ok_dialog.png"));
		jLabelEnabled.setEnabled(enabled);
		jLabelEnabled.setHorizontalAlignment(SwingConstants.RIGHT);
		return jLabelEnabled;
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
