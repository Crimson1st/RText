/*
 * 10/10/2009
 *
 * SpellingOptionPanel.java - An option panel for the spelling checker.
 * Copyright (C) 2009 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rsyntaxtextarea;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;

import org.fife.rtext.NumberDocumentFilter;
import org.fife.rtext.RText;
import org.fife.rtext.RTextPrefs;
import org.fife.rtext.RTextUtilities;
import org.fife.rtext.SpellingSupport;
import org.fife.ui.FSATextField;
import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.RColorButton;
import org.fife.ui.RColorSwatchesButton;
import org.fife.ui.SelectableLabel;
import org.fife.ui.LabelValueComboBox;
import org.fife.ui.UIUtil;
import org.fife.ui.rtextfilechooser.RTextFileChooser;


/**
 * Options panel for the spelling checker.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class SpellingOptionPanel extends OptionsDialogPanel {

	private JCheckBox enabledCB;
	private JLabel dictLabel;
	private LabelValueComboBox<String, String> dictCombo;
	private JLabel userDictLabel;
	private FSATextField userDictField;
	private SelectableLabel userDictDescField;
	private JButton userDictBrowse;
	private RTextFileChooser chooser;
	private JLabel colorLabel;
	private RColorSwatchesButton spellingColorButton;
	private JLabel errorsPerFileLabel;
	private JTextField maxErrorsField;
	private JCheckBox viewSpellingWindowCB;

	private Listener listener;
	private ResourceBundle msg;

	private static final String[][] DICTIONARIES = {
		{ "English (United Kingdom)", SpellingSupport.DICTIONARIES[0] },
		{ "English (United States)", SpellingSupport.DICTIONARIES[1] },
	};


	public SpellingOptionPanel() {

		ComponentOrientation orientation = ComponentOrientation.
		getOrientation(getLocale());

		listener = new Listener();
		msg = ResourceBundle.getBundle(
						"org.fife.ui.rsyntaxtextarea.SpellingOptionPanel");
		setName(msg.getString("Title"));

		setLayout(new BorderLayout());
		setBorder(UIUtil.getEmpty5Border());
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setBorder(new OptionPanelBorder(msg.getString("Spelling")));
		add(contentPane);

		Box temp = Box.createVerticalBox();
		contentPane.add(temp, BorderLayout.NORTH);

		enabledCB = new JCheckBox(msg.getString("Enabled"));
		enabledCB.setActionCommand("Enabled");
		enabledCB.addActionListener(listener);
		addLeftAligned(temp, enabledCB, COMPONENT_VERTICAL_SPACING);

		dictLabel = new JLabel(msg.getString("Dictionary"));
		dictCombo = new LabelValueComboBox<>();
		for (String[] dictionary : DICTIONARIES) {
			dictCombo.addLabelValuePair(dictionary[0], dictionary[1]);
		}
		dictCombo.setEditable(false);
		dictCombo.setActionCommand("Dictionary");
		dictCombo.addActionListener(listener);
		JPanel dictComboPanel = new JPanel(new BorderLayout());
		dictComboPanel.add(dictCombo, BorderLayout.LINE_START);
		dictLabel.setLabelFor(dictCombo);

		userDictLabel = new JLabel(msg.getString("UserDictionary"));
		userDictField = new FSATextField(35);
		userDictField.getDocument().addDocumentListener(listener);
		userDictBrowse = new JButton(msg.getString("Browse"));
		userDictBrowse.setActionCommand("BrowseUserDictionary");
		userDictBrowse.addActionListener(listener);
		JPanel userDictFieldPanel = new JPanel(new BorderLayout());
		userDictFieldPanel.add(userDictField);
		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		buttonPanel.add(userDictBrowse);
		userDictFieldPanel.add(buttonPanel, BorderLayout.LINE_END);
		userDictDescField = new SelectableLabel(msg.getString("UserDictionaryDesc"));

		colorLabel = new JLabel(msg.getString("Color"));
		spellingColorButton = new RColorSwatchesButton();
		spellingColorButton.addPropertyChangeListener(
								RColorButton.COLOR_CHANGED_PROPERTY, listener);
		JPanel colorButtonPanel = new JPanel(new BorderLayout());
		colorButtonPanel.add(spellingColorButton, BorderLayout.LINE_START);

		errorsPerFileLabel = new JLabel(msg.getString("MaxErrorsPerFile"));
		maxErrorsField = new JTextField(8);
		((AbstractDocument)maxErrorsField.getDocument()).setDocumentFilter(
										new NumberDocumentFilter());
		maxErrorsField.getDocument().addDocumentListener(listener);
		JPanel maxErrorsPanel = new JPanel(new BorderLayout());
		maxErrorsPanel.add(maxErrorsField, BorderLayout.LINE_START);

		JPanel temp2 = new JPanel(new SpringLayout());
		UIUtil.addLabelValuePairs(temp2, orientation,
			dictLabel, dictComboPanel,
			userDictLabel, userDictFieldPanel,
			Box.createRigidArea(new Dimension(1,1)), userDictDescField,
			colorLabel, colorButtonPanel,
			errorsPerFileLabel, maxErrorsPanel);
		UIUtil.makeSpringCompactGrid(temp2, 5, 2, 0, 0, 5, 5);
		addLeftAligned(temp, temp2, COMPONENT_VERTICAL_SPACING, 20);

		viewSpellingWindowCB = new JCheckBox(msg.getString("ViewSpellingErrorWindow"));
		viewSpellingWindowCB.setActionCommand("ViewSpellingWindow");
		viewSpellingWindowCB.addActionListener(listener);
		addLeftAligned(temp, viewSpellingWindowCB, SECTION_VERTICAL_SPACING);

		JButton rdButton = new JButton(msg.getString("RestoreDefaults"));
		rdButton.setActionCommand("RestoreDefaults");
		rdButton.addActionListener(listener);
		addLeftAligned(temp, rdButton);

		temp.add(Box.createVerticalGlue());

		applyComponentOrientation(orientation);

	}


	@Override
	protected void doApplyImpl(Frame owner) {
		RText rtext = (RText)owner;
		SpellingSupport support = rtext.getMainView().getSpellingSupport();
		support.setSpellCheckingEnabled(enabledCB.isSelected());
		support.setSpellingDictionary(dictCombo.getSelectedValue());
		support.setUserDictionary(getUserDictionary());
		support.setSpellCheckingColor(spellingColorButton.getColor());
		support.setMaxSpellingErrors(getMaxSpellingErrors());
		rtext.setSpellingWindowVisible(viewSpellingWindowCB.isSelected());
	}


	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {

		OptionsPanelCheckResult res = null;

		// Check maximum error count
		String maxErrors = maxErrorsField.getText();
		try {
			int max = Integer.parseInt(maxErrors);
			if (max<0) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException nfe) {
			String desc = msg.getString("Error.InvalidMaxErrors.txt");
			res = new OptionsPanelCheckResult(this, maxErrorsField, desc);
		}

		if (res==null) {

			// Check user dictionary file.  It's okay if it doesn't exist yet,
			// just verify that its parent directory exists, and that it itself
			// isn't a directory.
			File userDict = getUserDictionary();
			if (userDict!=null) {
				if (userDict.isDirectory()) {
					String desc = msg.getString("Error.UserDictionaryIsDirectory.txt");
					res = new OptionsPanelCheckResult(this, userDictField, desc);
				}
				else {
					File parent = userDict.getParentFile();
					if (parent==null || !parent.exists()) {
						String desc = msg.getString("Error.CannotCreateUserDictionary.txt");
						res = new OptionsPanelCheckResult(this, userDictField, desc);
					}
				}
			}

		}

		return res;

	}


	private int getMaxSpellingErrors() {
		try {
			return Integer.parseInt(maxErrorsField.getText().trim());
		} catch (NumberFormatException nfe) { // Shouldn't happen
			return RTextPrefs.DEFAULT_MAX_SPELLING_ERRORS; // Default value
		}
	}


	@Override
	public JComponent getTopJComponent() {
		return enabledCB;
	}


	private File getUserDictionary() {
		String temp = userDictField.getText();
		if (temp.trim().isEmpty()) {
			return null;
		}
		return new File(temp).getAbsoluteFile();
	}


	/**
	 * Toggles whether relevant widgets are enabled based on whether spell
	 * checking is currently enabled.
	 *
	 * @param enabled Whether spell checking is currently enabled.
	 */
	private void setSpellCheckingEnabled(boolean enabled) {
		enabledCB.setSelected(enabled);
		dictLabel.setEnabled(enabled);
		dictCombo.setEnabled(enabled);
		userDictLabel.setEnabled(enabled);
		userDictField.setEnabled(enabled);
		userDictBrowse.setEnabled(enabled);
		userDictDescField.setEnabled(enabled);
		colorLabel.setEnabled(enabled);
		spellingColorButton.setEnabled(enabled);
		errorsPerFileLabel.setEnabled(enabled);
		maxErrorsField.setEnabled(enabled);
		//viewSpellingWindowCB.setEnabled(enabled);
	}


	@Override
	protected void setValuesImpl(Frame owner) {
		RText rtext = (RText)owner;
		SpellingSupport support = rtext.getMainView().getSpellingSupport();
		boolean enabled = support.isSpellCheckingEnabled();
		setSpellCheckingEnabled(enabled);
		dictCombo.setSelectedValue(support.getSpellingDictionary());
		userDictField.setFileSystemAware(false);
		File temp = support.getUserDictionary();
		userDictField.setText(temp==null ? "" : temp.getAbsolutePath());
		userDictField.setFileSystemAware(true);
		spellingColorButton.setColor(support.getSpellCheckingColor());
		maxErrorsField.setText(Integer.toString(
				support.getMaxSpellingErrors()));
		viewSpellingWindowCB.setSelected(rtext.isSpellingWindowVisible());
	}


	/**
	 * Listens for events in this panel.
	 */
	private class Listener implements DocumentListener, ActionListener,
								PropertyChangeListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			String command = e.getActionCommand();

			if ("Enabled".equals(command)) {
				boolean enabled = enabledCB.isSelected();
				setSpellCheckingEnabled(enabled);
				setDirty(true);
			}

			else if ("Dictionary".equals(command)) {
				setDirty(true);
			}

			else if ("BrowseUserDictionary".equals(command)) {
				if (chooser==null) {
					chooser = new RTextFileChooser();
				}
				int rc = chooser.showOpenDialog(null);
				if (rc==RTextFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					userDictField.setFileSystemAware(false);
					userDictField.setText(file.getAbsolutePath());
					userDictField.setFileSystemAware(true);
					setDirty(true);
				}
			}

			else if ("ViewSpellingWindow".equals(command)) {
				setDirty(true);
			}

			else if ("RestoreDefaults".equals(command)) {

				File userDictFile = new File(
						RTextUtilities.getPreferencesDirectory(),
						"userDictionary.txt");
				String userDictFileName = userDictFile.getAbsolutePath();
				Color defaultColor = RTextPrefs.DEFAULT_SPELLING_ERROR_COLOR;
				String defaultMaxErrors = Integer.toString(RTextPrefs.
												DEFAULT_MAX_SPELLING_ERRORS);

				if (!enabledCB.isSelected() ||
						dictCombo.getSelectedIndex()!=1 ||
						!userDictField.getText().equals(userDictFileName) ||
						!spellingColorButton.getColor().equals(defaultColor) ||
						!defaultMaxErrors.equals(maxErrorsField.getText()) ||
						viewSpellingWindowCB.isSelected()) {

					setSpellCheckingEnabled(true);
					dictCombo.setSelectedIndex(1);
					userDictField.setFileSystemAware(false);
					userDictField.setText(userDictFileName);
					userDictField.setFileSystemAware(true);
					spellingColorButton.setColor(defaultColor);
					maxErrorsField.setText(defaultMaxErrors);
					viewSpellingWindowCB.setSelected(false);

					setDirty(true);

				}
			}

		}

		@Override
		public void changedUpdate(DocumentEvent e) {
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			setDirty(true);
		}

		@Override
		public void propertyChange(PropertyChangeEvent e) {

			String prop = e.getPropertyName();

			if (RColorSwatchesButton.COLOR_CHANGED_PROPERTY.equals(prop)) {
				setDirty(true);
			}

		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			setDirty(true);
		}

	}


}
